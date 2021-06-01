# Sentinel DataSource Consul

Sentinel DataSource Consul provides integration with Consul. The data source leverages blocking query (backed by
long polling) of Consul.

## Usage

To use Sentinel DataSource Consul, you could add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-consul</artifactId>
    <version>x.y.z</version>
</dependency>

```

Then you can create a `ConsulDataSource` and register to rule managers.
For instance:

```java
ReadableDataSource<String, List<FlowRule>> dataSource = new ConsulDataSource<>(host, port, ruleKey, waitTimeoutInSecond, flowConfigParser);
FlowRuleManager.register2Property(dataSource.getProperty());
```

- `ruleKey`: the rule persistence key
- `waitTimeoutInSecond`: long polling timeout (in second) of the Consul API client
