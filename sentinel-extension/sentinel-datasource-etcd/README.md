# Sentinel DataSource Etcd

Sentinel DataSource Etcd provides integration with Etcd so that Etcd
can be the dynamic rule data source of Sentinel. The data source uses push model (watcher).

To use Sentinel DataSource Etcd, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-etcd</artifactId>
    <version>x.y.z</version>
</dependency>
```
Configure Etcd Connect Properties By Config File (for example sentinel.properties)

```
csp.sentinel.etcd.end.points=http://ip1:port1,http://ip2:port2
csp.sentinel.etcd.user=your_user
csp.sentinel.etcd.password=your_password
csp.sentinel.etcd.charset=your_charset
csp.sentinel.etcd.auth.enable=true //if ture open user/password or ssl check
csp.sentinel.etcd.authority=authority //ssl
```
or JVM args(Add -D prefix)


Then you can create an `EtcdDataSource` and register to rule managers.
For instance:

```java
  //`rule_key` is the rule config key
  ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(rule_key, (rule) -> JSON.parseArray(rule, FlowRule.class));
  FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());
```

> Note: It needs to update JDK version to JDK8


We've also provided an example: [sentinel-demo-etcd-datasource].