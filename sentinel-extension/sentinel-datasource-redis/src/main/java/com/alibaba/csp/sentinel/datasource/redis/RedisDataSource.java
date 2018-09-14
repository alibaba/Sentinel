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
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A read-only {@code DataSource} with Redis backend.
 * </p>
 * <p>
 * The data source first loads initial rules from a Redis String during initialization.
 * Then the data source subscribe from specific channel. When new rules is published to the channel,
 * the data source will observe the change in realtime and update to memory.
 * </p>
 * <p>
 * Note that for consistency, users should publish the value and save the value to the ruleKey simultaneously
 * like this (using Redis transaction):
 * <pre>
 *  MULTI
 *  SET ruleKey value
 *  PUBLISH channel value
 *  EXEC
 * </pre>
 * </p>
 *
 * @author tiger
 */
public class RedisDataSource<T> extends AbstractDataSource<String, T> {

    private final RedisClient redisClient;

    private final String ruleKey;

    /**
     * Constructor of {@code RedisDataSource}.
     *
     * @param connectionConfig Redis connection config
     * @param ruleKey          data key in Redis
     * @param channel          channel to subscribe in Redis
     * @param parser           customized data parser, cannot be empty
     */
    public RedisDataSource(RedisConnectionConfig connectionConfig, String ruleKey, String channel,
                           Converter<String, T> parser) {
        super(parser);
        AssertUtil.notNull(connectionConfig, "Redis connection config can not be null");
        AssertUtil.notEmpty(ruleKey, "Redis ruleKey can not be empty");
        AssertUtil.notEmpty(channel, "Redis subscribe channel can not be empty");
        this.redisClient = getRedisClient(connectionConfig);
        this.ruleKey = ruleKey;
        loadInitialConfig();
        subscribeFromChannel(channel);
    }

    /**
     * Build Redis client fromm {@code RedisConnectionConfig}.
     *
     * @return a new {@link RedisClient}
     */
    private RedisClient getRedisClient(RedisConnectionConfig connectionConfig) {
        if (connectionConfig.getRedisSentinels().size() == 0) {
            RecordLog.info("[RedisDataSource] Creating stand-alone mode Redis client");
            return getRedisStandaloneClient(connectionConfig);
        } else {
            RecordLog.info("[RedisDataSource] Creating Redis Sentinel mode Redis client");
            return getRedisSentinelClient(connectionConfig);
        }
    }

    private RedisClient getRedisStandaloneClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder redisUriBuilder = RedisURI.builder();
        redisUriBuilder.withHost(connectionConfig.getHost())
            .withPort(connectionConfig.getPort())
            .withDatabase(connectionConfig.getDatabase())
            .withTimeout(Duration.ofMillis(connectionConfig.getTimeout()));
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
    public String readSource() {
        if (this.redisClient == null) {
            throw new IllegalStateException("Redis client has not been initialized or error occurred");
        }
        RedisCommands<String, String> stringRedisCommands = redisClient.connect().sync();
        return stringRedisCommands.get(ruleKey);
    }

    @Override
    public void close() {
        redisClient.shutdown();
    }

    private class DelegatingRedisPubSubListener extends RedisPubSubAdapter<String, String> {

        DelegatingRedisPubSubListener() {
        }

        @Override
        public void message(String channel, String message) {
            RecordLog.info(String.format("[RedisDataSource] New property value received for channel %s: %s", channel, message));
            getProperty().updateValue(parser.convert(message));
        }
    }
}
