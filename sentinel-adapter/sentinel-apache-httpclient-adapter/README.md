# Sentinel Apache Httpclient Adapter

## Introduction

Sentinel provides integration for OkHttp client to enable flow control for web requests.

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-apache-httpclient-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

We can use the `SentinelApacheHttpClientBuilder` when `CloseableHttpClient` at initialization, for example:

```java
CloseableHttpClient httpclient = new SentinelApacheHttpClientBuilder().build();
```

## Configuration

- `SentinelApacheHttpClientConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| prefix | custom resource prefix | `String` | `httpclient:` |
| extractor | custom resource extractor | `ApacheHttpClientResourceExtractor` | `DefaultApacheHttpClientResourceExtractor` |
| fallback | handle request when it is blocked | `ApacheHttpClientFallback` | `DefaultApacheHttpClientFallback` |

### extractor (resource extractor)

We can define `ApacheHttpClientResourceExtractor` to custom resource extractor replace `DefaultApacheHttpClientResourceExtractor`, for example: httpclient:GET:/httpclient/back/1 ==> httpclient:GET:/httpclient/back/{id}

```java
SentinelApacheHttpClientConfig.setExtractor(new ApacheHttpClientResourceExtractor() {

    @Override
    public String extractor(String method, String uri, HttpRequestWrapper request) {
        String regex = "/httpclient/back/";
        if (uri.contains(regex)) {
            uri = uri.substring(0, uri.indexOf(regex) + regex.length()) + "{id}";
        }
        return method + ":" + uri;
    }
});
```

### fallback (Block handling)

We can define `ApacheHttpClientFallback` to handle request is blocked according to the actual scenario, for example:

```java
public class DefaultApacheHttpClientFallback implements ApacheHttpClientFallback {

    @Override
    public CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e) {
        // Just wrap and throw the exception.
        throw new SentinelRpcException(e);
    }
}
```