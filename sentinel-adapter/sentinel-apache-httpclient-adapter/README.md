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

If we want to add some additional configurations, we can refer to the following code

```java
HttpClientBuilder builder = new SentinelApacheHttpClientBuilder();
//builder Other Definitions
CloseableHttpClient httpclient = builder.build();
```

## Configuration

- `SentinelApacheHttpClientConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| prefix | customize resource prefix | `String` | `httpclient:` |
| extractor | customize resource extractor | `ApacheHttpClientResourceExtractor` | `DefaultApacheHttpClientResourceExtractor` |
| fallback | handle request when it is blocked | `ApacheHttpClientFallback` | `DefaultApacheHttpClientFallback` |

### extractor (resource extractor)

We can define `ApacheHttpClientResourceExtractor` to customize resource extractor replace `DefaultApacheHttpClientResourceExtractor` at `SentinelApacheHttpClientBuilder` default config, for example: httpclient:GET:/httpclient/back/1 ==> httpclient:GET:/httpclient/back/{id}

```java
SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
config.setExtractor(new ApacheHttpClientResourceExtractor() {

    @Override
    public String extractor(HttpRequestWrapper request) {
        String contains = "/httpclient/back/";
        String uri = request.getRequestLine().getUri();
        if (uri.startsWith(contains)) {
            uri = uri.substring(0, uri.indexOf(contains) + contains.length()) + "{id}";
        }
        return request.getMethod() + ":" + uri;
    }
});
CloseableHttpClient httpclient = new SentinelApacheHttpClientBuilder(config).build();
```

### fallback (Block handling)

We can define `ApacheHttpClientFallback` at `SentinelApacheHttpClientBuilder` default config, to handle request is blocked according to the actual scenario, for example:

```java
public class DefaultApacheHttpClientFallback implements ApacheHttpClientFallback {

    @Override
    public CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e) {
        // Just wrap and throw the exception.
        throw new SentinelRpcException(e);
    }
}
```