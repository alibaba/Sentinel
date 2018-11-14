# Sentinel Web Servlet Filter

Sentinel provides Servlet filter integration to enable flow control for web requests. Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-web-servlet</artifactId>
    <version>x.y.z</version>
</dependency>
```

To use the filter, you can simply configure your `web.xml` with:

```xml
<filter>
	<filter-name>SentinelCommonFilter</filter-name>
	<filter-class>com.alibaba.csp.sentinel.adapter.servlet.CommonFilter</filter-class>
</filter>

<filter-mapping>
	<filter-name>SentinelCommonFilter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>
```

For Spring web applications you can configure with Spring bean:

```java
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean sentinelFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CommonFilter());
        registration.addUrlPatterns("/*");
        registration.setName("sentinelFilter");
        registration.setOrder(1);

        return registration;
    }
}
```

When a request is blocked, Sentinel servlet filter will give a default page indicating the request blocked.
If customized block page is set (via `WebServletConfig.setBlockPage(blockPage)` method),
the filter will redirect the request to provided URL. You can also implement your own
block handler (the `UrlBlockHandler` interface) and register to `WebCallbackManager`.

The `UrlCleaner` interface is designed for clean and unify the URL resource.
For REST APIs, you have to clean the URL resource (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`), or
the amount of context and resources will exceed the threshold.

`RequestOriginParser` interface is useful for extracting request origin (e.g. IP or appName from HTTP Header)
from HTTP request. You can implement your own `RequestOriginParser` and register to `WebCallbackManager`.