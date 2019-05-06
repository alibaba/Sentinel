# Sentinel Metric Prometheus

Sentinel Metric Prometheus provides integration with Prometheus, so that Sentinel can expose its metrics in Prometheus format.

To use Sentinel Metric Prometheus, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-metric-prometheus</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then sentinel metric will automatic write to Prometheus client via Sentinel metric extension SPI.

You could export these metrics by any of [Prometheus exposition](https://github.com/prometheus/client_java#exporting). The The simplest of these may be the HTTPServer:

```java
HTTPServer server = new HTTPServer(1234);
```

We provide [PrometheusMetricExtensionDemo](./src/test/java/com/alibaba/csp/sentinel/metric/extension/prometheus/PrometheusMetricExtensionDemo.java) to show how to do this.

