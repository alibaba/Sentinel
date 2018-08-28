# Sentinel DataSource Redis

Sentinel DataSource Redis provides integration with Redis so that Redis
can be the dynamic rule data source of Sentinel. The data source uses push model (listener) with redis pub/sub feature.

this RedisDataSource implement only Redis Standalone. if you want to use redis cluster. please read this [Redis Cluster PUB/SUB](https://github.com/lettuce-io/lettuce-core/wiki/Pub-Sub),
 then you can implement by yourself.

To use Sentinel DataSource Redis, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-redis</artifactId>
    <version>x.y.z</version>
</dependency>

```

Then you can create an `RedisDataSource` and register to rule managers.
For instance:

```java

DataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource<List<FlowRule>>(client, ruleKey, channel, flowConfigParser);
FlowRuleManager.register2Property(redisDataSource.getProperty());
```

*client* : we use [lettuce](https://lettuce.io/) as redis-cli client. you can build client this way [how client build](https://github.com/lettuce-io/lettuce-core/wiki/Basic-usage) . 

*ruleKey* : when the json rule data publish. it also should save to the key for init read.

*channel* : the channel to listen.  

you can also create multi data source listen for different rule type. 

you can run test case for usage.