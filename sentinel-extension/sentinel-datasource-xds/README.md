# Sentinel DataSource xds

Sentinel DataSource xds provides integration with xds.

This data now only provides access to zero-trust underlying data (certificates and authentication rules).

The module requires JDK 1.8 or later.

> **NOTE**: Currently we do only support xds for istio 15 and 16 versions

## Usage

To use Sentinel DataSource xds, you should add the following dependency:

```xml

<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-xds</artifactId>
    <version>x.y.z</version>
</dependency>

```

Then you can create an `RedisDataSource` and register the TrustManager.
For instance:

```java
Converter<XdsProperty, CertPair> parser=source->source.getCertPairRepository().getInstance();
        XdsConfigProperties xdsConfigProperties=TestUtil.createConfig();
        XdsDataSource<CertPair> xdsDataSource=new XdsDataSource<>(xdsConfigProperties,parser);
        xdsDataSource.registerTrustManager(TrustManager.getInstance());
```

- `xdsConfigProperties`: use `XdsConfigProperties` class to build your xds config
- `parser`: The parser doesn't really work because the XdsDataSource relies on a registration to pass data

You can also create multi data sources to subscribe for different rule type.

## How to build XdsConfigProperties

### Build default XdsConfigProperties

```java
XdsConfigProperties config=XdsConfigProperties.getXdsDefaultXdsProperties();

```

### Build XdsConfigProperties from env

```java
XdsConfigProperties config=XdsConfigProperties.getFromXdsPropertiesEnv();
```

### Build Custom XdsConfigProperties

```java
XdsConfigProperties config=new XdsConfigProperties();
        config.setXXX(xxx);
```
