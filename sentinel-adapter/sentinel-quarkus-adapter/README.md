# sentinel quarkus adapter

sentinel quarkus adapter provides `sentinel-annotation-quarkus-adapter` and `sentinel-jax-rs-quarkus-adapter` to adapt `sentinel-annotation-cdi-interceptor` and `sentinel-jax-rs-adapter` for quarkus

sentinel quarkus adapter also provides `sentinel-native-image-quarkus-adapter` to support running sentinel with quarkus in native image mode.

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

if your quarkus application want to use both `sentinel-annotation-quarkus-adapter` and `sentinel-jax-rs-quarkus-adapter` , then add these two dependency together to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-jax-rs-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-annotation-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

when quarkus application started, you can see the enabled feature like:

```
INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, resteasy, sentinel-annotation, sentinel-jax-rs]
```

## for quarkus native image

if you want to make sentinel with quarkus running in native image mode, you should add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-native-image-quarkus-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

and then add `--allow-incomplete-classpath` to `quarkus.native.additional-build-args`.

if you use `sentinel-jax-rs-quarkus-adapter` you should set `quarkus.native.auto-service-loader-registration` to true.

you can refer to `sentinel-demo-quarkus`'s `pom.xml` for more details.

when quarkus application started, you can see the enabled feature like:

```
INFO  [io.quarkus] (main) Installed features: [cdi, resteasy, sentinel-annotation, sentinel-jax-rs, sentinel-native-image]
```

### notes for limitations

`sentinel-native-image-quarkus-adapter` currently rely on `sentinel-logging-slf4j` to make sentinel run in native image mode easily, because `quarkus-core` provides `Target_org_slf4j_LoggerFactory` to substitue `getLogger` method.

currently `sentinel-transport-simple-http` can work in native image mode, while `sentinel-transport-netty-http` cannot work in native image mode without extra config or substitutions.

## references for build native image or AOT

- [Quarkus - Tips for writing native applications](https://quarkus.io/guides/writing-native-applications-tips)

- [Quarkus - Class Loading Reference](https://quarkus.io/guides/class-loading-reference)

- [substratevm LIMITATIONS](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md)

- [Accessing resources in Substrate VM images](https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md)
