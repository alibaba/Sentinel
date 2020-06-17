# Sentinel Quarkus Adapter

Sentinel provides `sentinel-annotation-quarkus-adapter` and `sentinel-jax-rs-quarkus-adapter` to
adapt [sentinel-annotation-cdi-interceptor](https://github.com/alibaba/Sentinel/tree/master/sentinel-extension/sentiel-annotation-cdi-interceptor)
and [sentinel-jax-rs-adapter](https://github.com/alibaba/Sentinel/tree/master/sentinel-adapter/sentinel-jax-rs-adapter) for Quarkus.

The integration module also provides `sentinel-native-image-quarkus-adapter` to support running Sentinel with Quarkus in native image mode.

To use sentinel-jax-rs-quarkus-adapter, you can simply add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-jax-rs-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

To use sentinel-annotation-quarkus-adapter, you can simply add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-annotation-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

When Quarkus application started, you can see the enabled feature like:

```
INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, resteasy, sentinel-annotation, sentinel-jax-rs]
```

## For Quarkus native image

If you want to integrate Quarkus with Sentinel while running in native image mode,
you should add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-native-image-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

And then add `--allow-incomplete-classpath` to `quarkus.native.additional-build-args`.

If you're using `sentinel-jax-rs-quarkus-adapter`, you'll need to set `quarkus.native.auto-service-loader-registration` to true.

When Quarkus application started, you can see the enabled feature like:

```
INFO  [io.quarkus] (main) Installed features: [cdi, resteasy, sentinel-annotation, sentinel-jax-rs, sentinel-native-image]
```

For more details you may refer to the `pom.xml` of [sentinel-demo-quarkus](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-quarkus).

### Limitations

`sentinel-native-image-quarkus-adapter` currently relies on `sentinel-logging-slf4j` to help Sentinel
run in native image mode easily, because `quarkus-core` provides `Target_org_slf4j_LoggerFactory` to substitute `getLogger` method.

Currently `sentinel-transport-simple-http` can work in native image mode, while `sentinel-transport-netty-http` cannot work in native image mode without extra config or substitutions.

## References for build native image or AOT

- [Quarkus - Tips for writing native applications](https://quarkus.io/guides/writing-native-applications-tips)
- [Quarkus - Class Loading Reference](https://quarkus.io/guides/class-loading-reference)
- [SubstrateVM LIMITATIONS](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md)
- [Accessing resources in Substrate VM images](https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md)
