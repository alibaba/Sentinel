# Sentinel DataSource Nacos

Sentinel DataSource Nacos provides integration with [Nacos](http://nacos.io) so that Nacos
can be the dynamic rule data source of Sentinel.

To use Sentinel DataSource Nacos, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `NacosDataSource` and register to rule managers.
For instance:

```java
// remoteAddress is the address of Nacos
// groupId and dataId are concepts of Nacos
ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
```

We've also provided an example: [sentinel-demo-nacos-datasource](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-nacos-datasource).