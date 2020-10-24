# Sentinel Tracing SkyWalking

Sentinel Tracing SkyWalking provides integration with SkyWalking. It would output a SENTINEL span in the Block callback
with SkyWalking and OpenTracing SDK, and display in SkyWalking Tracing UI. This feature maybe can help to statistic, alert sentinel blocks. 

## Usage

To use Sentinel Tracing SkyWalking, you could add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-tracing-skywalking</artifactId>
    <version>x.y.z</version>
</dependency>
```

Refer to SkyWalking UI for Sentinel tracing

![](http://nepxion.gitee.io/docs/discovery-doc/Skywalking3.jpg)
![](http://nepxion.gitee.io/docs/discovery-doc/Skywalking4.jpg)