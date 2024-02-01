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
    <artifactId>sentinel-prometheus-metric-exporter</artifactId>
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
    - targets: ['localhost:9092']
```

```yaml
Note: the port needs to be the same as the value in the configuration (csp.sentinel.prometheus.fetch.port)
```

## Params for exporter

you can set system params to control the exporter behavior

### 1.csp.sentinel.prometheus.fetch.port

the port for prometheus exporter,default 9092

### 2.csp.sentinel.prometheus.fetch.size

the max fetch nums for prometheus exporter,in case the memory is not enough,default 1024

### 3.csp.sentinel.prometheus.fetch.delay

the delay time for fetching , may be it is still do some statistics work according to the sliding window size when fetching,

so need to set the delay time to insure the accuracy.

unit: second

default: 0

### 4.csp.sentinel.prometheus.fetch.identify

set the resource which need to fetch,default null,fetch all resources

### 5.csp.sentinel.prometheus.fetch.types

the types need to fetch,such as passQps,concurrency

format: "xx|xx|xx"

default: "passQps|blockQps|exceptionQps|rt|concurrency"

you can reset the types as you need to,exm: "passQps|rt|concurrency|occupiedPassQps"

the type is same as the MetricNode class variables, with range:
{"passQps","blockQps","successQps","exceptionQps","rt","occupiedPassQps","concurrency"}

### 6.csp.sentinel.prometheus.app

set the appName when do PromSQL

## how it looks

![image](https://github.com/alibaba/Sentinel/assets/71377602/dedde134-53ed-4b4e-b184-98e55184aacf)
