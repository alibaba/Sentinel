# Sentinel for jax-rs

Sentinel provides filter integration to enable flow control for web requests.
Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-jax-rs-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

the `SentinelJaxRsProviderFilter` is auto activated in pure jax-rs application.

For Spring web applications you can configure with Spring bean:

```java
@Configuration
public class FilterConfig {

    @Bean
    public SentinelJaxRsProviderFilter sentinelJaxRsProviderFilter() {
        return new SentinelJaxRsProviderFilter();
    }
}
```

For jax-rs client, register `SentinelJaxRsClientFilter` when build Client

```
Client client = ClientBuilder.newClient()
                .register(new SentinelJaxRsClientFilter());
```

When a request is blocked, Sentinel jax-rs filter will return Response with status of TOO_MANY_REQUESTS indicating the request is rejected.

You can customize it by implement your own `SentinelJaxRsFallback` and register to `SentinelJaxRsConfig`.

The `RequestOriginParser` interface is useful for extracting request origin (e.g. IP or appName from HTTP Header)
from HTTP request. You can implement your own `RequestOriginParser` and register to `SentinelJaxRsConfig`.
