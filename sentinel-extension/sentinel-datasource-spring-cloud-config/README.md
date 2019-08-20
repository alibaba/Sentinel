# Sentinel DataSource SpringCloudConfig

Sentinel DataSource SpringCloudConfig provides integration with SpringCloudConfig so that SpringCloudConfig
can be the dynamic rule data source of Sentinel.

To use Sentinel DataSource SpringCloudConfig, you should add the following dependency:

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
//flow_rule is the propery key in SpringConfigConfig
SpringCloudConfigDataSource dataSource = new SpringCloudConfigDataSource("flow_rule", new Converter<String, List<FlowRule>>() {
            @Override
            public List<FlowRule> convert(String source) {
                return JSON.parseArray(source, FlowRule.class);
            }
        });
        FlowRuleManager.register2Property(dataSource.getProperty());
```

If the client want to perceive the remote config changed, it can binding a git webhook callback with the ```com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator.refresh```
 API. Like test demo  ```com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SpringCouldDataSourceTest.refresh``` do.


We  offer test cases and demo in:
[com.alibaba.csp.sentinel.datasource.spring.cloud.config.test]. 
When you run test cases, please follow the steps:

```
//first start config server
com.alibaba.csp.sentinel.datasource.spring.cloud.config.server.ConfigServer

//second start config client
com.alibaba.csp.sentinel.datasource.spring.cloud.config.client.ConfigClient

//third run test cases and demo
com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SentinelRuleLocatorTests
com.alibaba.csp.sentinel.datasource.spring.cloud.config.test.SpringCouldDataSourceTest
```