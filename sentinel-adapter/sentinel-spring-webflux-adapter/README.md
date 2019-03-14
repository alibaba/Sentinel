# Sentinel Spring WebFlux Adapter

> Note: this module requires Java 8 or later version.

Sentinel provides integration module with Spring WebFlux, so reactive web applications can also leverage Sentinel's flow control
and circuit breaking to achieve reliability. The integration module is based on the Sentinel Reactor Adapter.

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-webflux-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you only need to inject the corresponding `SentinelWebFluxFilter` and `SentinelBlockExceptionHandler` instance
in Spring configuration. For example:

```java
@Configuration
public class WebFluxConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public WebFluxConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                         ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(-1)
    public SentinelBlockExceptionHandler sentinelBlockExceptionHandler() {
        // Register the block exception handler for Spring WebFlux.
        return new SentinelBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @Order(-1)
    public SentinelWebFluxFilter sentinelWebFluxFilter() {
        // Register the Sentinel WebFlux filter.
        return new SentinelWebFluxFilter();
    }
}
```

You can register various customized callback in `WebFluxCallbackManager`:

- `setBlockHandler`: register a customized `BlockRequestHandler` to handle the blocked request. The default implementation is `DefaultBlockRequestHandler`, which returns default message like `Blocked by Sentinel: FlowException`.
- `setUrlCleaner`: used for normalization of URL. The function type is `(ServerWebExchange, String) → String`, which means `(webExchange, originalUrl) → finalUrl`.
- `setRequestOriginParser`: used to resolve the origin from the HTTP request. The function type is `ServerWebExchange → String`.

You can also refer to the demo: [sentinel-demo-spring-webflux](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-spring-webflux).