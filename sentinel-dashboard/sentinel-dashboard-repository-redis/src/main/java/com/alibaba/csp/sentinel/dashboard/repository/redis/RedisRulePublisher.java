/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.redis;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class RedisRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

    @Autowired
    private RedisConfig redisConfig;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        /**
         * Note:
         * By default channelSuffix is "", channel is same as ruleKey.
         * Pay attention to the constructor parameter of {@link com.alibaba.csp.sentinel.datasource.redis.RedisDataSource},
         * ruleKey and channel should be same.
         */
        String channel = ruleKey + redisProperties.getChannelSuffix();

        RedisClient redisClient = redisConfig.createRedisClient();

        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        RedisPubSubCommands<String, String> subCommands = connection.sync();

        subCommands.multi();
        subCommands.set(ruleKey, rules);
        subCommands.publish(channel, rules);
        subCommands.exec();

        connection.close();
        redisClient.shutdown();
    }
}
