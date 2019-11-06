# Sentinel Zuul2 Adapter

Sentinel Zuul2 Adapter provides  **route level** and **customized API level**
flow control for Zuul API Gateway.

> *Note*: this adapter only support Zuul 2.x.

## How to use

> You can refer to demo `sentinel-demo-zuul2-gateway`

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

> If you want to use **route level** flow control, you need to implement a inbound filter for grouping and matching route additionally.
In the filter, you find route id and put it into SessionContext with using ZuulConstant.PROXY_ID_KEY as key.

## How it works

As Zuul 2.x is based on netty, a event-drive model, so we use `AsyncEntry` to do flow control.

- `SentinelZuulInboundFilter`: This inbound filter will regard all proxy ID (`proxy` in `SessionContext`) and all customized API as resources. When a `BlockException` caught, the filter will set endpoint to find a fallback to execute.
- `SentinelZuulOutboundFilter`: When the response has no exception caught, the post filter will trace the exception and complete the entries.
- `SentinelZuulEndpoint`: When an exception is caught, the filter will find a fallback to execute.

## Integration with Sentinel Dashboard

1. Start [Sentinel Dashboard](https://github.com/alibaba/Sentinel/wiki/Dashboard).
2. You can configure the rules in Sentinel dashboard or via dynamic rule configuration.

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

## Request origin parser

You can register customized request origin parser like this:

```java
public class MyRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpRequestMessage request) {
        return request.getInboundRequest().getOriginalHost() + ":" + request.getInboundRequest().getOriginalPort();
    }
}
```