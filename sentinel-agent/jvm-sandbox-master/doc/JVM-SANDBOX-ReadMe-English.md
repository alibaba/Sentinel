# JVM-SANDBOX
>Real - time non-invasive AOP framework container based on JVM


## Foreword

#### What is the core function of the JVM-SANDBOX？

##### Real-time non-invasive AOP framework
In the common AOP framework to achieve the program, there are two kinds of static weaving and dynamic weaving.

###### Static weaving

Static weaving occurs in the bytecode generation according to a certain framework of the rules in advance AOP byte code into the target class and method to achieve AOP.

###### Dynamic weaving

Dynamic weaving allows AOP bytecode enhancement of the specified method to be completed during the execution of the JVM.

Common dynamic weaving programs are mostly used to rename the original method, and then create a new signature method to do the agent's work mode to complete the AOP function (common implementation such as CgLib), but there are some application boundaries.

- Invasive

    The target class of the agent needs to be invaded.

- Curable

    The target agent method is solidified after startup and can not re-validate an existing method
    
###### Hot deployment
There are some ways to implement AOP is done through a similar hot deployment, but there are some application boundaries for existing hot deployment implementations:

- Performance damage huge
- There is an intrusion into the JVM
- Must be started when explicitly opened

Based on this I am through the JDK6 provided Instrumentation-API implementation of the use of HotSwap technology without restarting the JVM in the case of any method to achieve AOP enhancements. And performance overhead is still within acceptable limits


##### Provides a plug-and-play module management container
In order to realize the dynamic hot-swapping of the sandbox module, the container client and the sandbox dynamic pluggable container communicate with the HTTP protocol. The bottom layer uses Jetty8 as the HTTP server.

#### What can the JVM-SANDBOX do？

In the JVM-SANDBOX (hereinafter referred to as the sandbox) world view, any one of the Java method calls can be broken down into `BEFORE`,` RETURN` and `THROWS` three links, which in three links on the corresponding link Event detection and process control mechanisms.

```java
// BEFORE-EVENT
try {

   /*
    * do something...
    */

    // RETURN-EVENT
    return;

} catch (Throwable cause) {
    // THROWS-EVENT
}
```

Based on the `BEFORE`,` RETURN` and `THROWS` three events, you can do a lot of AOP operation.

1. You can perceive and change the method call
2. You can sense and change the method call return value and throw the exception
3. You can change the flow of method execution

    - The custom result object is returned directly before the method body is executed, and the original method code will not be executed
    - Re-construct a new result object before the method body returns, and can even change to throw an exception
    - Throws a new exception after throwing an exception in the method body, and can even change to a normal return


#### What are the possible scenarios for the JVM-SANDBOX?

- Online fault location
- Online system flow control
- Online fault simulation
- Method to request recording and result playback
- Dynamic log printing
- Safety information monitoring and desensitization

The JVM sandbox can also help you do a lot, depending on how big your brain is.