# Sentinel Spring OkHttp Adapter

## Introduction

Sentinel provides integration for OkHttp client to enable flow control for web requests.

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-okhttp-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

We can add the `SentinelOkHttpInterceptor` interceptor when `OkHttpClient` at initialization, for example:

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new SentinelOkHttpInterceptor())
        .build();
```

## Configuration

- `SentinelOkHttpConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| cleaner | aggregate URLs according to actual scenarios | `OkHttpUrlCleaner` | `DefaultOkHttpUrlCleaner` |
| fallback | handle request when it is blocked | `OkHttpFallback` | `DefaultOkHttpFallback` |

### cleaner (URL cleaner)

We can define `OkHttpUrlCleaner` to aggregate URLs according to actual scenarios, for example: /okhttp/back/1 ==> /okhttp/back/{id}

```java
OkHttpUrlCleaner cleaner = (request, connection) -> {
    String url = request.url().toString();
    String regex = "/okhttp/back/";
    if (url.contains(regex)) {
        url = url.substring(0, url.indexOf(regex) + regex.length()) + "{id}";
    }
    return url;
};
SentinelOkHttpConfig.setCleaner(cleaner);
```

### fallback (Block handling)

We can define `OkHttpFallback` to handle request is blocked according to the actual scenario, for example:

```java
public class DefaultOkHttpFallback implements OkHttpFallback {

    @Override
    public Response handle(Request request, Connection connection, BlockException e) {
        // Just wrap and throw the exception.
        throw new SentinelRpcException(e);
    }
}
```