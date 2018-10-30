# Sentinel Zuul Adapter

Zuul does not provide rateLimit function, If use default `SentinelRibbonFilter` route filter. it wrapped by Hystrix Command. so only provide Service level 
circuit protect. 

Sentinel can provide `ServiceId` level and `API Path` level flow control for zuul gateway service. 

*Note*: this project is for zuul 1.

## How to use

1. Add maven dependency

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
// this is property config. set filter ennable
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

- `SentinelPreFilter`: Get an entry of resource,the first order is **ServiceId**(the key in RequestContext is `serviceId`, this can set in own custom filter), then **API Path**. 
- `SentinelPostFilter`: When success response,exit entry.
- `SentinelPreFilter`:  When get an `Exception`, trace the exception and exit context. 


the order of Filter can be changed in property:



Filters create structure like:


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

`book` and `coke` are serviceId. 

`---/book/coke` is api path, the real uri is `/coke`. 


## Integration with Sentinel DashBord

1. Start [Sentinel DashBord](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0).

2. Sentinel has full rule config features. see [Dynamic-Rule-Configuration](https://github.com/alibaba/Sentinel/wiki/Dynamic-Rule-Configuration)

## Fallbacks

Implements `SentinelFallbackProvider` to define your own Fallback Provider when Sentinel Block Exception throwing. the default 
Fallback Provider is `DefaultBlockFallbackProvider`. 

By default Fallback route is `ServiveId + URI PATH`, example `/book/coke`, first `book` is serviceId, `/coke` is URI PATH. so that both  
can be needed.

Here is an example:

```java

// custom provider 
public class MyBlockFallbackProvider implements SentinelFallbackProvider {

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
 SentinelFallbackManager.registerProvider(new MyBlockFallbackProvider());

```

Default block response

```json

{
    "code":429,
    "message":"Sentinel block exception",
    "route":"/"
}
```

## Origin parser

```java

public class DefaultRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpServletRequest request) {
        return "";
    }
}

```
