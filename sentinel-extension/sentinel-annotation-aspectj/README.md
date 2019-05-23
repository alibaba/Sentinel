# Sentinel Annotation AspectJ

This extension is an AOP implementation using AspectJ for Sentinel annotations.
Currently only runtime waving is supported.

## Annotation

The `@SentinelResource` annotation indicates a resource definition, including:

- `value`: Resource name, required (cannot be empty)
- `entryType`: Resource entry type (inbound or outbound), `EntryType.OUT` by default
- `fallback` (refactored since 1.6.0): Fallback method when exceptions caught (including `BlockException`, but except the exceptions defined in `exceptionsToIgnore`). The fallback method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `fallbackClass` with corresponding `Class` (Note the method in other classes must be *static*). The method signature requirement:
  - The return type should match the origin method;
  - The parameter list should match the origin method, and an additional `Throwable` parameter can be provided to get the actual exception.
- `defaultFallback` (since 1.6.0): The default fallback method when exceptions caught (including `BlockException`, but except the exceptions defined in `exceptionsToIgnore`). Its intended to be a universal common fallback method. The method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `fallbackClass` with corresponding `Class` (Note the method in other classes must be *static*). The default fallback method signature requirement:
  - The return type should match the origin method;
  - parameter list should be empty, and an additional `Throwable` parameter can be provided to get the actual exception.
- `blockHandler`: Handler method that handles `BlockException` when blocked. The parameter list of the method should match original method, with the last additional parameter type `BlockException`. The return type should be same as the original method. The `blockHandler` method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `blockHandlerClass` with corresponding `Class` (Note the method in other classes must be *static*).
- `exceptionsToIgnore` (since 1.6.0): List of business exception classes that should not be traced and caught in fallback.
- `exceptionsToTrace` (since 1.5.1): List of business exception classes to trace and record. In most cases, using `exceptionsToIgnore` is better. If both `exceptionsToTrace` and `exceptionsToIgnore` are present, only `exceptionsToIgnore` will be activated.

For example:

```java
@SentinelResource(value = "abc", fallback = "doFallback")
public String doSomething(long i) {
    return "Hello " + i;
}

public String doFallback(long i, Throwable t) {
    // Return fallback value.
    return "fallback";
}

public String defaultFallback(Throwable t) {
    return "default_fallback";
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