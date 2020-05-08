# sentinel quarkus adapter

sentinel quarkus adapter provides `sentinel-annotation-quarkus-adapter` and `sentinel-jax-rs-quarkus-adapter` to adapt `sentinel-annotation-cdi-interceptor` and `sentinel-jax-rs-adapter` for quarkus

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

if your quarkus appilcation want to use both `sentinel-annotation-quarkus-adapter` and `sentinel-jax-rs-quarkus-adapter` , then add these two dependency together to your `pom.xml`:

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

sentinel quarkus adapter's support for quarkus native image build is under development. It will coming soon.

## references for build native image or AOT

- [Quarkus - Tips for writing native applications](https://quarkus.io/guides/writing-native-applications-tips)

- [Quarkus - Class Loading Reference](https://quarkus.io/guides/class-loading-reference)

- [substratevm LIMITATIONS](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md)

- [Accessing resources in Substrate VM images](https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md)
