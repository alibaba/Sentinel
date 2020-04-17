# Sentinel SOFARPC Adapter

Sentinel SOFARPC Adapter provides service provider filter and consumer filter
for [SOFARPC](https://www.sofastack.tech/projects/sofa-rpc) services.

**Note: This adapter supports SOFARPC 5.4.x version and above, and 5.6.x is officially recommended.**

To use Sentinel SOFARPC Adapter, you can simply add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-sofa-rpc-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

The Sentinel filters are **enabled by default**. Once you add the dependency,
the SOFARPC services and methods will become protected resources in Sentinel,
which can leverage Sentinel's flow control and guard ability when rules are configured.
Demos can be found in [sentinel-demo-sofa-rpc](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-sofa-rpc).

If you don't want the filters enabled, you can manually disable them. For example:

```java
providerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");
consumerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");
```

or add setting in `rpc-config.json` file, and its priority is lower than above.

```json
{
  "sofa.rpc.sentinel.enabled": true
}
```

For more details of SOFARPC filter, see [here](https://www.sofastack.tech/projects/sofa-rpc/custom-filter/).

## SOFARPC resources

The resource for SOFARPC services has two granularities: service interface and service method.

- Service interface：resourceName format is `interfaceName`，e.g. `com.alibaba.csp.sentinel.demo.sofa.rpc.DemoService`
- Service method：resourceName format is `interfaceName#methodSignature`，e.g. `com.alibaba.csp.sentinel.demo.sofa.rpc.DemoService#sayHello(java.lang.Integer,java.lang.String,int)`

## Flow control based on caller

In many circumstances, it's also significant to control traffic flow based on the **caller**.
For example, assuming that there are two services A and B, both of them initiate remote call requests to the service provider.
If we want to limit the calls from service B only, we can set the `limitApp` of flow rule as the identifier of service B (e.g. service name).

Sentinel SOFARPC Adapter will automatically resolve the SOFARPC consumer's *application name* as the caller's name (`origin`),
and will bring the caller's name when doing resource protection.
If `limitApp` of flow rules is not configured (`default`), flow control will take effects on all callers.
If `limitApp` of a flow rule is configured with a caller, then the corresponding flow rule will only take effect on the specific caller.

## Global fallback

Sentinel SOFARPC Adapter supports global fallback configuration.
The global fallback will handle exceptions and give replacement result when blocked by
flow control, degrade or system load protection. You can implement your own `SofaRpcFallback` interface
and then register to `SofaRpcFallbackRegistry`. If no fallback is configured, Sentinel will wrap the `BlockException`
then directly throw it out.