/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.datasource.redis;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.datasource.redis.util.AssertUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.concurrent.TimeUnit;

/**
 * A read-only {@code DataSource} with Redis backend.
 * <p>
 * When data source init,reads form redis with string k-v,value is string format rule config data.
 * This data source subscribe from specific channel and then data published in redis with this channel,data source
 * will be notified and then update rule config in time.
 * </p>
 *
 * @author tiger
 */

public class RedisDataSource<T> extends AbstractDataSource<String, T> {

    private RedisClient redisClient = null;

    private String ruleKey;

    /**
     * Constructor of {@code RedisDataSource}
     *
     * @param connectionConfig redis connection config.
     * @param ruleKey          data save in redis.
     * @param channel          subscribe from channel.
     * @param parser           convert <code>ruleKey<code>`s value to {@literal alibaba/Sentinel} rule type
     */
    public RedisDataSource(RedisConnectionConfig connectionConfig, String ruleKey, String channel, Converter<String, T> parser) {
        super(parser);
        AssertUtil.notNull(connectionConfig, "redis connection config can not be null");
        AssertUtil.notEmpty(ruleKey, "redis subscribe ruleKey can not be empty");
        AssertUtil.notEmpty(channel, "redis subscribe channel can not be empty");
        this.redisClient = getRedisClient(connectionConfig);
        this.ruleKey = ruleKey;
        loadInitialConfig();
        subscribeFromChannel(channel);
    }

    /**
     * build redis client form {@code RedisConnectionConfig} with io.lettuce.
     *
     * @return a new {@link RedisClient}
     */
    private RedisClient getRedisClient(RedisConnectionConfig connectionConfig) {
        if (connectionConfig.getRedisSentinels().size() == 0) {
            RecordLog.info("start standLone mode to connect to redis");
            return getRedisStandLoneClient(connectionConfig);
        } else {
            RecordLog.info("start redis sentinel mode to connect to redis");
            return getRedisSentinelClient(connectionConfig);
        }
    }

    private RedisClient getRedisStandLoneClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder redisUriBuilder = RedisURI.builder();
        redisUriBuilder.withHost(connectionConfig.getHost())
                .withPort(connectionConfig.getPort())
                .withDatabase(connectionConfig.getDatabase())
                .withTimeout(connectionConfig.getTimeout(), TimeUnit.MILLISECONDS);
        if (password != null) {
            redisUriBuilder.withPassword(connectionConfig.getPassword());
        }
        if (StringUtil.isNotEmpty(connectionConfig.getClientName())) {
            redisUriBuilder.withClientName(clientName);
        }
        return RedisClient.create(redisUriBuilder.build());
    }

    private RedisClient getRedisSentinelClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder sentinelRedisUriBuilder = RedisURI.builder();
        for (RedisConnectionConfig config : connectionConfig.getRedisSentinels()) {
            sentinelRedisUriBuilder.withSentinel(config.getHost(), config.getPort());
        }
        if (password != null) {
            sentinelRedisUriBuilder.withPassword(connectionConfig.getPassword());
        }
        if (StringUtil.isNotEmpty(connectionConfig.getClientName())) {
            sentinelRedisUriBuilder.withClientName(clientName);
        }
        sentinelRedisUriBuilder.withSentinelMasterId(connectionConfig.getRedisSentinelMasterId())
                .withTimeout(connectionConfig.getTimeout(), TimeUnit.MILLISECONDS);
        return RedisClient.create(sentinelRedisUriBuilder.build());
    }

    private void subscribeFromChannel(String channel) {
        StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();
        RedisPubSubAdapter<String, String> adapterListener = new DelegatingRedisPubSubListener();
        pubSubConnection.addListener(adapterListener);
        RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
        sync.subscribe(channel);
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[RedisDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[RedisDataSource] Error when loading initial config", ex);
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.redisClient == null) {
            throw new IllegalStateException("redis client has not been initialized or error occurred");
        }
        RedisCommands<String, String> stringRedisCommands = redisClient.connect().sync();
        return stringRedisCommands.get(ruleKey);
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
            RecordLog.info(String.format("[RedisDataSource] New property value received for channel %s: %s",  channel, message));
            getProperty().updateValue(parser.convert(message));
        }
    }

}
