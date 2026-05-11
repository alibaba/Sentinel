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
| --- | --- | --- | --- |
| prefix | Customize resource prefix | `String` | `httpclient:` |
| extractor | Customize resource extractor | `ApacheHttpClientResourceExtractor` | `DefaultApacheHttpClientResourceExtractor` |
| fallback | Handle request when it is blocked | `ApacheHttpClientFallback` | `DefaultApacheHttpClientFallback` |

### Resource Extractor

The default extractor generates resource names in the format `METHOD:url` (e.g. `GET:http://example.com/api/users`),
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

### Migration from Apache HttpClient 4.x Adapter

> **Note:** The default resource naming convention differs between the HC4 and HC5 adapters. If you are migrating from
> `sentinel-apache-httpclient-adapter` to `sentinel-apache-httpclient5-adapter`, please review your existing flow control
> rules carefully — they will **not** match automatically.

| Adapter | Default Resource Name Format | Example |
| --- | --- | --- |
| `sentinel-apache-httpclient-adapter` (HC4) | URI only (path, may include query) | `httpclient:/api/users` |
| `sentinel-apache-httpclient5-adapter` (HC5) | `METHOD:full_url` (query/fragment stripped) | `httpclient:GET:http://example.com/api/users` |

Both adapters share the same default resource prefix (`httpclient:`), but the resource name suffix is incompatible.
This means any flow control rules configured against HC4 resource names will no longer take effect after migration.

You have two options:

1. **Update your rules** to match the new HC5 resource name format (recommended — aligns with the OkHttp adapter and
   provides better granularity by including the HTTP method).
2. **Preserve the old format** by supplying a custom `ApacheHttpClientResourceExtractor` that returns only the URI path,
   e.g.:

   ```java
   SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
   config.setExtractor(request -> request.getRequestUri());
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
