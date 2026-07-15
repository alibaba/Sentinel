# CtSph 并发回归测试设计

## 背景

PR #3592 将 `CtSph` 的 slot chain 缓存从 Copy-on-Write `HashMap` 改为 `ConcurrentHashMap` 原地写入。生产实现仍通过外层同步块串行化首次创建，以保证相同资源只生成一个 `ProcessorSlotChain`、资源总数不超过 `Constants.MAX_SLOT_CHAIN_SIZE`，并保护 `SlotChainProvider.newSlotChain()` 的非线程安全初始化过程。

现有 `CtSphTest` 只验证串行查找和容量限制，没有覆盖多线程同时进入 `lookProcessChain` 时必须保持的行为。

## 范围

本次只修改以下文件：

- `sentinel-core/src/test/java/com/alibaba/csp/sentinel/CtSphTest.java`
- `sentinel-core/src/main/java/com/alibaba/csp/sentinel/CtSph.java` 中两处既有格式问题

不新增 JMH 性能基准，不调整生产并发模型，不修改公开 API。

## 测试方案

### 并发缓存类型契约

验证测试可见的缓存实现满足 `ConcurrentMap` 契约，而不是绑定到某一个具体实现类。这个断言直接保护锁外读取所依赖的安全发布机制，并用于执行回归测试的红绿验证。

### 同一资源并发查找

创建固定大小线程池，让多个任务通过同一个 `CountDownLatch` 同时调用 `lookProcessChain`。收集所有 `Future` 返回值并验证：

- 所有结果都非空；
- 所有结果都是同一个 `ProcessorSlotChain` 实例；
- `CtSph.entrySize()` 最终等于 1。

该用例保护“同资源全局共享同一条 slot chain”的契约。

### 容量边界并发创建

先将缓存填充到 `MAX_SLOT_CHAIN_SIZE - 1`，然后让多个线程同时用不同资源调用 `lookProcessChain`。验证：

- 只有一个新资源能够获得非空 chain；
- 其余资源返回 `null`；
- `CtSph.entrySize()` 最终严格等于 `MAX_SLOT_CHAIN_SIZE`，不能越界。

该用例保护容量检查与插入必须在同一临界区完成的契约，同时避免实际并发创建 6000 条完整 slot chain，缩短测试时间。

## 并发测试稳定性

- 使用固定线程池，避免无界创建线程；
- 使用起跑锁存器扩大并发重叠窗口；
- 所有 `Future#get` 都设置超时，失败时不会永久挂住构建；
- 在 `finally` 中关闭线程池；
- 继续依赖现有 `setUp` 和 `tearDown` 清理静态缓存，保证用例隔离。

## 验证方式

1. 先运行并发缓存类型契约测试。
2. 临时将缓存退回普通 `HashMap`，确认该测试能够拒绝非并发 Map；随后恢复 `ConcurrentHashMap`。
3. 运行完整 `CtSphTest`。
4. 运行 `sentinel-core` 测试和 Java 8 目标级别编译检查。
