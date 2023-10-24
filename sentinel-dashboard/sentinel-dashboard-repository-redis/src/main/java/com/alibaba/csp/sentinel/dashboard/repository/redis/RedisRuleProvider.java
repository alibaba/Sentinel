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
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRuleProvider;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class RedisRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Autowired
    private RedisClient redisClient;

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        String ruleKey = buildRuleKey(app, ip, port);
        String rules = syncCommands.get(ruleKey);
        connection.close();
        return rules;
    }
}
