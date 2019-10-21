# JVM-SANDBOX

## DEVELOPER-GUIDE

### A clock, a bell that can be reported, a damaged bell

We define an abstract class clock, expect to be able to achieve a certain time to report time.

```java
/**
 * Clock time
 */
public abstract class Clock {

    /**
     * Status check
     */
    abstract void checkState();

    // Date formatting
    private final java.text.SimpleDateFormat clockDateFormat
            = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Format the date object as a string
     *
     * @param date Date object
     * @return Date formatted output
     */
    final String formatDate(java.util.Date date) {
        return clockDateFormat.format(date);
    }

    /**
     * Get the current time
     *
     * @return current time
     */
    final java.util.Date nowDate() {
        return new java.util.Date();
    }

    /**
     * report time
     *
     * @return report time
     */
    final String report() {
        checkState();
        return formatDate(nowDate());
    }

    /**
     * Delay a certain time
     *
     * @throws InterruptedException Interrupted
     */
    abstract void delay() throws InterruptedException;

    /**
     * Cycle time
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

This clock has two implementation classes，

- One is normal to achieve

  ```java
    /**
     * One is normal to achieve
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

  Run up to once every second
  
  ```
  2017-02-27 14:48:58
  2017-02-27 14:48:59
  2017-02-27 14:49:00
  2017-02-27 14:49:01
  2017-02-27 14:49:02
  ```

- One is the damaged bell to achieve

  ```java
    /**
     * A damaged bell is realized
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
  
  Run every 10 seconds after the time when the error will be reported

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
  
### Repair damaged bells

Obviously, the normal working clock is what we want to achieve, but now we are running on hand is just a damaged clock. Next, we fix this damaged clock to describe how to build a sandbox module to repair the damaged clock and explain how the module is working.

#### 问题定位

The problem lies in two parts of `BrokenClock` 's

1. The `checkState ()` method throws an exception in the implementation of the method
2. `Delay ()` method in the delay of 10 seconds, it seems that when writing code accidentally knocked a 0


#### Create a Java project `clock-tinker`

1. Assuming the use of MAVEN, add the sandbox module to rely on the two libraries

    ```xml
    <!-- 
         The sandbox module API
    -->
    <dependency>
        <groupId>com.alibaba.jvm.sandbox</groupId>
        <artifactId>sandbox-api</artifactId>
        <version>1.0.3</version>
        <scope>provided</scope>
    </dependency>

    <!-- 
         javax.servlet package
    -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>3.0.1</version>
        <scope>provided</scope>
    </dependency>
    ```

#### Write the module code

```java
/**
 * Repair damaged module
 */
@Information(id = "broken-clock-tinker")
public class BrokenClockTinkerModule implements Module {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Http("/repairCheckState")
    public void repairCheckState() {

        moduleEventWatcher.watch(

                // Match to Clock$BrokenClock#checkState ()
                new NameRegexFilter("Clock\\$BrokenClock", "checkState"),

                // Listen to the THROWS event and change the original method to throw an exception for normal return
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        // 立即返回
                        ProcessControlException.throwReturnImmediately(null);
                    }
                },

                // Specifies that the monitored event is an exception thrown
                Event.Type.THROWS
        );

    }

}
```

#### Register according to SPI specification

1. Create`META-INF/service/com.alibaba.jvm.sandbox.api.Module`file
2. Paste the previous module into the contents of the file

     ```
     com.github.ompc.demo.jvm.sandbox.clocktinker.BrokenClockTinkerModule
     ```
     
#### Compile deployment`clock-tinker`Module

1. It is recommended to rely on all the two packages and tripartite package into a JAR file, you need to increase the configuration in the `pom.xml`

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
    
2. Run the command to complete the package

    ```
    mvn clean package
    ```
    
3. Copy the prepared package to the user module directory

    ```
    cp target/clock-tinker-1.0-SNAPSHOT-jar-with-dependencies.jar ~/.sandbox-module/
    ```
    
4. Start sandbox

    ```
    ./sandbox.sh -p 64229 -l
    module-mgr          	ACTIVE  	LOADED  	0    	0    	0.0.0.1        	luanjia@taobao.com
info                	ACTIVE  	LOADED  	0    	0    	0.0.0.1        	luanjia@taobao.com
broken-clock-tinker 	ACTIVE  	LOADED  	0    	0    	UNKNOW_VERSION 	UNKNOW_AUTHOR
    ```
    
    You can see that the `broken-clock-tinker` module has been loaded correctly by the sandbox
    
1. Activate the fix checkState () method

    ```
    ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairCheckState'
    ```
    
    After a while, you will find the original has been throwing unusual clock has begun to refresh the time
    
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

In the `delay ()` method implementation of the `BrokenClock` class, the error is delayed by 10 seconds. It is necessary to return immediately before the method body is executed, thus preventing the execution of the method body from delaying by 10 seconds.

```java
@Http("/repairDelay")
public void repairDelay() {

    moduleEventWatcher.watch(

            // Match to Clock$BrokenClock#checkState()
            new NameRegexFilter("Clock\\$BrokenClock", "delay"),

            // Listen to the THROWS event and change the original method to throw an exception for normal return
            new EventListener() {
                @Override
                public void onEvent(Event event) throws Throwable {

                    // Delay here for 1s
                    Thread.sleep(1000L);

                    // And then immediately return, because the BEFORE event is listening, so then return immediately, the method body will not be executed
                    ProcessControlException.throwReturnImmediately(null);
                }
            },

            // Specifies that the monitored event is before the method is executed
            Event.Type.BEFORE

    );

}
```

Continue to pack, deploy, and after this replacement module, we perform module hot deployment replacement

```
./sandbox.sh -p 64229 -f
module flush finished, total=3;
```

Module refresh when the first will freeze the original module, and clean up the original plug the code to delete. So after the module refresh is complete, we see the previously repaired `checkState ()` method has attacked.

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

It does not matter, we continue to complete the repair work

- Execute the repair command

  ```
  # Fix the checkState () method
  ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairCheckState'

  # Fix the delay () method
  ./sandbox.sh -p 64229 -d 'broken-clock-tinker/repairDelay'
  ```
  
- Final problem fix

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

### Summary

In this tutorial, we show you how to use the sandbox module to change the execution of the original method, which involves the core of the sandbox `ModuleEventWatcher`, the implementation of this class can be injected through the` @ Resource` comment.

- Through the process of changing the `BEFORE` event, you can circumvent the execution of the original method body, thus bypassing the embarrassing thing of the delay of the` delay () `method.

- By changing the process in the `THROWS` event session, the` checkState () `method should be thrown to throw the return value.

You can even spy on, tamper with parameters, return value, throw the exception, etc., which will be able to achieve through the sandbox module. Sandbox module can help you achieve a lot of interesting features, look forward to your imagination.