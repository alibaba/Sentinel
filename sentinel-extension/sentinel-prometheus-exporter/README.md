# Sentinel Prometheus Exporter

Sentinel Prometheus Exporter is a module which provides the Sentinel metrics data for prometheus.

You can integrate it into your Sentinel application, and then get the sentinel metrics in your prometheus.

## How to use
To use Sentinel Prometheus Exporter, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-prometheus-exporter</artifactId>
    <version>x.y.z</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient</artifactId>
    <version>0.3.0</version>
</dependency>
```
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_httpserver</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Params for exporter
you can set system params to control the exporter behavior
### sentinel.prometheus.port
the port for prometheus exporter,default 20001
### sentinel.prometheus.app
the appName for prometheus exporter,default same as sentinel appName

## Types for exporter

