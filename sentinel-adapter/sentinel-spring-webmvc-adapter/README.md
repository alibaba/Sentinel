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

Config interceptor

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
        //Config
        SentinelSpringMvcConfig config = new SentinelSpringMvcConfig();

        //Custom configuration if necessary
        config.setHttpMethodSpecify(false)
                .setOriginParser(request -> request.getHeader("S-user"));

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelInterceptor(config)).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelSpringMvcTotalConfig config = new SentinelSpringMvcTotalConfig();

        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container")
                .setTotalTarget("my_spring_mvc_total_url_request");

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelTotalInterceptor(config)).addPathPatterns("/**");
    }
}
```

Config BlockException handler

```java
@ControllerAdvice
@Order(0)
public class SentinelSringMvcBlockHandlerConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public JSONObject sentinelBlockHandler(BlockException e) {
        AbstractRule rule = e.getRule();
        //Log
        logger.info("Blocked by sentinel, {}", rule.toString());
        //Return object
        JSONObject bolckedJson = new JSONObject();
        bolckedJson.put("code", -2);
        bolckedJson.put("message", "Blocked by Sentinel");
        return ResultWrapper.blocked();
    }
}
```