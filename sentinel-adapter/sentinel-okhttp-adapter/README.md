# Sentinel OkHttp Adapter

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
| extractor | custom resource extractor | `OkHttpResourceExtractor` | `DefaultOkHttpResourceExtractor` |
| fallback | handle request when it is blocked | `OkHttpFallback` | `DefaultOkHttpFallback` |

### extractor (resource extractor)

We can define `OkHttpResourceExtractor` to custom resource extractor replace `DefaultOkHttpResourceExtractor`, for example: okhttp:GET:ip:port/okhttp/back/1 ==> /okhttp/back/{id}

```java
OkHttpResourceExtractor extractor = (request, connection) -> {
    String resource = request.url().toString();
    String regex = "/okhttp/back/";
    if (resource.contains(regex)) {
        resource = resource.substring(0, resource.indexOf(regex) + regex.length()) + "{id}";
    }
    return resource;
};
SentinelOkHttpConfig.setExtractor(extractor);
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