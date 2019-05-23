# Sentinel Zuul Adapter

Sentinel Zuul Adapter provides **route level** and **customized API level**
flow control for Zuul API Gateway.

> *Note*: this adapter only support Zuul 1.x.

## How to use

1. Add Maven dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-zuul-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

2. Register filters

For Spring Cloud Zuul users, we only need to inject the three filters in Spring configuration class like this:

```java
@Configuration
public class ZuulConfig {

    @Bean
    public ZuulFilter sentinelZuulPreFilter() {
        // We can provider the filter order here.
        return new SentinelZuulPreFilter(10000);
    }

    @Bean
    public ZuulFilter sentinelZuulPostFilter() {
        return new SentinelZuulPostFilter(1000);
    }

    @Bean
    public ZuulFilter sentinelZuulErrorFilter() {
        return new SentinelZuulErrorFilter(-1);
    }
}
```

For original Zuul users:

```java
// Get filter registry
final FilterRegistry r = FilterRegistry.instance();

// We need to register all three filters.
SentinelZuulPreFilter sentinelPreFilter = new SentinelZuulPreFilter();
r.put("sentinelZuulPreFilter", sentinelPreFilter);
SentinelZuulPostFilter postFilter = new SentinelZuulPostFilter();
r.put("sentinelZuulPostFilter", postFilter);
SentinelZuulErrorFilter errorFilter = new SentinelZuulErrorFilter();
r.put("sentinelZuulErrorFilter", errorFilter);
```

## How it works

As Zuul run as per thread per connection block model, we add filters around route filter to trace Sentinel statistics.

- `SentinelZuulPreFilter`: This pre-filter will regard all proxy ID (`proxy` in `RequestContext`) and all customized API as resources. When a `BlockException` caught, the filter will try to find a fallback to execute.
- `SentinelZuulPostFilter`: When the response has no exception caught, the post filter will complete the entries.
- `SentinelZuulPreFilter`:  When an exception is caught, the filter will trace the exception and complete the entries.

<img width="792" src="https://user-images.githubusercontent.com/9305625/47277113-6b5da780-d5ef-11e8-8a0a-93a6b09b0887.png">

The order of filters can be changed via the constructor.

The invocation chain resembles this:

```bash
-EntranceNode: sentinel_gateway_context$$route$$another-route-b(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:8 1mb:1 1mt:9)
--another-route-b(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:4 1mb:1 1mt:5)
--another_customized_api(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:4 1mb:0 1mt:4)
-EntranceNode: sentinel_gateway_context$$route$$my-route-1(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:6 1mb:0 1mt:6)
--my-route-1(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:2 1mb:0 1mt:2)
--some_customized_api(t:0 pq:0.0 bq:0.0 tq:0.0 rt:0.0 prq:0.0 1mp:2 1mb:0 1mt:2)
```

## Integration with Sentinel Dashboard

1. Start [Sentinel Dashboard](https://github.com/alibaba/Sentinel/wiki/Dashboard).
2. You can configure the rules in Sentinel dashboard or via dynamic rule configuration.

## Fallbacks

You can implement `SentinelFallbackProvider` to define your own fallback provider when Sentinel `BlockException` is thrown.
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
    public String parseOrigin(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
```