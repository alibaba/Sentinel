# JVM-SANDBOX

## DEVELOPER-GUIDE

### 一个钟，一个能报时的钟，一个损坏了的钟

我们定义了一个抽象类的钟，期望可以实现每隔一定的时间进行报时。

```java
/**
 * 报时的钟
 */
public abstract class Clock {

    /**
     * 状态检查
     */
    abstract void checkState();

    // 日期格式化
    private final java.text.SimpleDateFormat clockDateFormat
            = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化日期对象为字符串
     *
     * @param date 日期对象
     * @return 日期格式化输出
     */
    final String formatDate(java.util.Date date) {
        return clockDateFormat.format(date);
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    final java.util.Date nowDate() {
        return new java.util.Date();
    }

    /**
     * 报告时间
     *
     * @return 报告时间
     */
    final String report() {
        checkState();
        return formatDate(nowDate());
    }

    /**
     * 延时一定的时间
     *
     * @throws InterruptedException 中断
     */
    abstract void delay() throws InterruptedException;

    /**
     * 循环播报时间
     */
    final void loopReport() throws InterruptedException {
        while (true) {
            try {
                System.out.println(report());
            } catch (Throwable cause) {
                cause.printStackTrace();
            }
            delay();
        }
    }

}
```

这个钟有两个实现类，

- 一个是正常的实现

  ```java
    /**
     * 一个正常的钟实现
     */
    static class NormalClock extends Clock {

        @Override
        void checkState() {
            return;
        }

        @Override
        void delay() throws InterruptedException {
            Thread.sleep(1000L);
        }

    }
```

  运行起来能每隔一秒进行一次报时
  
  ```
  2017-02-27 14:48:58
  2017-02-27 14:48:59
  2017-02-27 14:49:00
  2017-02-27 14:49:01
  2017-02-27 14:49:02
  ```

- 一个是损坏的钟实现

  ```java
    /**
     * 一个损坏的钟实现
     */
    static class BrokenClock extends Clock {

        @Override
        void checkState() {
            throw new IllegalStateException();
        }

        @Override
        void delay() throws InterruptedException {
            Thread.sleep(10000L);
        }

    }
  ```
  
  运行起来后每隔十秒报时的时候就会报错

  ```
java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:77)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:50)
        at Clock.main(Clock.java:94)
java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:77)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:50)
        at Clock.main(Clock.java:94)
java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:77)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:50)
        at Clock.main(Clock.java:94)
java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:77)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:50)
        at Clock.main(Clock.java:94)
  ```
  
### 修复损坏的钟

很明显，正常工作的钟才是我们希望的实现，但目前我们手头上运行的恰恰是一个损坏的钟。接下来我们通过修复这个损坏的钟来描述如何通过构建一个沙箱模块来修复这个损坏的钟，并介绍模块是如何进行工作的。

#### 问题定位

问题出在了`BrokenClock`的两个地方

1. `checkState()`方法的实现中抛出了一个异常
2. `delay()`方法中延时了10秒，似乎编写代码的时候不小心多敲了一个0


#### 创建一个Java工程`clock-tinker`

1. 假设用的是MAVEN，添加沙箱模块二方库的依赖

    ```xml
    <!-- 
         沙箱模块的API定义二方包
         这个二方包可以被声明为provided
    -->
    <dependency>
        <groupId>com.alibaba.jvm.sandbox</groupId>
        <artifactId>sandbox-api</artifactId>
        <version>1.0.3</version>
        <scope>provided</scope>
    </dependency>

    <!-- 
         javax.servlet的三方包
         在沙箱模块中需要用到HttpServletReuqest和HttpServletResponse
         整个沙箱模块被放置在Servlet容器中完成加载
    -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>3.0.1</version>
        <scope>provided</scope>
    </dependency>
    ```

#### 编写模块代码

```java
/**
 * 修复损坏的钟模块
 */
@Information(id = "broken-clock-tinker")
public class BrokenClockTinkerModule implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Http("/repairCheckState")
    public void repairCheckState() {

        moduleEventWatcher.watch(

                // 匹配到Clock$BrokenClock#checkState()
                new NameRegexFilter("Clock\\$BrokenClock", "checkState"),

                // 监听THROWS事件并且改变原有方法抛出异常为正常返回
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        // 立即返回
                        ProcessControlException.throwReturnImmediately(null);
                    }
                },

                // 指定监听的事件为抛出异常
                Event.Type.THROWS
        );

    }

}
```

#### 根据SPI规范注册

1. 创建`META-INF/services/com.alibaba.jvm.sandbox.api.Module`文件
2. 往文件内容中将之前的模块注入进来

     ```
     com.github.ompc.demo.jvm.sandbox.clocktinker.BrokenClockTinkerModule
     ```
     
#### 编译部署`clock-tinker`模块

1. 推荐将所有依赖的二方包和三方包都打入到一个JAR文件中，需要在`pom.xml`中增加配置

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>attached</goal>
                        </goals>
                        <phase>package</phase>
                        <configuration>
                            <descriptorRefs>
                                <descriptorRef>jar-with-dependencies</descriptorRef>
                            </descriptorRefs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    ```
    
1. 运行命令完成打包

    ```
    mvn clean package
    ```
    
1. 将打好的包复制到用户模块目录下

    ```
    cp target/clock-tinker-1.0-SNAPSHOT-jar-with-dependencies.jar ~/.sandbox-module/
    ```
    
1. 启动沙箱

    ```
    ./sandbox.sh -p 64229 -l
    module-mgr          	ACTIVE  	LOADED  	0    	0    	0.0.0.1        	luanjia@taobao.com
info                	ACTIVE  	LOADED  	0    	0    	0.0.0.1        	luanjia@taobao.com
broken-clock-tinker 	ACTIVE  	LOADED  	0    	0    	UNKNOW_VERSION 	UNKNOW_AUTHOR
    ```
    
    可以看到`broken-clock-tinker`模块已经正确被沙箱所加载
    
1. 激活修复checkState()方法

    ```
    ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairCheckState'
    ```
    
    过一会，你就会发现原本一直抛异常的钟已经开始在刷新时间了
    
    ```
    java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:89)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:57)
        at Clock.main(Clock.java:111)
    java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:89)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:57)
        at Clock.main(Clock.java:111)
    2017-02-27 21:34:44
    2017-02-27 21:34:54
    2017-02-27 21:35:04
    2017-02-27 21:35:14
    2017-02-27 21:35:24
    2017-02-27 21:35:34
    2017-02-27 21:35:44
    2017-02-27 21:35:54
    2017-02-27 21:36:04
    2017-02-27 21:36:14
    ```
    
#### 修复错误的delay()方法

在`BrokenClock`类的`delay()`方法实现中，错误的延时了10秒，这里需要在方法体执行之前就得立即进行返回，从而可以避免方法体的执行导致延时10秒。

```java
@Http("/repairDelay")
public void repairDelay() {

    moduleEventWatcher.watch(

            // 匹配到Clock$BrokenClock#checkState()
            new NameRegexFilter("Clock\\$BrokenClock", "delay"),

            // 监听THROWS事件并且改变原有方法抛出异常为正常返回
            new EventListener() {
                @Override
                public void onEvent(Event event) throws Throwable {

                    // 在这里延时1s
                    Thread.sleep(1000L);

                    // 然后立即返回，因为监听的是BEFORE事件，所以此时立即返回，方法体将不会被执行
                    ProcessControlException.throwReturnImmediately(null);
                }
            },

            // 指定监听的事件为方法执行前
            Event.Type.BEFORE

    );

}
```

继续打包、部署，这次替换模块之后，我们执行模块热部署替换

```
./sandbox.sh -p 64229 -f
module flush finished, total=3;
```

模块刷新的时候首先会冻结原有模块，并清理删除原有的插桩代码。所以在模块刷新完成之后我们看到之前被修好的`checkState()`方法又发作了

```
2017-02-27 21:45:54
2017-02-27 21:46:04
2017-02-27 21:46:14
2017-02-27 21:46:24
2017-02-27 21:46:34
java.lang.IllegalStateException
    at Clock$BrokenClock.checkState(Clock.java:89)
    at Clock.report(Clock.java:40)
    at Clock.loopReport(Clock.java:57)
    at Clock.main(Clock.java:111)
java.lang.IllegalStateException
    at Clock$BrokenClock.checkState(Clock.java:89)
    at Clock.report(Clock.java:40)
    at Clock.loopReport(Clock.java:57)
    at Clock.main(Clock.java:111)
```

没关系，我们继续完成修复工作

- 执行修复命令

  ```
  # 修复checkState()方法
  ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairCheckState'

  # 修复delay()方法
  ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairDelay'
  ```
  
- 最终问题修复

  ```
  java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:89)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:57)
        at Clock.main(Clock.java:111)
  java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:89)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:57)
        at Clock.main(Clock.java:111)
  java.lang.IllegalStateException
        at Clock$BrokenClock.checkState(Clock.java:89)
        at Clock.report(Clock.java:40)
        at Clock.loopReport(Clock.java:57)
        at Clock.main(Clock.java:111)
  2017-02-27 21:51:24
  2017-02-27 21:51:34
  2017-02-27 21:51:35
  2017-02-27 21:51:36
  2017-02-27 21:51:37
  2017-02-27 21:51:38
  2017-02-27 21:51:39
  ```  

### 小结

在这个教程中给大家演示了如何利用沙箱的模块改变了原有方法的执行流程，这里涉及到了沙箱最核心的类`ModuleEventWatcher`，这个类的实现可以通过`@Resource`注释注入进来。

- 通过在`BEFORE`事件环节的改变流程，可以规避掉原有方法体的执行，从而绕开了`delay()`方法实现的延时10秒这一尴尬的事情。

- 通过在`THROWS`事件环节的改变流程，可以让原本应该抛出异常的`checkState()`方法转变为抛出返回值。

你甚至可以窥探、篡改入参、返回值、抛出的异常等等，这些都将可以通过沙箱模块来实现。沙箱模块还能帮你实现很多有意思的功能，期待你的想象。
