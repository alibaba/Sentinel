# sentinel客户端

在[主流框架的适配](https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E6%B5%81%E6%A1%86%E6%9E%B6%E7%9A%84%E9%80%82%E9%85%8D)中，提到了[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

幸运的是，可以按照[动态数据源支持](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel#%E5%8A%A8%E6%80%81%E6%95%B0%E6%8D%AE%E6%BA%90%E6%94%AF%E6%8C%81)中描述的那样，直接使用apollo作为规则的数据源，客户端使用的代码，不需要修改
