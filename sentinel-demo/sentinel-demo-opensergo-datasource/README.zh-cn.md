# Sentinel OpenSergo 数据源 Demo

本 demo 将以一个普通 Spring Boot 应用的视角来介绍如何结合 [Sentinel OpenSergo 数据源](https://sentinelguard.io/zh-cn/docs/opensergo-data-source.html)
将应用接入 OpenSergo 统一治理控制面，并通过 [OpenSergo 流量防护与容错 CRD](https://github.com/opensergo/opensergo-specification/blob/main/specification/zh-Hans/fault-tolerance.md) 进行统一流量防护管控。

**注意**：在应用启动前，确保 OpenSergo 控制面及 CRD 已经部署在 Kubernetes 集群中，
可以参考[控制面快速部署文档](https://opensergo.io/zh-cn/docs/quick-start/opensergo-control-plane/)。
若您需要在本地启动 demo 应用，则需要将 OpenSergo 控制面的 Service 类型调整为 `LoadBalancer` 或 `NodePort` 对外暴露 endpoint。

1. 在 demo 中引入 `sentinel-datasource-opensergo` 数据源模块：

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-opensergo</artifactId>
    <!-- 在此替换成最新版本 -->
    <version>0.1.0-beta</version>
</dependency>
```

2. 在项目合适的位置（如 Spring 初始化 hook 或 Sentinel `InitFunc` 中）中创建并注册 Sentinel OpenSergo 数据源。在本 demo 中，我们通过 Spring `@PostConstruct` 来注册数据源：

```java
@Configuration
public class DataSourceConfig {

    @PostConstruct
    public void init() throws Exception {
        // 通过 -Dproject.name 指定应用名称，或者直接在代码中替换应用名
        OpenSergoDataSourceGroup openSergo = new OpenSergoDataSourceGroup("localhost", 10246, "default", AppNameUtil.getAppName());
        openSergo.start();
        // 注册流控规则数据源
        FlowRuleManager.register2Property(openSergo.subscribeFlowRules());
    }
}
```

3. 启动 Spring Boot 应用，通过 `-Dproject.name` 指定应用名称，如 `-Dproject.name=foo-app`。在应用启动前，[确保 OpenSergo 控制面及 CRD 已经部署在 Kubernetes 集群中](https://opensergo.io/zh-cn/docs/quick-start/opensergo-control-plane/)。
4. 在 Kubernetes 集群中配置 OpenSergo 流量防护规则，通过 kubectl apply 到集群中。[规则示例](src/main/resources/opensergo_cr_sample.yml)：

```yaml
apiVersion: fault-tolerance.opensergo.io/v1alpha1
kind: RateLimitStrategy
metadata:
  name: rate-limit-foo
  labels:
    # 应用名需要与 demo 启动时指定的应用名称一致
    app: foo-app
spec:
  metricType: RequestAmount
  limitMode: Local
  threshold: 2
  statDurationSeconds: 1
---
apiVersion: fault-tolerance.opensergo.io/v1alpha1
kind: FaultToleranceRule
metadata:
  name: my-opensergo-rule-1
  labels:
    # 应用名需要与 demo 启动时指定的应用名称一致
    app: foo-app
spec:
  targets:
    - targetResourceName: 'GET:/foo/{id}'
  strategies:
    - name: rate-limit-foo
      kind: RateLimitStrategy
```

我们的 Spring Boot demo 中提供一系列的 HTTP 接口，其中包含 `GET /foo/{id}` 这个接口。上面的规则为该接口配置了单机 QPS 阈值为 2 的
单机流控规则，每秒钟调用该接口的次数最多为两次。在 `FaultToleranceRule` target 中，我们将 `targetResourceName` 指定为
Sentinel 里面的资源名 (resourceName)。

5. 持续访问 `/foo/{id}` 接口，可以观察到每秒钟前两次请求可以正常返回，这一秒后续的请求会返回默认的流控状态码 429。

后续 Sentinel 还会支持 OpenSergo spec 中的 `FallbackAction`，可以通过 CRD 方式自定义防护触发后的返回。

