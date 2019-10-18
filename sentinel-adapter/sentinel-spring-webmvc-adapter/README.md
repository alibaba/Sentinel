# Sentinel Spring Webmvc Interceptor

Sentinel provides Spring Webmvc Interceptor integration to enable flow control for web requests.
Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-webmvc-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

To activate the interceptor, you can simply configure your `web.xml` with:

```xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/**" />
        <bean class="com.alibaba.scp.sentinel.adapter.spring.webmvc.SentinelHandlerInterceptor.java" />
    </mvc:interceptor>
</mvc:interceptors>
```

For Spring web applications you can configure with Spring bean:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SentinelHandlerInterceptor()).addPathPatterns("/**");
    }
}
```

When using Sentinel `sentinel-web-servlet` and `sentinel-spring-webmvc-adapter` at same time,
The `sentinel-spring-webmvc-adapter` will will automatically disable.