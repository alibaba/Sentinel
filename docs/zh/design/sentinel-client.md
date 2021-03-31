# sentinel客户端

在[主流框架的适配](https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E6%B5%81%E6%A1%86%E6%9E%B6%E7%9A%84%E9%80%82%E9%85%8D)中，提到了[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

幸运的是，可以按照[动态数据源支持](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel#%E5%8A%A8%E6%80%81%E6%95%B0%E6%8D%AE%E6%BA%90%E6%94%AF%E6%8C%81)中描述的那样，直接使用apollo作为规则的数据源，客户端使用的代码，不需要修改

但是为了在使用上尽可能的方便，将sentinel控制台的地址等信息，放入Apollo的公共namespace中

在使用上最好可以达到一个效果，就是应用本身只需要多使用一个公共namespace，就可以直接接入sentinel的控制台

具体接入方法参考[使用/sentinel-客户端](zh/usage/sentinel-client)

## 公共namespace包含的信息

### sentinel控制台地址

```properties
spring.cloud.sentinel.transport.dashboard = xxx:8080
```

### project.name

```properties
project.name = ${app.id}
```

### datasource

有非常多种类型的流控规则，为了让客户端不用自行配置，在公共配置中，帮助所有应用提前配置好

以FLOW规则为例，也就是流控规则

可以配置如下

```properties
spring.cloud.sentinel.datasource.FLOW.apollo.namespace-name = ${sentinel.apollo.namespace-name}
spring.cloud.sentinel.datasource.FLOW.apollo.rule-type = FLOW
spring.cloud.sentinel.datasource.FLOW.apollo.flow-rules-key = ${project.name}${sentinel.apollo.suffix.FLOW}
spring.cloud.sentinel.datasource.FLOW.apollo.default-flow-rule-value = []
```

如果是降级规则，可以配置成

```properties
spring.cloud.sentinel.datasource.DEGRADE.apollo.namespace-name = ${sentinel.apollo.namespace-name}
spring.cloud.sentinel.datasource.DEGRADE.apollo.rule-type = DEGRADE
spring.cloud.sentinel.datasource.DEGRADE.apollo.flow-rules-key = ${project.name}${sentinel.apollo.suffix.DEGRADE}
spring.cloud.sentinel.datasource.DEGRADE.apollo.default-flow-rule-value = []
```

经过上述的配置，客户端在接入时，无需自己管理应该怎么进行配置，而是直接从公共配置中读取，直接使用，极大节约了开发在这方面需要耗费的精力

