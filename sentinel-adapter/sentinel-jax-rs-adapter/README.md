# Sentinel adapter for JAX-RS

Sentinel provides integration to enable fault-tolerance and flow control for JAX-RS web requests.
Add the following dependency in `pom.xml` (if you are using Maven):

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-jax-rs-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

## SentinelJaxRsProviderFilter

The `SentinelJaxRsProviderFilter` is auto activated in pure JAX-RS application.

For Spring web applications you can configure with Spring bean:

```java
@Configuration
public class FilterConfig {

    @Bean
    public SentinelJaxRsProviderFilter sentinelJaxRsProviderFilter() {
        return new SentinelJaxRsProviderFilter();
    }
}
```

## DefaultExceptionMapper

Sentinel provides DefaultExceptionMapper to map Throwable to Response (with Status.INTERNAL_SERVER_ERROR),
in order to let SentinelJaxRsProviderFilter to be called and exit the Sentinel entry.

According to `3.3.4 Exceptions` of [jaxrs-2_1-final-spec](https://download.oracle.com/otn-pub/jcp/jaxrs-2_1-final-eval-spec/jaxrs-2_1-final-spec.pdf):

> Checked exceptions and throwables that have not been mapped and cannot be thrown directly MUST be wrapped in a container-specific exception that is then thrown and allowed to propagate to the underlying container.

If WebApplicationException or its subclasses are thrown, they'll be automatically converted to `Response` and can enter response filter.
If other kind of exceptions were thrown, and not matched by custom exception mapper, then the response filter cannot be called.
For this case, a default exception mapper maybe introduced.

According to `4.4 Exception Mapping Providers` of [jaxrs-2_1-final-spec](https://download.oracle.com/otn-pub/jcp/jaxrs-2_1-final-eval-spec/jaxrs-2_1-final-spec.pdf):

> When choosing an exception mapping provider to map an exception,  an implementation MUST use the provider whose generic type is the nearest superclass of the exception.  If two or more exception providers are applicable, the one with the highest priority MUST be chosen as described in Section 4.1.3.

If user also provides customized exception mapper of `Throwable`, then user has the responsibility to convert it to response and then the response filter can be called.

As describe in `6.7.1 exceptions` of [jaxrs-2_1-final-spec](https://download.oracle.com/otn-pub/jcp/jaxrs-2_1-final-eval-spec/jaxrs-2_1-final-spec.pdf):

> A response mapped from an exception MUST be processed using the ContainerResponse filter chain and the WriteTo interceptor chain (if an entity is present in the mapped response).

## SentinelJaxRsClientTemplate

For jax-rs client, we provide `SentinelJaxRsClientTemplate` you can use it like this:

```
Response response = SentinelJaxRsClientTemplate.execute(resourceName, new Supplier<Response>() {
    @Override
    public Response get() {
        return client.target(host).path(url).request()
                .get();
    }
});
```

or executeAsync like this:

```
Future<Response> future = SentinelJaxRsClientTemplate.executeAsync(resourceName, new Supplier<Future<Response>>() {
    @Override
    public Future<Response> get() {
        return client.target(host).path(url).request()
                .async()
                .get();
    }
});
```

When a request is blocked, Sentinel JAX-RS filter will return Response with status of `TOO_MANY_REQUESTS` indicating the request is rejected.

You can customize it by implement your own `SentinelJaxRsFallback` and register to `SentinelJaxRsConfig`.
