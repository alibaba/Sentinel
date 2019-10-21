# Sentinel Dubbo Demo

Sentinel 提供了与 Dubbo 整合的模块 - Sentinel Dubbo Adapter，主要包括针对 Service Provider 和 Service Consumer 实现的 Filter。使用时用户只需引入以下模块（以 Maven 为例）：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-dubbo-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

引入此依赖后，Dubbo 的服务接口和方法（包括调用端和服务端）就会成为 Sentinel 中的资源，在配置了规则后就可以自动享受到 Sentinel 的防护能力。

> **注：若希望接入 Dashboard，请参考后面接入控制台的步骤。只引入 Sentinel Dubbo Adapter 无法接入控制台！**

若不希望开启 Sentinel Dubbo Adapter 中的某个 Filter，可以手动关闭对应的 Filter，比如：

```java
@Bean
public ConsumerConfig consumerConfig() {
    ConsumerConfig consumerConfig = new ConsumerConfig();
    consumerConfig.setFilter("-sentinel.dubbo.consumer.filter");
    return consumerConfig;
}
```

我们提供了几个具体的 Demo 来分别演示 Provider 和 Consumer 的限流场景。

## Service Provider

Service Provider 用于向外界提供服务，处理各个消费者的调用请求。为了保护 Provider 不被激增的流量拖垮影响稳定性，可以给 Provider 配置 **QPS 模式**的限流，这样当每秒的请求量超过设定的阈值时会自动拒绝多的请求。限流粒度可以是服务接口和服务方法两种粒度。若希望整个服务接口的 QPS 不超过一定数值，则可以为对应服务接口资源（resourceName 为**接口全限定名**）配置 QPS 阈值；若希望服务的某个方法的 QPS 不超过一定数值，则可以为对应服务方法资源（resourceName 为**接口全限定名:方法签名**）配置 QPS 阈值。有关配置详情请参考 [流量控制 | Sentinel](https://github.com/alibaba/Sentinel/wiki/%E6%B5%81%E9%87%8F%E6%8E%A7%E5%88%B6)。

Demo 1 演示了此限流场景，我们看一下这种模式的限流产生的效果。假设我们已经定义了某个服务接口 `com.alibaba.csp.sentinel.demo.dubbo.FooService`，其中有一个方法 `sayHello(java.lang.String)`，Provider 端该方法设定 QPS 阈值为 10。在 Consumer 端在 1s 之内连续发起 15 次调用，可以通过日志文件看到 Provider 端被限流。拦截日志统一记录在 `~/logs/csp/sentinel-block.log` 中：

```
2018-07-24 17:13:43|1|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String),FlowException,default,|5,0
```

在 Provider 对应的 metrics 日志中也有记录：

```
1532423623000|2018-07-24 17:13:43|com.alibaba.csp.sentinel.demo.dubbo.FooService|15|0|15|0|3
1532423623000|2018-07-24 17:13:43|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String)|10|5|10|0|0
```

很多场景下，根据**调用方**来限流也是非常重要的。比如有两个服务 A 和 B 都向 Service Provider 发起调用请求，我们希望只对来自服务 B 的请求进行限流，则可以设置限流规则的 `limitApp` 为服务 B 的名称。Sentinel Dubbo Adapter 会自动解析 Dubbo 消费者（调用方）的 application name 作为调用方名称（`origin`），在进行资源保护的时候都会带上调用方名称。若限流规则未配置调用方（`default`），则该限流规则对所有调用方生效。若限流规则配置了调用方则限流规则将仅对指定调用方生效。

> 注：Dubbo 默认通信不携带对端 application name 信息，因此需要开发者在调用端手动将 application name 置入 attachment 中，provider 端进行相应的解析。Sentinel Dubbo Adapter 实现了一个 Filter 用于自动从 consumer 端向 provider 端透传 application name。若调用端未引入 Sentinel Dubbo Adapter，又希望根据调用端限流，可以在调用端手动将 application name 置入 attachment 中，key 为 `dubboApplication`。

在限流日志中会也会记录调用方的名称，如：

```
2018-07-25 16:26:48|1|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String),FlowException,default,demo-consumer|5,0
```

其中日志中的 `demo-consumer` 即为调用方名称。

## Service Consumer

> 对服务消费方的流量控制可分为**控制并发线程数**和**服务降级**两个维度。

### 并发线程数限流

Service Consumer 作为客户端去调用远程服务。每一个服务都可能会依赖几个下游服务，若某个服务 A 依赖的下游服务 B 出现了不稳定的情况，服务 A 请求服务 B 的响应时间变长，从而服务 A 调用服务 B 的线程就会产生堆积，最终可能耗尽服务 A 的线程数。我们通过用并发线程数来控制对下游服务 B 的访问，来保证下游服务不可靠的时候，不会拖垮服务自身。基于这种场景，推荐给 Consumer 配置**线程数模式**的限流，来保证自身不被不稳定服务所影响。限流粒度同样可以是服务接口和服务方法两种粒度。

采用基于线程数的限流模式后，我们不需要再显式地去进行线程池隔离，Sentinel 会控制资源的线程数，超出的请求直接拒绝，直到堆积的线程处理完成。

Demo 2 演示了此限流场景，我们看一下这种模式的效果。假设当前服务 A 依赖两个远程服务方法 `sayHello(java.lang.String)` 和 `doAnother()`。前者远程调用的响应时间 为 1s-1.5s之间，后者 RT 非常小（30 ms 左右）。服务 A 端设两个远程方法 thread count 为 5。然后每隔 50 ms 左右向线程池投入两个任务，作为消费者分别远程调用对应方法，持续 10 次。可以看到 `sayHello` 方法被限流 5 次，因为后面调用的时候前面的远程调用还未返回（RT 高）；而 `doAnother()` 调用则不受影响。线程数目超出时快速失败能够有效地防止自己被慢调用所影响。

### 服务降级

当服务依赖于多个下游服务，而某个下游服务调用非常慢时，会严重影响当前服务的调用。这里我们可以利用 Sentinel 熔断降级的功能，为调用端配置基于平均 RT 的[降级规则](https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7)。这样当调用链路中某个服务调用的平均 RT 升高，在一定的次数内超过配置的 RT 阈值，Sentinel 就会对此调用资源进行降级操作，接下来的调用都会立刻拒绝，直到过了一段设定的时间后才恢复，从而保护服务不被调用端短板所影响。同时可以配合 fallback 功能使用，在被降级的时候提供相应的处理逻辑。

## Fallback

从 0.1.1 版本开始，Sentinel Dubbo Adapter 还支持配置全局的 fallback 函数，可以在 Dubbo 服务被限流/降级/负载保护的时候进行相应的 fallback 处理。用户只需要实现自定义的 [`DubboFallback`](https://github.com/alibaba/Sentinel/blob/master/sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/fallback/DubboFallback.java) 接口，并通过 `DubboFallbackRegistry` 注册即可。默认情况会直接将 `BlockException` 包装后抛出。同时，我们还可以配合 [Dubbo 的 fallback 机制](http://dubbo.apache.org/#!/docs/user/demos/local-mock.md?lang=zh-cn) 来为降级的服务提供替代的实现。

Demo 2 的 Consumer 端提供了一个简单的 fallback 示例。

## Sentinel Dashboard

Sentinel 还提供 API 用于获取实时的监控信息，对应文档见[此处](https://github.com/alibaba/Sentinel/wiki/%E5%AE%9E%E6%97%B6%E7%9B%91%E6%8E%A7)。为了便于使用，Sentinel 还提供了一个控制台（Dashboard）用于配置规则、查看监控、机器发现等功能。

接入 Dashboard 的步骤（**缺一不可**）：

1. 按照 [Sentinel 控制台文档](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0) 启动控制台
2. 应用引入 `sentinel-transport-simple-http` 依赖，以便控制台可以拉取对应应用的相关信息
3. 给应用添加相关的启动参数，启动应用。需要配置的参数有：
   - `-Dcsp.sentinel.api.port`：客户端的 port，用于上报相关信息
   - `-Dcsp.sentinel.dashboard.server`：控制台的地址
   - `-Dproject.name`：应用名称，会在控制台中显示

注意某些环境下本地运行 Dubbo 服务还需要加上 `-Djava.net.preferIPv4Stack=true` 参数。比如 Service Provider 示例的启动参数：

```bash
-Djava.net.preferIPv4Stack=true -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=dubbo-provider-demo
```

Service Consumer 示例的启动参数：

```bash
-Djava.net.preferIPv4Stack=true -Dcsp.sentinel.api.port=8721 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=dubbo-consumer-demo
```

这样在启动 Service Provider 和 Service Consumer 示例以后，就可以在 Sentinel 控制台中找到我们的服务了。可以很方便地在控制台中配置限流规则：

![规则配置](http://dubbo.incubator.apache.org/img/blog/sentinel-dashboard-view-rules.png)

或者查看实时监控数据：

![秒级实时监控](http://dubbo.incubator.apache.org/img/blog/sentinel-dashboard-metrics.png)
