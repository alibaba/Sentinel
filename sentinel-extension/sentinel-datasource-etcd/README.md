# Sentinel DataSource Etcd

Sentinel DataSource Etcd provides integration with etcd so that etcd
can be the dynamic rule data source of Sentinel. The data source uses push model (watcher).

To use Sentinel DataSource Etcd, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-etcd</artifactId>
    <version>x.y.z</version>
</dependency>
```

We could configure Etcd connection configuration by config file (for example `sentinel.properties`):

```
csp.sentinel.etcd.endpoints=http://ip1:port1,http://ip2:port2
csp.sentinel.etcd.user=your_user
csp.sentinel.etcd.password=your_password
csp.sentinel.etcd.charset=your_charset
csp.sentinel.etcd.auth.enable=true # if ture, then open user/password or ssl check
csp.sentinel.etcd.authority=authority # ssl
```

Or we could configure via JVM -D args or via `SentinelConfig.setConfig(key, value)`.

Then we can create an `EtcdDataSource` and register to rule managers. For instance:

```java
// `rule_key` is the rule config key
ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(rule_key, (rule) -> JSON.parseArray(rule, FlowRule.class));
FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());
```

We've also provided an example: [sentinel-demo-etcd-datasource](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-etcd-datasource)