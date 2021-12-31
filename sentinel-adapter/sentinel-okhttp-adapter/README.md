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
        .addInterceptor(new SentinelOkHttpInterceptor(new SentinelOkHttpConfig()))
        .build();
```

## Configuration

`SentinelOkHttpConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| resourcePrefix | customized resource name prefix | `String` | `okhttp:` |
| resourceExtractor | customized resource extractor | `OkHttpResourceExtractor` | `DefaultOkHttpResourceExtractor` |
| fallback | handle request when it is blocked | `OkHttpFallback` | `DefaultOkHttpFallback` |

### Resource Extractor

We can define `OkHttpResourceExtractor` to customize the logic of extracting resource name from the HTTP request.
For example: `okhttp:GET:ip:port/okhttp/back/1 ==> /okhttp/back/{id}`

```java
OkHttpResourceExtractor extractor = (request, connection) -> {
    String resource = request.url().toString();
    String regex = "/okhttp/back/";
    if (resource.contains(regex)) {
        resource = resource.substring(0, resource.indexOf(regex) + regex.length()) + "{id}";
    }
    return resource;
};
```

The pattern of default resource name extractor is `${HTTP_METHOD}:${URL}` (e.g. `GET:/foo`).

### Fallback (Block handling)

We can define `OkHttpFallback` to handle blocked request. For example:

```java
public class DefaultOkHttpFallback implements OkHttpFallback {

    @Override
    public Response handle(Request request, Connection connection, BlockException e) {
        return new Response(myErrorBuilder);
    }
}
```