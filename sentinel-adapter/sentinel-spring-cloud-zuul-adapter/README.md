# Sentinel Spring Cloud Zuul Adapter

Zuul does not provide rateLimit function, If use default `SentinelRibbonFilter` route filter. it wrapped by Hystrix Command. so only provide Service level 
circuit protect. 

Sentinel can provide `ServiceId` level and `API Path` level flow control for zuul gateway service. 

*Note*: this project is for zuul 1.

## How to use

1. Add maven dependency

```xml
       <dependency>
          <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-spring-cloud-zuul-starter</artifactId>
            <version>x.y.z</version>
        </dependency>

```

2. Set application.property

```
// default value is false
sentinel.zuul.enabled=true
```

## How it works

As Zuul run as per thread per connection block model, we add filters around `route Filter` to trace sentinel statistics.   

- `SentinelPreFilter`: Get an entry of resource,the first order is **ServiceId**, then **API Path**. 
- `SentinelPostFilter`: When success response,exit entry.
- `SentinelPreFilter`:  When get an `Exception`, trace the exception and exit context. 


the order of Filter can be changed by configuration:

```
sentinel.zuul.order.post=10
```


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

public class MyBlockFallbackProvider implements SentinelFallbackProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultBlockFallbackProvider.class);
    
    // you can define provider as route level 
    @Override
    public String getRoute() {
        return "/coke/coke";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        if (cause instanceof BlockException) {
            logger.info("get in fallback block exception:{}", cause);
            return response(HttpStatus.TOO_MANY_REQUESTS, route);
        } else {
            return response(HttpStatus.INTERNAL_SERVER_ERROR, route);
        }
    }
 }

```

## Origin parser

```java

WebCallbackManager.setRequestOriginParser(RequestOriginParser requestOriginParser)

```
