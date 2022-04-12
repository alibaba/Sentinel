# Sentinel Metric Exporter

Sentinel Metric Exporter is a module which provides the Sentinel metric data exporting ability. Now you can integrate it into your Sentinel application, and then get the metric data in JMX. You can also integrate the JMX data into your monitor system easily, like Prometheus.

To use Sentinel Metric Exporter, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-metric-exporter</artifactId>
    <version>x.y.z</version>
</dependency>
```

And then you can find the MBean info in your tool.

![MBean Info](https://user-images.githubusercontent.com/25661357/150902723-6350a629-a173-47f9-a94b-6563ae55a5ce.png "MBean Info")