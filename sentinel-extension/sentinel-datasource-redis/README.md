# Sentinel DataSource Redis

Sentinel DataSource Redis provides integration with Redis. The data source leverages Redis pub-sub feature to implement push model (listener).

The data source uses [Lettuce](https://lettuce.io/) as the Redis client internal. Requires JDK 1.8 or later.

> **NOTE**: Currently we do not support Redis Cluster now.

## Usage

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

- `redisConnectionConfig`: use `RedisConnectionConfig` class to build your Redis connection config
- `ruleKey`: the rule persistence key of a Redis String
- `channel`: the channel to subscribe

You can also create multi data sources to subscribe for different rule type.

Note that the data source first loads initial rules from a Redis String (provided `ruleKey`) during initialization.
So for consistency, users should publish the value and save the value to the `ruleKey` simultaneously like this (using Redis transaction):

```
MULTI
SET ruleKey value
PUBLISH channel value
EXEC
```

An example using Lettuce Redis client:

```java
public <T> void pushRules(List<T> rules, Converter<List<T>, String> encoder) {
    StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();
    RedisPubSubCommands<String, String> subCommands = connection.sync();
    String value = encoder.convert(rules);
    subCommands.multi();
    subCommands.set(ruleKey, value);
    subCommands.publish(ruleChannel, value);
    subCommands.exec();
}
```

## How to build RedisConnectionConfig

### Build with Redis standalone mode

```java
RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withHost("localhost")
                .withPort(6379)
                .withPassword("pwd")
                .withDataBase(2)
                .build();

```

### Build with Redis Sentinel mode

```java
RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withRedisSentinel("redisSentinelServer1",5000)
                .withRedisSentinel("redisSentinelServer2",5001)
                .withRedisSentinelMasterId("redisSentinelMasterId").build();
```
