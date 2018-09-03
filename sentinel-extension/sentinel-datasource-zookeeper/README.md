# Sentinel DataSource ZooKeeper

Sentinel DataSource ZooKeeper provides integration with ZooKeeper so that ZooKeeper
can be the dynamic rule data source of Sentinel. The data source uses push model (listener).

To use Sentinel DataSource ZooKeeper, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-zookeeper</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `ZookeeperDataSource` and register to rule managers.
For instance:

```java
// `path` is the data path in ZooKeeper
ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(remoteAddress, path, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
```

> Note: It's not recommended to add a large amount of rules to a single path (has limitation, also leads to bad performance).

We've also provided an example: [sentinel-demo-zookeeper-datasource](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-zookeeper-datasource).