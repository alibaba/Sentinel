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

# Async Servlet Support
After servlet 3.0 async servlet is supported for slow request. So sentinel's servlet adapter should support it and keep backward compatibility.  

But there may be difference between implementations of servlet. Generally there are two kinds of flow for async requests.  

## Normal Request Flow
For normal request the flow is shown below:  
![Normal Request](NormalRequestFlow.png)  

## Async Request Flow
### Tomcat
For async servlet in containers like tomcat the flow is like:  
![Async Request](AsyncRequestFlow1.png)  

### Spring Boot or Mock
For async servlet in some special implementation things changed:  
![Async Request](AsyncRequestFlow2.png)  

## Design
So in `CommonFilter` we should deal with several scenarios:

* Judge a request entering async pool by calling `isAsyncStarted`(since servlet 3.0).
* Register a listener for async servlets to exit their sentinel contexts.
* Trace `timeout` and `error` in the listeners for sentinel.
* Handle the reentering filters of async servlet in some implementations. Separate them from fresh requests.

To make sure backward compatibility we can use `reflection`. All reflections are cached for better performance.

