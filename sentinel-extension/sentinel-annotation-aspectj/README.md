# Sentinel Annotation AspectJ

This extension is an AOP implementation using AspectJ for Sentinel annotations.
Currently only runtime waving is supported.

## Annotation

The `@SentinelResource` annotation indicates a resource definition, including:

- `value`: Resource name, required (cannot be empty)
- `entryType`: Resource entry type (inbound or outbound), `EntryType.OUT` by default
- `fallback`: Fallback method when degraded (optional).
The fallback method should be located in the same class with original method.
The signature of the fallback method should match the original method (parameter types and return type).
- `blockHandler`: Handler method that handles `BlockException` when blocked.
The signature should match original method, with the last additional parameter type `BlockException`.
The block handler method should be located in the same class with original method by default.
If you want to use method in other classes, you can set the `blockHandlerClass` with corresponding `Class`
(Note the method in other classes must be *static*).

For example:

```java
@SentinelResource(value = "abc", fallback = "doFallback", blockHandler = "handleException")
public String doSomething(long i) {
    return "Hello " + i;
}

public String doFallback(long i) {
    // Return fallback value.
    return "Oops, degraded";
}

public String handleException(long i, BlockException ex) {
    // Handle the block exception here.
    return null;
}
```

## Configuration

### AspectJ

If you are using AspectJ directly, you can add the Sentinel annotation aspect to
your `aop.xml`:

```xml
<aspects>
    <aspect name="com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect"/>
</aspects>
```

### Spring AOP

If you are using Spring AOP, you should add a configuration to register the aspect
as a Spring bean:

```java
@Configuration
public class SentinelAspectConfiguration {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
```

An example for using Sentinel Annotation AspectJ with Spring Boot can be found [here](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-annotation-spring-aop).