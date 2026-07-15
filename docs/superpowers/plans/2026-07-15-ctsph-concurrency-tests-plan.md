# CtSph 并发回归测试实施计划

> **供智能代理执行：** 必须使用 `superpowers:executing-plans`，按任务逐项实施并在检查点复核。

**目标：** 为 `CtSph.lookProcessChain` 补充确定性的并发缓存、同资源唯一性和容量上限回归测试。

**架构：** 测试继续放在现有 `CtSphTest` 中，通过固定线程池、就绪锁存器和起跑锁存器让任务形成真实并发。生产并发模型保持不变，只修复两处已有格式问题；每个测试通过临时错误变体完成红测，随后恢复当前正确实现完成绿测。

**技术栈：** Java 8、JUnit 4、`java.util.concurrent`、Maven Surefire。

## 全局约束

- 只修改 `CtSphTest.java` 和 `CtSph.java` 的两处格式。
- 不新增 JMH benchmark，不新增依赖，不修改公开 API。
- 所有并发等待必须设置超时，线程池必须在 `finally` 中关闭。
- 临时错误变体只能用于红测，验证后必须恢复，不能进入最终差异。

---

### 任务一：并发缓存类型契约

**文件：**

- 修改：`sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java`
- 临时变体：`sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java`

**接口：**

- 使用：`CtSph.getChainMap()`
- 产出：测试方法 `testChainMapSupportsConcurrentAccess()`

- [ ] **步骤 1：添加并发 Map 契约测试**

在测试文件中引入：

```java
import java.util.concurrent.ConcurrentMap;
```

添加测试：

```java
@Test
public void testChainMapSupportsConcurrentAccess() {
    assertTrue("Chain map should support lock-free concurrent reads",
        CtSph.getChainMap() instanceof ConcurrentMap);
}
```

- [ ] **步骤 2：验证红测**

临时将 `CtSph` 的缓存实例改为普通 `HashMap`，运行：

```bash
mvn -pl sentinel-core -am -Dtest=CtSphTest#testChainMapSupportsConcurrentAccess \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：断言失败，提示缓存不支持锁外并发读取。

- [ ] **步骤 3：恢复 `ConcurrentHashMap` 并验证绿测**

恢复当前生产实现后运行相同命令。

预期：1 个测试通过，0 个失败。

- [ ] **步骤 4：提交并发 Map 契约测试**

```bash
git add sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java
git commit -m "test: verify CtSph uses concurrent chain map"
```

---

### 任务二：并发执行辅助方法与同资源唯一性

**文件：**

- 修改：`sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java`
- 临时变体：`sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java`

**接口：**

- 产出：`private <T> List<T> invokeConcurrently(List<Callable<T>> tasks) throws Exception`
- 产出：测试方法 `testLookUpSameSlotChainConcurrently()`

- [ ] **步骤 1：添加并发执行辅助方法**

增加以下 Java 标准库导入：

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
```

添加辅助方法：

```java
private <T> List<T> invokeConcurrently(List<Callable<T>> tasks) throws Exception {
    final int taskCount = tasks.size();
    final ExecutorService executor = Executors.newFixedThreadPool(taskCount);
    final CountDownLatch ready = new CountDownLatch(taskCount);
    final CountDownLatch start = new CountDownLatch(1);
    final List<Future<T>> futures = new ArrayList<Future<T>>(taskCount);

    try {
        for (final Callable<T> task : tasks) {
            futures.add(executor.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new TimeoutException("Timed out waiting for concurrent test start");
                    }
                    return task.call();
                }
            }));
        }

        assertTrue("Concurrent tasks were not ready in time", ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        List<T> results = new ArrayList<T>(taskCount);
        for (Future<T> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        return results;
    } finally {
        start.countDown();
        executor.shutdownNow();
        assertTrue("Executor did not terminate in time", executor.awaitTermination(5, TimeUnit.SECONDS));
    }
}
```

- [ ] **步骤 2：添加同资源并发测试**

```java
@Test
public void testLookUpSameSlotChainConcurrently() throws Exception {
    final int taskCount = 32;
    final ResourceWrapper resource = new StringResourceWrapper("concurrent-resource", EntryType.IN);
    List<Callable<ProcessorSlot<Object>>> tasks =
        new ArrayList<Callable<ProcessorSlot<Object>>>(taskCount);
    for (int i = 0; i < taskCount; i++) {
        tasks.add(new Callable<ProcessorSlot<Object>>() {
            @Override
            public ProcessorSlot<Object> call() {
                return ctSph.lookProcessChain(resource);
            }
        });
    }

    List<ProcessorSlot<Object>> chains = invokeConcurrently(tasks);
    ProcessorSlot<Object> expected = chains.get(0);
    assertNotNull(expected);
    for (ProcessorSlot<Object> chain : chains) {
        assertSame("Same resource should share one slot chain", expected, chain);
    }
    assertEquals(1, CtSph.entrySize());
}
```

- [ ] **步骤 3：验证红测**

临时把 `lookProcessChain` 改成每次调用都创建并返回新 chain 的错误变体，运行：

```bash
mvn -pl sentinel-core -am -Dtest=CtSphTest#testLookUpSameSlotChainConcurrently \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：`assertSame` 失败。

- [ ] **步骤 4：恢复生产实现并验证绿测**

恢复双重检查和 `ConcurrentHashMap` 写入后运行相同命令。

预期：1 个测试通过，0 个失败。

- [ ] **步骤 5：提交同资源并发测试**

```bash
git add sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java
git commit -m "test: cover concurrent CtSph chain lookup"
```

---

### 任务三：容量边界并发创建

**文件：**

- 修改：`sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java`
- 临时变体：`sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java`

**接口：**

- 产出：`private void fillResources(int count)`
- 产出：测试方法 `testLookUpSlotChainAtCapacityConcurrently()`

- [ ] **步骤 1：提取指定数量的缓存填充方法**

```java
private void fillFullResources() {
    fillResources(Constants.MAX_SLOT_CHAIN_SIZE);
}

private void fillResources(int count) {
    for (int i = 0; i < count; i++) {
        ResourceWrapper resourceWrapper = new StringResourceWrapper("test-resource-" + i, EntryType.IN);
        CtSph.getChainMap().put(resourceWrapper, SlotChainProvider.newSlotChain());
    }
}
```

- [ ] **步骤 2：添加容量边界并发测试**

```java
@Test
public void testLookUpSlotChainAtCapacityConcurrently() throws Exception {
    fillResources(Constants.MAX_SLOT_CHAIN_SIZE - 1);
    final int taskCount = 16;
    List<Callable<ProcessorSlot<Object>>> tasks =
        new ArrayList<Callable<ProcessorSlot<Object>>>(taskCount);
    for (int i = 0; i < taskCount; i++) {
        final ResourceWrapper resource =
            new StringResourceWrapper("concurrent-capacity-resource-" + i, EntryType.IN);
        tasks.add(new Callable<ProcessorSlot<Object>>() {
            @Override
            public ProcessorSlot<Object> call() {
                return ctSph.lookProcessChain(resource);
            }
        });
    }

    int createdCount = 0;
    for (ProcessorSlot<Object> chain : invokeConcurrently(tasks)) {
        if (chain != null) {
            createdCount++;
        }
    }
    assertEquals("Only one resource should be created at the capacity boundary", 1, createdCount);
    assertEquals(Constants.MAX_SLOT_CHAIN_SIZE, CtSph.entrySize());
}
```

- [ ] **步骤 3：验证红测**

临时将容量判断从 `>=` 改为 `>`，运行：

```bash
mvn -pl sentinel-core -am -Dtest=CtSphTest#testLookUpSlotChainAtCapacityConcurrently \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：创建数量为 2 且缓存数量达到 6001，断言失败。

- [ ] **步骤 4：恢复 `>=` 并验证绿测**

恢复正确判断后运行相同命令。

预期：1 个测试通过，0 个失败。

- [ ] **步骤 5：提交容量边界并发测试**

```bash
git add sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java
git commit -m "test: cover concurrent CtSph capacity limit"
```

---

### 任务四：格式整理与完整验证

**文件：**

- 修改：`sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java:218`
- 修改：`sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java:229`

**接口：** 无公开接口变化。

- [ ] **步骤 1：修复同步块格式**

```java
synchronized (CHAIN_MAP) {
```

- [ ] **步骤 2：运行完整 `CtSphTest`**

```bash
mvn -pl sentinel-core -am -Dtest=CtSphTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：全部测试通过，0 个失败。

- [ ] **步骤 3：运行 `sentinel-core` 完整测试**

```bash
mvn -pl sentinel-core -am test
```

预期：构建成功，0 个测试失败。

- [ ] **步骤 4：运行 Java 8 编译与差异检查**

```bash
javac --release 8 -cp sentinel-core/target/classes \
  -d /tmp/sentinel-review-final-classes \
  sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java
git diff --check
git status --short
```

预期：Java 编译退出码为 0，差异检查无错误，只包含设计、计划、测试和两处格式变更。

- [ ] **步骤 5：提交格式整理**

```bash
git add sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java
git commit -m "style: fix CtSph synchronized block formatting"
```
