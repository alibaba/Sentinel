# Sentinel Prometheus Exporter

Sentinel Prometheus Exporter is a module which provides the Sentinel metrics data for prometheus.

You can integrate it into your Sentinel application, and then get the sentinel metrics in your prometheus.

## How it works

when the prometheus server collect the sentinel metrics,it get metrics from sentinel logs
![image](https://github.com/alibaba/Sentinel/assets/71377602/2982209b-a3c7-403b-ae50-1dc7a17f90b7)

## How to use

To use Sentinel Prometheus Exporter, you should add the following dependency:

### 1. add sentinel-prometheus-exporter

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-prometheus-exporter</artifactId>
    <version>x.y.z</version>
</dependency>
```

### 2. add prometheus dependency

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

### 3. set prometheus.yml with fetch config

```yaml
scrape_configs:
- job_name: 'sentinelMetrics'
  static_configs:
    - targets: ['localhost:20001']
```

## Params for exporter

you can set system params to control the exporter behavior

### 1.sentinel.prometheus.port

the port for prometheus exporter,default 20001

### 2.sentinel.prometheus.size

the max fetch nums for prometheus exporter,in case the memory is not enough,default 1024

### 3.sentinel.prometheus.delay

the delay time for fetching , may be it is still do some statistics work according to the sliding window size when fetching,

so need to set the delay time to insure the accuracy.

unit: second

default: 0

### 4.sentinel.prometheus.identify

set the resource which need to fetch,default null,fetch all resources

### 5.sentinel.prometheus.types

the types need to fetch,such as passQps,concurrency

format: "xx|xx|xx"

default: "passQps|blockQps|exceptionQps|rt|concurrency"

you can reset the types as you need to,exm: "passQps|rt|concurrency|occupiedPassQps"

the type is same as the MetricNode class variables, with range:
{"passQps","blockQps","successQps","exceptionQps","rt","occupiedPassQps","concurrency"}

## how it looks

![image](https://github.com/alibaba/Sentinel/assets/71377602/dedde134-53ed-4b4e-b184-98e55184aacf)
