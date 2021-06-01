# Sentinel Zuul 2.x Adapter

This adapter provides **route level** and **customized API level**
flow control for Zuul 2.x API Gateway.

> *Note*: this adapter only supports Zuul 2.x.

## How to use

> You can refer to demo [`sentinel-demo-zuul2-gateway`](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-zuul2-gateway).

1. Add Maven dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-zuul2-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

2. Register filters

```java
filterMultibinder.addBinding().toInstance(new SentinelZuulInboundFilter(500));
filterMultibinder.addBinding().toInstance(new SentinelZuulOutboundFilter(500));
filterMultibinder.addBinding().toInstance(new SentinelZuulEndpoint());
```

## How it works

As Zuul 2.x is based on Netty, an event-driven asynchronous model, so we use `AsyncEntry`.

- `SentinelZuulInboundFilter`: This inbound filter will regard all routes (`routeVIP` in `SessionContext` by default) and all customized API as resources. When a `BlockException` caught, the filter will set endpoint to find a fallback to execute.
- `SentinelZuulOutboundFilter`: When the response has no exception caught, the post filter will trace the exception and complete the entries.
- `SentinelZuulEndpoint`: When an exception is caught, the filter will find a fallback to execute.

## Integration with Sentinel Dashboard

1. Start [Sentinel Dashboard](https://github.com/alibaba/Sentinel/wiki/Dashboard).
2. You can configure the rules in Sentinel dashboard or via dynamic rule configuration.

> You may need to add `-Dcsp.sentinel.app.type=1` property to mark this application as API gateway.

## Fallbacks

You can implement `ZuulBlockFallbackProvider` to define your own fallback provider when Sentinel `BlockException` is thrown.
The default fallback provider is `DefaultBlockFallbackProvider`.

By default fallback route is proxy ID (or customized API name).

Here is an example:

```java

// custom provider
public class MyBlockFallbackProvider implements ZuulBlockFallbackProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultBlockFallbackProvider.class);

    // you can define root as service level
    @Override
    public String getRoute() {
        return "my-route";
    }

    @Override
        public BlockResponse fallbackResponse(String route, Throwable cause) {
            RecordLog.info(String.format("[Sentinel DefaultBlockFallbackProvider] Run fallback route: %s", route));
            if (cause instanceof BlockException) {
                return new BlockResponse(429, "Sentinel block exception", route);
            } else {
                return new BlockResponse(500, "System Error", route);
            }
        }
 }

 // register fallback
 ZuulBlockFallbackManager.registerProvider(new MyBlockFallbackProvider());
```

Default block response:

```json
{
    "code":429,
    "message":"Sentinel block exception",
    "route":"/"
}
```
