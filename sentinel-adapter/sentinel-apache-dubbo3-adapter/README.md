# Sentinel Apache Dubbo Adapter (for 3.0.5+)

> Note: 中文文档请见[此处](https://sentinelguard.io/zh-cn/docs/open-source-framework-integrations.html)。

Sentinel Dubbo Adapter provides service consumer filter and provider filter
for [Apache Dubbo](https://dubbo.apache.org/en/) services.

**Note: This adapter only supports Apache Dubbo 3.0.5 and above.**
For `org.apache:dubbo` 2.7.x, please use `sentinel-apache-dubbo-adapter` module instead.
For legacy `com.alibaba:dubbo` 2.6.x, please use `sentinel-dubbo-adapter` module instead.

To use Sentinel Dubbo 3.x Adapter, you can simply add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-apache-dubbo3-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

The Sentinel filters are **enabled by default**. Once you add the dependency,
the Dubbo services and methods will become protected resources in Sentinel,
which can leverage Sentinel's flow control and overload protection ability when rules are configured.
Demos can be found in [sentinel-demo-apache-dubbo](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-apache-dubbo).

If you don't want the filters enabled, you can manually disable them. For example:

```xml
<dubbo:consumer filter="-sentinel.dubbo.consumer.filter"/>

<dubbo:provider filter="-sentinel.dubbo.provider.filter"/>
```

For more details of Dubbo filter, see [here](https://dubbo.apache.org/zh/docs3-v2/java-sdk/reference-manual/spi/description/filter/).

## Dubbo resources

The resource for Dubbo services has two granularities: service interface and service method.

- Service interface: resourceName format is `interfaceName`, e.g. `com.alibaba.csp.sentinel.demo.dubbo.FooService`
- Service method: resourceName format is `interfaceName:methodSignature`, e.g. `com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String)`

> **Note**: Dubbo *group+version+interface* level is also supported in Sentinel Apache Dubbo adapter.
> You may just add `-Dcsp.sentinel.dubbo.interface.group.version.enabled=true` JVM property,
> then the resource name of the Dubbo interface and method will be prefixed with group and version info.

## Flow control based on caller

In many circumstances, it's also significant to control traffic flow based on the **caller**.
For example, assuming that there are two services A and B, both of them initiate remote call requests to the service provider.
If we want to limit the calls from service B only, we can set the `limitApp` of flow rule as the identifier of service B (e.g. service name).

Sentinel Dubbo Adapter will automatically resolve the Dubbo consumer's *application name* as the caller's name (`origin`),
and will bring the caller's name when doing resource protection.
If `limitApp` of flow rules is not configured (`default`), flow control will take effects on all callers.
If `limitApp` of a flow rule is configured with a caller, then the corresponding flow rule will only take effect on the specific caller.

> Note: The adapter provides support for customizing origin parsing logic as well.
> You may register your own `DubboOriginParser` implementation to `DubboAdapterGlobalConfig`.

## Global fallback

Sentinel Dubbo Adapter supports global fallback configuration.
The global fallback will handle exceptions and give replacement result when blocked by
flow control, degrade or system load protection. You can implement your own `DubboFallback` interface
and then register to `DubboAdapterGlobalConfig`.

If no fallback is configured, Sentinel will wrap the `BlockException` with a `RuntimeException` as the fallback result.

Besides, we can also leverage [Dubbo mock mechanism](https://dubbo.apache.org/zh/docs3-v2/java-sdk/advanced-features-and-usage/service/service-downgrade/) to provide fallback implementation of degraded Dubbo services.