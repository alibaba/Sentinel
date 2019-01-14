# Sentinel Zuul Adapter

Sentinel Zuul Adapter provides **ServiceId level** and **API Path level** flow control for Zuul gateway service.

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

```java
// get registry
final FilterRegistry r = FilterRegistry.instance();
// this is property config. set filter enable
SentinelZuulProperties properties = new SentinelZuulProperties();
properties.setEnabled(true);
// set url cleaner, here use default
DefaultUrlCleaner defaultUrlCleaner = new DefaultUrlCleaner();
// set origin parser. here use default
DefaultRequestOriginParser defaultRequestOriginParser = new DefaultRequestOriginParser();

// register filters. you must register all three filters.
SentinelPreFilter sentinelPreFilter = new SentinelPreFilter(properties, defaultUrlCleaner, defaultRequestOriginParser);
r.put("sentinelPreFilter", sentinelPreFilter);
SentinelPostFilter postFilter = new SentinelPostFilter(properties);
r.put("sentinelPostFilter", postFilter);
SentinelErrorFilter errorFilter = new SentinelErrorFilter(properties);
r.put("sentinelErrorFilter", errorFilter);
```

## How it works

As Zuul run as per thread per connection block model, we add filters around `route Filter` to trace sentinel statistics.

- `SentinelPreFilter`: Get an entry of resource, the first order is **ServiceId** (the key in RequestContext is `serviceId`, this can set in own custom filter), then **API Path**.
- `SentinelPostFilter`: When success response, exit entry.
- `SentinelPreFilter`:  When an `Exception` caught, trace the exception and exit context.

<img width="792" src="https://user-images.githubusercontent.com/9305625/47277113-6b5da780-d5ef-11e8-8a0a-93a6b09b0887.png">

The order of filters can be changed in property.

The invocation chain resembles this:

```bash
EntranceNode: machine-root(t:3 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
-EntranceNode: coke(t:2 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
--coke(t:2 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
---/coke/coke(t:0 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
-EntranceNode: sentinel_default_context(t:0 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
-EntranceNode: book(t:1 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
--book(t:1 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
---/book/coke(t:0 pq:0 bq:0 tq:0 rt:0 prq:0 1mp:0 1mb:0 1mt:0)
```

- `book` and `coke` are serviceId.
- `/book/coke` is api path, the real API path is `/coke`.

## Integration with Sentinel Dashboard

1. Start [Sentinel Dashboard](https://github.com/alibaba/Sentinel/wiki/Dashboard).
2. You can configure the rules in Sentinel dashboard or via dynamic rule configuration.

## Fallbacks

You can implement `SentinelFallbackProvider` to define your own fallback provider when Sentinel `BlockException` is thrown.
The default fallback provider is `DefaultBlockFallbackProvider`.

By default fallback route is `ServiveId + URI PATH`, example `/book/coke`, first `book` is serviceId, `/coke` is URI PATH, so that both can be needed.

Here is an example:

```java

// custom provider
public class MyBlockFallbackProvider implements ZuulBlockFallbackProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultBlockFallbackProvider.class);

    // you can define root as service level
    @Override
    public String getRoute() {
        return "/coke/coke";
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