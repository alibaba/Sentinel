# Sentinel Apache HttpClient 5.x Adapter

## Introduction

Sentinel provides integration for Apache HttpClient 5.x to enable flow control for outgoing HTTP requests.

## Usage

### Add dependency

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-apache-httpclient5-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

### Build the HttpClient

```java
CloseableHttpClient httpclient = HttpClients.custom()
    .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
        new SentinelApacheHttpClient5Handler())
    .build();
```

Or with custom configuration:

```java
SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
config.setPrefix("httpclient:");
config.setExtractor(myExtractor);
config.setFallback(myFallback);

CloseableHttpClient httpclient = HttpClients.custom()
    .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
        new SentinelApacheHttpClient5Handler(config))
    .build();
```

### Configuration

| Name | Description | Type | Default Value |
|------|------------|------|---------------|
| prefix | Customize resource prefix | `String` | `httpclient:` |
| extractor | Customize resource extractor | `ApacheHttpClientResourceExtractor` | `DefaultApacheHttpClientResourceExtractor` |
| fallback | Handle request when it is blocked | `ApacheHttpClientFallback` | `DefaultApacheHttpClientFallback` |

### Resource Extractor

The default extractor generates resource names in the format `METHOD:url` (e.g. `GET:/api/users`),
with query parameters and fragments stripped. You can customize this by implementing `ApacheHttpClientResourceExtractor`:

```java
public class MyResourceExtractor implements ApacheHttpClientResourceExtractor {
    @Override
    public String extractor(ClassicHttpRequest request) {
        // custom resource name extraction logic
        return request.getMethod() + ":" + request.getRequestUri();
    }
}
```

### Fallback

The default fallback throws `SentinelRpcException`. You can customize the behavior:

```java
public class MyFallback implements ApacheHttpClientFallback {
    @Override
    public ClassicHttpResponse handle(ClassicHttpRequest request, BlockException e) {
        // return a custom response or throw exception
        throw new SentinelRpcException(e);
    }
}
```
