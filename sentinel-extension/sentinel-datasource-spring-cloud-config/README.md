# Sentinel DataSource Spring Cloud Config

Sentinel DataSource Spring Cloud Config provides integration with Spring Cloud Config
so that Spring Cloud Config can be the dynamic rule data source of Sentinel.

To use Sentinel DataSource Spring Cloud Config, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-spring-cloud-config</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `SpringCloudConfigDataSource` and register to rule managers.
For instance:

```Java
ReadableDataSource<String, List<FlowRule>> flowRuleDs = new SpringCloudConfigDataSource<>(ruleKey, s -> JSON.parseArray(s, FlowRule.class));
FlowRuleManager.register2Property(flowRuleDs.getProperty());
```

To notify the client that the remote config has changed, we could bind a git webhook callback with the
`com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator.refresh` API.
We may refer to the the sample `com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SpringCouldDataSourceTest#refresh` in test cases.

We offer test cases and demo in the package: `com.alibaba.csp.sentinel.datasource.spring.cloud.config.test`.
When you are running test cases, please follow the steps:

```
// First, start the Spring Cloud config server
com.alibaba.csp.sentinel.datasource.spring.cloud.config.server.ConfigServer

// Second, start the Spring Cloud config client
com.alibaba.csp.sentinel.datasource.spring.cloud.config.client.ConfigClient

// Third, run the test cases and demo
com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SentinelRuleLocatorTests
com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SpringCouldDataSourceTest
```