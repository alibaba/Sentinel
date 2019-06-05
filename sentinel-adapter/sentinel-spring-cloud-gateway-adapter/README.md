# Sentinel Spring Cloud Gateway Adapter

> Note: this module requires Java 8 or later version.

Sentinel provides integration module with Spring Cloud Gateway.
The integration module is based on the Sentinel Reactor Adapter.

Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you only need to inject the corresponding `SentinelGatewayFilter` and `SentinelGatewayBlockExceptionHandler` instance
in Spring configuration. For example:

```java
@Configuration
public class GatewayConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(-1)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        // Register the block exception handler for Spring Cloud Gateway.
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
}
```

The gateway adapter will regard all `routeId` (defined in Spring properties) and all customized API definitions
(defined in `GatewayApiDefinitionManager` of `sentinel-api-gateway-adapter-common` module) as resources.

You can register various customized callback in `GatewayCallbackManager`:

- `setBlockHandler`: register a customized `BlockRequestHandler` to handle the blocked request. The default implementation is `DefaultBlockRequestHandler`, which returns default message like `Blocked by Sentinel: FlowException`.