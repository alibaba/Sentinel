# Sentinel Tracing OpenTracing

Sentinel Tracing OpenTracing provides integration with OpenTracing. It would output a SENTINEL span in the Block callback 
with OpenTracing SDK, and display in Tracing UI (such as Jaeger, Zipkin etc). This feature maybe can help to statistic, alert sentinel blocks.

## Usage

To use Sentinel Tracing OpenTracing, you could add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-tracing-opentracing</artifactId>
    <version>x.y.z</version>
</dependency>
```

And you also need to add 3rd middleware dependency, for example:

Integration with Jaeger Client for Spring Cloud

```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-spring-cloud-starter</artifactId>
    <version>x.y.z</version>
</dependency>

<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-spring-jaeger-starter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Refer to Jaeger UI for Sentinel tracing

![](http://nepxion.gitee.io/docs/discovery-doc/Jaeger6.jpg)