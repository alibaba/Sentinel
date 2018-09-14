# Sentinel DataSource Redis

Sentinel DataSource Redis provides integration with Redis. make Redis
as dynamic rule data source of Sentinel. The data source uses push model (listener) with redis pub/sub feature.

**NOTE**:
we not support redis cluster as a pub/sub dataSource now.

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
ReadableDataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource<List<FlowRule>>(redisConnectionConfig, ruleKey, channel, flowConfigParser);
FlowRuleManager.register2Property(redisDataSource.getProperty());
```

_**redisConnectionConfig**_ : use `RedisConnectionConfig` class to build your connection config. 

_**ruleKey**_ : when the json rule data publish. it also should save to the key for init read.

_**channel**_ : the channel to listen.  

you can also create multi data source listen for different rule type. 

you can see test cases for usage.

## Before start

RedisDataSource init config by read from redis key `ruleKey`, value store the latest rule config data. 
so you should first config your redis ruleData in back end. 

since update redis rule data. it should simultaneously send data to `channel`.

you may implement like this (using Redis transaction):

```

MULTI
PUBLISH channel value
SET ruleKey value
EXEC

``` 


## How to build RedisConnectionConfig


### Build with redis standLone mode

```java
RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withHost("localhost")
                .withPort(6379)
                .withPassword("pwd")
                .withDataBase(2)
                .build();

```


### Build with redis sentinel mode

```java
RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withRedisSentinel("redisSentinelServer1",5000)
                .withRedisSentinel("redisSentinelServer2",5001)
                .withRedisSentinelMasterId("redisSentinelMasterId").build();
```
