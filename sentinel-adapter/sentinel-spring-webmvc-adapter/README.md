# Sentinel Spring MVC Adapter

## Introduction

Sentinel provides integration for Spring Web to enable flow control for web requests.

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-webmvc-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then we could add a configuration bean to configure the interceptor:

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        // Enable the HTTP method prefix.
        config.setHttpMethodSpecify(true);
        // Add to the interceptor list.
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");
    }
}
```

Then Sentinel will extract URL patterns defined in Web Controller as the web resource (e.g. `/foo/{id}`).

## Configuration

### Block handling

Sentinel Spring Web adapter provides a `BlockExceptionHandler` interface to handle the blocked requests.
We could set the handler via `SentinelWebMvcTotalConfig#setBlockExceptionHandler()` method.

By default the interceptor will throw out the `BlockException`.
We need to set a global exception handler function in Spring to handle it. An example:

```java
@ControllerAdvice
@Order(0)
public class SentinelBlockExceptionHandlerConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public String sentinelBlockHandler(BlockException e) {
        AbstractRule rule = e.getRule();
        logger.info("Blocked by Sentinel: {}", rule.toString());
        return "Blocked by Sentinel";
    }
}
```

We've provided a `DefaultBlockExceptionHandler`. When a request is blocked, the handler will return a default page
indicating the request is rejected (`Blocked by Sentinel (flow limiting)`).
The HTTP status code of the default block page is **429 (Too Many Requests)**.

We could also implement our implementation of the `BlockExceptionHandler` interface and
set to the config object. An example:

```java
SentinelWebMvcConfig config = new SentinelWebMvcConfig();
config.setBlockExceptionHandler((request, response, e) -> {
    String resourceName = e.getRule().getResource();
    // Depending on your situation, you can choose to process or throw
    if ("/hello".equals(resourceName)) {
        // Do something ......
        response.getWriter().write("Blocked by Sentinel");
    } else {
        // Handle it in global exception handling
        throw e;
    }
});
```

### Customized configuration

- Common configuration in `SentinelWebMvcConfig` and `SentinelWebMvcTotalConfig`:

| name | description | type | default value |
|------|------------|------|-------|
| `blockExceptionHandler`| The handler that handles the block request | `BlockExceptionHandler` | null (throw out the BlockException) |
| `originParser` | Extracting request origin (e.g. IP or appName from HTTP Header) from HTTP request | `RequestOriginParser` | - |

- `SentinelWebMvcConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| urlCleaner | The `UrlCleaner` interface is designed for clean and unify the URL resource. | `UrlCleaner` | - |
| requestAttributeName | Attribute key in request used by Sentinel (internal) | `String` | `$$sentinel_spring_web_entry_attr` |
| httpMethodSpecify | Specify whether the URL resource name should contain the HTTP method prefix (e.g. `POST:`). | `boolean` | `false` |
| webContextUnify | Specify whether unify web context(i.e. use the default context name). | `boolean` | `true` |

- `SentinelWebMvcTotalConfig` configuration:

| name | description | type | default value |
|------|------------|------|-------|
| totalResourceName | The resource name in `SentinelTotalInterceptor` | `String` | `spring-mvc-total-url-request` |
| requestAttributeName | Attribute key in request used by Sentinel (internal) | `String` | `$$sentinel_spring_web_total_entry_attr` |