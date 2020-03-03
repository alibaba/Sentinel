# Sentinel SOFARPC Demo

Sentinel 提供了与 SOFARPC 整合的模块 - `sentinel-sofa-rpc-adapter`，主要包括针对 Service Provider 和 Service Consumer 实现的 Filter。使用时用户只需引入以下模块（以 Maven 为例）：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-sofa-rpc-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

引入此依赖后，SOFARPC 的服务接口和方法（包括调用端和服务端）就会成为 Sentinel 中的资源，在配置了规则后就可以自动享受到 Sentinel 的防护能力。

> **注：若希望接入 Dashboard，请参考 demo 中的注释添加启动参数。只引入 `sentinel-sofa-rpc-adapter` 依赖无法接入控制台！**

若不希望开启 Sentinel SOFARPC Adapter 中的某个 Filter，可以手动关闭对应的 Filter，比如：

```java
providerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");
consumerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");
```

或者在 `rpc-config.json` 文件中设置，它的优先级要低一些。

```json
{
  "sofa.rpc.sentinel.enabled": true
}
```

## 运行 Demo

1. 启动控制台，运行 `DashboardApplication`

2. 启动 Provider，运行 `DemoProvider`（VM参数：`-Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080`）

3. 启动 Consumer，运行 `DemoConsumer`（VM参数：`-Dproject.name=DemoConsumer -Dcsp.sentinel.dashboard.server=localhost:8080`）

通过控制台实时监控、簇点链路菜单观察接口调用、资源情况；对资源设置不同流控规则，进行观察和调试。

参考：[Sentinel 控制台文档](https://github.com/alibaba/Sentinel/wiki/控制台).