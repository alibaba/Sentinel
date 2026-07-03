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
import io.lettuce.core.SslOptions;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    private final RedisClusterClient redisClusterClient;

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
        if (connectionConfig.getRedisClusters().size() == 0) {
            this.redisClient = getRedisClient(connectionConfig);
            this.redisClusterClient = null;
        } else {
            this.redisClusterClient = getRedisClusterClient(connectionConfig);
            this.redisClient = null;
        }
        this.ruleKey = ruleKey;
        loadInitialConfig();
        subscribeFromChannel(channel);
    }

    /**
     * init SslOptions, support jks or pem format
     *
     * @param connectionConfig Redis connection config
     * @return a new SslOptions
     */
    private SslOptions initSslOptions(RedisConnectionConfig connectionConfig) {
        if (!connectionConfig.isSslEnable()){
            return null;
        }

        SslOptions.Builder sslOptionsBuilder = SslOptions.builder();

        if (connectionConfig.getTrustedCertificatesPath() != null){
            if (connectionConfig.getTrustedCertificatesPath().endsWith(".jks")){
                // if the value is end with .jks，think it is java key store format，to invoke truststore method
                sslOptionsBuilder.truststore(
                        new File(connectionConfig.getTrustedCertificatesPath()),
                        connectionConfig.getTrustedCertificatesJksPassword()
                );
            } else {
                // if the value is not end with .jks，think it is pem format，to invoke trustManager method
                sslOptionsBuilder.trustManager(new File(connectionConfig.getTrustedCertificatesPath()));
            }
        }

        if (connectionConfig.getKeyCertChainFilePath() != null || connectionConfig.getKeyFilePath() != null) {
            if (connectionConfig.getKeyFilePath().endsWith(".jks")){
                sslOptionsBuilder.keystore(
                        new File(connectionConfig.getKeyCertChainFilePath()),
                        connectionConfig.getKeyFilePassword() == null ? null : connectionConfig.getKeyFilePassword().toCharArray()
                );
            } else {
                sslOptionsBuilder.keyManager(
                        new File(connectionConfig.getKeyCertChainFilePath()),
                        new File(connectionConfig.getKeyFilePath()),
                        connectionConfig.getKeyFilePassword() == null ? null : connectionConfig.getKeyFilePassword().toCharArray()
                );
            }
        }
        return sslOptionsBuilder.build();
    }

    /**
     * Build Redis client fromm {@code RedisConnectionConfig}.
     *
     * @return a new {@link RedisClient}
     */
    private RedisClient getRedisClient(RedisConnectionConfig connectionConfig) {
        RedisClient redisClient;
        if (connectionConfig.getRedisSentinels().size() == 0) {
            RecordLog.info("[RedisDataSource] Creating stand-alone mode Redis client");
            redisClient = getRedisStandaloneClient(connectionConfig);
        } else {
            RecordLog.info("[RedisDataSource] Creating Redis Sentinel mode Redis client");
            redisClient = getRedisSentinelClient(connectionConfig);
        }
        SslOptions sslOptions = initSslOptions(connectionConfig);
        if (sslOptions != null){
            redisClient.setOptions(
                    ClusterClientOptions.builder().sslOptions(sslOptions).build()
            );
        }
        return redisClient;
    }

    private RedisClusterClient getRedisClusterClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();

        //If any uri is successful for connection, the others are not tried anymore
        List<RedisURI> redisUris = new ArrayList<>();
        for (RedisConnectionConfig config : connectionConfig.getRedisClusters()) {
            RedisURI.Builder clusterRedisUriBuilder = RedisURI.builder();
            clusterRedisUriBuilder.withHost(config.getHost())
                .withPort(config.getPort())
                .withSsl(config.isSslEnable())
                .withTimeout(Duration.ofMillis(connectionConfig.getTimeout()));
            //All redis nodes must have same password
            if (password != null) {
                clusterRedisUriBuilder.withPassword(connectionConfig.getPassword());
            }
            redisUris.add(clusterRedisUriBuilder.build());
        }
        RedisClusterClient redisClusterClient =  RedisClusterClient.create(redisUris);
        SslOptions sslOptions = initSslOptions(connectionConfig);
        if (sslOptions != null){
            redisClusterClient.setOptions(
                    ClusterClientOptions.builder().sslOptions(sslOptions).build()
            );
        }
        return redisClusterClient;
    }


    private RedisClient getRedisStandaloneClient(RedisConnectionConfig connectionConfig) {
        char[] password = connectionConfig.getPassword();
        String clientName = connectionConfig.getClientName();
        RedisURI.Builder redisUriBuilder = RedisURI.builder();
        redisUriBuilder.withHost(connectionConfig.getHost())
            .withPort(connectionConfig.getPort())
            .withDatabase(connectionConfig.getDatabase())
            .withSsl(connectionConfig.isSslEnable())
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
            .withSsl(connectionConfig.isSslEnable())
            .withTimeout(Duration.ofMillis(connectionConfig.getTimeout()));
        return RedisClient.create(sentinelRedisUriBuilder.build());
    }

    private void subscribeFromChannel(String channel) {
        RedisPubSubAdapter<String, String> adapterListener = new DelegatingRedisPubSubListener();
        if (redisClient != null) {
            StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();
            pubSubConnection.addListener(adapterListener);
            RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
            sync.subscribe(channel);
        } else {
            StatefulRedisClusterPubSubConnection<String, String> pubSubConnection = redisClusterClient.connectPubSub();
            pubSubConnection.addListener(adapterListener);
            RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
            sync.subscribe(channel);
        }
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
        if (this.redisClient == null && this.redisClusterClient == null) {
            throw new IllegalStateException("Redis client or Redis Cluster client has not been initialized or error occurred");
        }

        if (redisClient != null) {
            RedisCommands<String, String> stringRedisCommands = redisClient.connect().sync();
            return stringRedisCommands.get(ruleKey);
        } else {
            RedisAdvancedClusterCommands<String, String> stringRedisCommands = redisClusterClient.connect().sync();
            return stringRedisCommands.get(ruleKey);
        }
    }

    @Override
    public void close() {
        if (redisClient != null) {
            redisClient.shutdown();
        } else {
            redisClusterClient.shutdown();
        }

    }

    private class DelegatingRedisPubSubListener extends RedisPubSubAdapter<String, String> {

        DelegatingRedisPubSubListener() {
        }

        @Override
        public void message(String channel, String message) {
            RecordLog.info("[RedisDataSource] New property value received for channel {}: {}", channel, message);
            getProperty().updateValue(parser.convert(message));
        }
    }
}
