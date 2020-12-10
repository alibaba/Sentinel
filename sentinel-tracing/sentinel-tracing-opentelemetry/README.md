# Sentinel Tracing OpenTelemetry

Sentinel Tracing OpenTelemetry provides integration with OpenTelemetry. It would output a SENTINEL span in the Block callback
with OpenTelemetry SDK, and display in Tracing UI (such as Jaeger, Zipkin etc). This feature maybe can help to statistic, alert sentinel blocks.

## Usage

To use Sentinel Tracing OpenTelemetry, you could add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-tracing-opentelemetry</artifactId>
    <version>x.y.z</version>
</dependency>
```

And you also need to add 3rd middleware dependency, refer to the example of `Sentinel Tracing OpenTracing`