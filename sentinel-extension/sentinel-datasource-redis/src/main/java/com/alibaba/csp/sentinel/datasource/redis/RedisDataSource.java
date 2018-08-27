package com.alibaba.csp.sentinel.datasource.redis;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.log.RecordLog;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;


/**
 * Redis data resource. when data publish in redis,this will update rule config in time
 *
 * @author moon tiger
 */

public class RedisDataSource<T> extends AbstractDataSource<String, T> {

    private RedisClient redisClient = null;

    private String ruleKey;

    public RedisDataSource(RedisClient client, String ruleKey, String channel, ConfigParser<String, T> parser) {
        super(parser);
        this.redisClient = client;
        this.ruleKey = ruleKey;
        initConfig();
        subscribeFromChannel(channel);
    }

    private void subscribeFromChannel(String channel) {
        StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();
        RedisPubSubAdapter<String, String> adapterListener = new DelegatingRedisPubSubListener();
        pubSubConnection.addListener(adapterListener);
        RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
        sync.subscribe(channel);
    }


    private void initConfig() {
        try {
            loadConfig();
        } catch (Exception e) {
            RecordLog.info("[RedisDataSource] Error when loading initial config", e);
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.redisClient == null) {
            throw new IllegalStateException("redis client has not been initialized or error occurred");
        }
        RedisCommands<String, String> stringRedisCommands = redisClient.connect().sync();
        String value = stringRedisCommands.get(ruleKey);
        if (value != null) {
            return value;
        }
        RecordLog.warn("rules is empty in redis. key:" + ruleKey);
        return null;
    }

    @Override
    public void close() throws Exception {
        redisClient.shutdown();
    }


    private class DelegatingRedisPubSubListener extends RedisPubSubAdapter<String, String> {

        DelegatingRedisPubSubListener() {
        }

        @Override
        public void message(String channel, String message) {
            getProperty().updateValue(parser.parse(message));
        }
    }


}
