# Sentinel DataSource Eureka

Sentinel DataSource Eureka provides integration with [Eureka](https://github.com/Netflix/eureka) so that Eureka
can be the dynamic rule data source of Sentinel.

To use Sentinel DataSource Eureka, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-eureka</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `EurekaDataSource` and register to rule managers.

SDK usage:

```java
EurekaDataSource<List<FlowRule>> eurekaDataSource = new EurekaDataSource("app-id", "instance-id",
        Arrays.asList("http://localhost:8761/eureka", "http://localhost:8762/eureka", "http://localhost:8763/eureka"),
        "rule-key", flowRuleParser);
FlowRuleManager.register2Property(eurekaDataSource.getProperty());
```

Example for Spring Cloud Application:

```java
@Bean
public EurekaDataSource<List<FlowRule>> eurekaDataSource(EurekaInstanceConfig eurekaInstanceConfig, EurekaClientConfig eurekaClientConfig) {

    List<String> serviceUrls = EndpointUtils.getServiceUrlsFromConfig(eurekaClientConfig,
            eurekaInstanceConfig.getMetadataMap().get("zone"), eurekaClientConfig.shouldPreferSameZoneEureka());

    EurekaDataSource<List<FlowRule>> eurekaDataSource = new EurekaDataSource(eurekaInstanceConfig.getAppname(),
            eurekaInstanceConfig.getInstanceId(), serviceUrls, "flowrules", new Converter<String, List<FlowRule>>() {
        @Override
        public List<FlowRule> convert(String o) {
            return JSON.parseObject(o, new TypeReference<List<FlowRule>>() {
            });
        }
    });

    FlowRuleManager.register2Property(eurekaDataSource.getProperty());
    return eurekaDataSource;
}

```

To refresh the rule dynamically, you need to call [Eureka-REST-operations](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations)
to update instance metadata:

```
PUT /eureka/apps/{appID}/{instanceID}/metadata?{ruleKey}={json of the rules}
```

Note: don't forget to encode your JSON string in the url.