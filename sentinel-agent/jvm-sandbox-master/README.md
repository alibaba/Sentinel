# ![BANNER](https://github.com/alibaba/jvm-sandbox/wiki/img/BANNER.png)

![license](https://img.shields.io/github/license/alibaba/arthas.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/jvm-sandbox.svg)](http://isitmaintained.com/project/alibaba/jvm-sandbox "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/jvm-sandbox.svg)](http://isitmaintained.com/project/alibaba/jvm-sandbox "Percentage of issues still open")

> JVM沙箱容器，一种JVM的非侵入式运行期AOP解决方案<br/>
> Real - time non-invasive AOP framework container based on JVM

## 目标群体

- [BTRACE](https://github.com/btraceio/btrace)好强大，也曾技痒想做一个更便捷、更适合自己的问题定位工具，既可支持线上链路监控排查，也可支持单机版问题定位。
- 有时候突然一个问题反馈上来，需要入参才能完成定位，但恰恰没有任何日志，甚至出现在别人的代码里，好想开发一个工具可以根据需要动态添加日志，最好还能按照业务ID进行过滤。
- 系统间的异常模拟可以使用的工具很多，可是系统内的异常模拟怎么办，加开关或是用AOP在开发系统中实现，好想开发一个更优雅的异常模拟工具，既能模拟系统间的异常，又能模拟系统内的异常。
- 好想获取行调用链路数据，可以用它识别场景、覆盖率统计等等，覆盖率统计工具不能原生支持，统计链路数据不准确。想自己开发一个工具获取行链路数据。
- 我想开发录制回放、故障模拟、动态日志、行链路获取等等工具，就算我开发完成了，这些工具底层实现原理相同，同时使用，要怎么消除这些工具之间的影响，怎么保证这些工具动态加载，怎么保证动态加载/卸载之后不会影响其他工具，怎么保证在工具有问题的时候，快速消除影响，代码还原

如果你有以上研发诉求，那么你就是JVM-SANDBOX(以下简称沙箱容器)的潜在客户。沙箱容器提供

1. 动态增强类你所指定的类，获取你想要的参数和行信息甚至改变方法执行
1. 动态可插拔容器框架

## 项目简介

**JVM-SANDBOX（沙箱）实现了一种在不重启、不侵入目标JVM应用的AOP解决方案。**

### 沙箱的特性

1. `无侵入`：目标应用无需重启也无需感知沙箱的存在
1. `类隔离`：沙箱以及沙箱的模块不会和目标应用的类相互干扰
1. `可插拔`：沙箱以及沙箱的模块可以随时加载和卸载，不会在目标应用留下痕迹
1. `多租户`：目标应用可以同时挂载不同租户下的沙箱并独立控制
1. `高兼容`：支持JDK[6,11]

### 沙箱常见应用场景

- 线上故障定位
- 线上系统流控
- 线上故障模拟
- 方法请求录制和结果回放
- 动态日志打印
- 安全信息监测和脱敏

*JVM-SANDBOX还能帮助你做很多很多，取决于你的脑洞有多大了。*

### 实时无侵入AOP框架

在常见的AOP框架实现方案中，有静态编织和动态编织两种。

1. **静态编织**：静态编织发生在字节码生成时根据一定框架的规则提前将AOP字节码插入到目标类和方法中，实现AOP；
1. **动态编织**：动态编织则允许在JVM运行过程中完成指定方法的AOP字节码增强.常见的动态编织方案大多采用重命名原有方法，再新建一个同签名的方法来做代理的工作模式来完成AOP的功能(常见的实现方案如CgLib)，但这种方式存在一些应用边界：
   - **侵入性**：对被代理的目标类需要进行侵入式改造。比如：在Spring中必须是托管于Spring容器中的Bean
   - **固化性**：目标代理方法在启动之后即固化，无法重新对一个已有方法进行AOP增强
 
要解决`无侵入`的特性需要AOP框架具备 **在运行时完成目标方法的增强和替换**。在JDK的规范中运行期重定义一个类必须准循以下原则
  1. 不允许新增、修改和删除成员变量
  1. 不允许新增和删除方法
  1. 不允许修改方法签名

JVM-SANDBOX属于基于Instrumentation的动态编织类的AOP框架，**通过精心构造了字节码增强逻辑，使得沙箱的模块能在不违反JDK约束情况下实现对目标应用方法的`无侵入`运行时AOP拦截**。

## 核心原理

### 事件驱动

在沙箱的世界观中，任何一个Java方法的调用都可以分解为`BEFORE`、`RETURN`和`THROWS`三个环节，由此在三个环节上引申出对应环节的事件探测和流程控制机制。

```java
// BEFORE
try {

   /*
    * do something...
    */

    // RETURN
    return;

} catch (Throwable cause) {
    // THROWS
}
```

基于`BEFORE`、`RETURN`和`THROWS`三个环节事件分离，沙箱的模块可以完成很多类AOP的操作。

1. 可以感知和改变方法调用的入参
1. 可以感知和改变方法调用返回值和抛出的异常
1. 可以改变方法执行的流程
    - 在方法体执行之前直接返回自定义结果对象，原有方法代码将不会被执行
    - 在方法体返回之前重新构造新的结果对象，甚至可以改变为抛出异常
    - 在方法体抛出异常之后重新抛出新的异常，甚至可以改变为正常返回

### 类隔离策略

沙箱通过自定义的SandboxClassLoader破坏了双亲委派的约定，实现了和目标应用的类隔离。所以不用担心加载沙箱会引起应用的类污染、冲突。各模块之间类通过ModuleJarClassLoader实现了各自的独立，达到模块之间、模块和沙箱之间、模块和应用之间互不干扰。

![jvm-sandbox-classloader](https://github.com/alibaba/jvm-sandbox/wiki/img/jvm-sandbox-classloader.png)

### 类增强策略

沙箱通过在BootstrapClassLoader中埋藏的Spy类完成目标类和沙箱内核的通讯

![jvm-sandbox-enhance-class](https://github.com/alibaba/jvm-sandbox/wiki/img/jvm-sandbox-enhance-class.jpg)

### 整体架构

![jvm-sandbox-architecture](https://github.com/alibaba/jvm-sandbox/wiki/img/jvm-sandbox-architecture.png)

## 快速安装

- **下载并安装**

  ```shell
  # 下载最新版本的JVM-SANDBOX
  wget http://ompc.oss-cn-hangzhou.aliyuncs.com/jvm-sandbox/release/sandbox-stable-bin.zip

  # 解压
  unzip sandbox-stable-bin.zip
  ```

- **挂载目标应用**

  ```shell
  # 进入沙箱执行脚本
  cd sandbox/bin

  # 目标JVM进程33342
  ./sandbox.sh -p 33342
  ```

- **挂载成功后会提示**

  ```shell
  ./sandbox.sh -p 33342
             NAMESPACE : default
               VERSION : 1.2.0
                  MODE : ATTACH
           SERVER_ADDR : 0.0.0.0
           SERVER_PORT : 55756
        UNSAFE_SUPPORT : ENABLE
          SANDBOX_HOME : /Users/vlinux/opt/sandbox
     SYSTEM_MODULE_LIB : /Users/vlinux/opt/sandbox/module
       USER_MODULE_LIB : ~/.sandbox-module;
   SYSTEM_PROVIDER_LIB : /Users/vlinux/opt/sandbox/provider
    EVENT_POOL_SUPPORT : DISABLE
  ```

- **卸载沙箱**

  ```shell
  ./sandbox.sh -p 33342 -S
  jvm-sandbox[default] shutdown finished.
  ```

## 项目背景

2014年[GREYS](https://github.com/oldmanpushcart/greys-anatomy)第一版正式发布，一路看着他从无到有，并不断优化强大，感慨羡慕之余，也在想GREYS是不是只能做问题定位。

2015年开始根据GREYS的底层代码完成了人生的第一个字节码增强工具——动态日志。之后又萌生了将其拆解成*录制回放*、*故障模拟*等工具的想法。扪心自问，我是想以一人一个团队的力量建立大而全的工具平台，还是做一个底层中台，让每一位技术人员都可以在它的基础上快速的实现业务功能。我选择了后者。

## 相关文档

- **[WIKI](https://github.com/alibaba/jvm-sandbox/wiki/Home)**
