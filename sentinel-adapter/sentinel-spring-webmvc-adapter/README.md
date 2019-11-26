# Sentinel Spring MVC Interceptor

Sentinel provides Spring MVC Interceptor integration to enable flow control for web requests, And support url like '/foo/{id}'

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-webmvc-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Configure interceptor

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Add sentinel interceptor
        addSpringMvcInterceptor(registry);
        //If you want to sentinel the total flow, you can add total interceptor
        addSpringMvcTotalInterceptor(registry);
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        //Configure
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        //Custom configuration if necessary
        config.setHttpMethodSpecify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));
        //Add sentinel interceptor
        registry.addInterceptor(new SentinelInterceptor(config)).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Configure
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();
        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my-spring-mvc-total-url-request");
        //Add sentinel interceptor
        registry.addInterceptor(new SentinelTotalInterceptor(config)).addPathPatterns("/**");
    }
}
```

Configure 'BlockException' handler, there are three options:
1. Global exception handling in spring MVC. <Recommend>
```java
@ControllerAdvice
@Order(0)
public class SentinelSpringMvcBlockHandlerConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public String sentinelBlockHandler(BlockException e) {
        AbstractRule rule = e.getRule();
        logger.info("Blocked by sentinel, {}", rule.toString());
        return "Blocked by Sentinel";
    }
}

```
2. Use `DefaultBlockExceptionHandler`
```java
//SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();
SentinelWebMvcConfig config = new SentinelWebMvcConfig();
config.setBlockExceptionHandler(new DefaultBlockExceptionHandler());
```
3. `implements BlockExceptionHandler`
```java
//SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();
SentinelWebMvcConfig config = new SentinelWebMvcConfig();
config.setBlockExceptionHandler((request, response, e) -> {
    String resourceName = e.getRule().getResource();
    //Depending on your situation, you can choose to process or throw
    if ("/hello".equals(resourceName)) {
        //Do something ......
        //Write string or error page;
        response.getWriter().write("Blocked by sentinel");
    } else {
        //Handle it in global exception handling
        throw e;
    }
});
```

Configuration
- Common configuration in `SentinelWebMvcConfig` and `SentinelWebMvcTotalConfig`

| name | description | type | default value |
|------|------------|------|-------|
| blockExceptionHandler| The handler when blocked by sentinel, there are three options:<br/>1. The default value is null, you can hanlde `BlockException` in spring MVC;<br/>2.Use `DefaultBlockExceptionHandler`;<br/>3. `implements BlockExceptionHandler`  | `BlockExceptionHandler` | `null` |
| originParser | `RequestOriginParser` interface is useful for extracting request origin (e.g. IP or appName from HTTP Header) from HTTP request | `RequestOriginParser` | `null` |

- `SentinelWebMvcConfig` configuration

| name | description | type | default value |
|------|------------|------|-------|
| urlCleaner | The `UrlCleaner` interface is designed for clean and unify the URL resource. For REST APIs, you can to clean the URL resource (e.g. `/api/user/getById` and `/api/user/getByName` -> `/api/user/getBy*`), avoid the amount of context and will exceed the threshold | `UrlCleaner` | `null` |
| requestAttributeName | Attribute name in request used by sentinel, please check record log, if it is already used, please set | `String` | sentinel_spring_mvc_entry_container |
| httpMethodSpecify | Specify http method, for example: GET:/hello | `boolean` | `false` |


`SentinelWebMvcTotalConfig` configuration

| name | description | type | default value |
|------|------------|------|-------|
| totalResourceName | The resource name in `SentinelTotalInterceptor` | `String` | spring-mvc-total-url-request |
| requestAttributeName | Attribute name in request used by sentinel, please check record log, if it is already used, please set | `String` | sentinel_spring_mvc_total_entry_container |

