# Sentinel Annotation CDI extension

This extension is an implementation using CDI interceptor for Sentinel annotations. [JSR 318: Enterprise JavaBeansTM 3.1/Interceptors 1.2](https://jcp.org/en/jsr/detail?id=318) define the javax interceptor and [CDI](http://www.cdi-spec.org/) related specifications extends the Java Interceptors specification and allows interceptor bindings to be applied to CDI stereotypes.

[CDI](http://www.cdi-spec.org/) is an abbreviation for  Contexts and Dependency Injection, the related JSRs are : [JSR 365: Contexts and Dependency Injection for JavaTM 2.0](https://jcp.org/en/jsr/detail?id=365), [JSR 346: Contexts and Dependency Injection for JavaTM EE 1.1](https://jcp.org/en/jsr/detail?id=346), [JSR 299: Contexts and Dependency Injection for the JavaTM EE platform](https://jcp.org/en/jsr/detail?id=299)

## Annotation

The `@SentinelResourceBinding` is modified from `@SentinelResource` by adding `@InterceptorBinding` for CDI specification,
and in order to intercept all kinds of `@SentinelResourceBinding` with different attributes in one interceptor,
all attributes of `@SentinelResourceBinding` are annotated with `@Nonbinding`.

The `@SentinelResourceBinding` annotation indicates a resource definition, including:

- `value`: Resource name, required (cannot be empty)
- `entryType`: Traffic type (inbound or outbound), `EntryType.OUT` by default
- `fallback`: Fallback method when exceptions caught (including `BlockException`, but except the exceptions defined in `exceptionsToIgnore`). The fallback method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `fallbackClass` with corresponding `Class` (Note the method in other classes must be *static*). The method signature requirement:
  - The return type should match the origin method;
  - The parameter list should match the origin method, and an additional `Throwable` parameter can be provided to get the actual exception.
- `defaultFallback`: The default fallback method when exceptions caught (including `BlockException`, but except the exceptions defined in `exceptionsToIgnore`). Its intended to be a universal common fallback method. The method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `fallbackClass` with corresponding `Class` (Note the method in other classes must be *static*). The default fallback method signature requirement:
  - The return type should match the origin method;
  - parameter list should be empty, and an additional `Throwable` parameter can be provided to get the actual exception.
- `blockHandler`: Handler method that handles `BlockException` when blocked. The parameter list of the method should match original method, with the last additional parameter type `BlockException`. The return type should be same as the original method. The `blockHandler` method should be located in the same class with original method by default. If you want to use method in other classes, you can set the `blockHandlerClass` with corresponding `Class` (Note the method in other classes must be *static*).
- `exceptionsToIgnore`: List of business exception classes that should not be traced and caught in fallback.
- `exceptionsToTrace`: List of business exception classes to trace and record. In most cases, using `exceptionsToIgnore` is better. If both `exceptionsToTrace` and `exceptionsToIgnore` are present, only `exceptionsToIgnore` will be activated.

For example:

```java
@SentinelResourceBinding(value = "abc", fallback = "doFallback")
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

According to [9.4. Interceptor enablement and ordering](https://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#enabled_interceptors), to enable the interceptor,
we may add configuration in `resources/META-INF/beans.xml` like this:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd" bean-discovery-mode="all" version="2.0">
    <interceptors>
        <class>com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceInterceptor</class>
    </interceptors>
</beans>
```
