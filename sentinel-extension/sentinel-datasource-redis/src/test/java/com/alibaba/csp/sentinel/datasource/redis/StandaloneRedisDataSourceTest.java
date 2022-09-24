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

import ai.grakn.redismock.RedisServer;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Redis stand-alone mode test cases for {@link RedisDataSource}.
 *
 * @author tiger
 */
public class StandaloneRedisDataSourceTest {

    private static RedisServer server = null;

    private RedisClient client;

    private String ruleKey = "sentinel.rules.flow.ruleKey";
    private String channel = "sentinel.rules.flow.channel";

    @Before
    public void buildResource() {
        try {
            // Bind to a random port.
            server = RedisServer.newRedisServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Converter<String, List<FlowRule>> flowConfigParser = buildFlowConfigParser();
        client = RedisClient.create(RedisURI.create(server.getHost(), server.getBindPort()));
        RedisConnectionConfig config = RedisConnectionConfig.builder()
            .withHost(server.getHost())
            .withPort(server.getBindPort())
            .build();
        initRedisRuleData();
        ReadableDataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource<List<FlowRule>>(config,
            ruleKey, channel, flowConfigParser);
        FlowRuleManager.register2Property(redisDataSource.getProperty());
    }

    @Test
    public void testPubMsgAndReceiveSuccess() {
        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertEquals(1, rules.size());
        int maxQueueingTimeMs = new Random().nextInt();
        StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();
        String flowRules =
            "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, "
                + "\"refResource\":null, "
                +
                "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":" + maxQueueingTimeMs
                + ", \"controller\":null}]";
        RedisPubSubCommands<String, String> subCommands = connection.sync();
        subCommands.multi();
        subCommands.set(ruleKey, flowRules);
        subCommands.publish(channel, flowRules);
        subCommands.exec();

        await().timeout(2, TimeUnit.SECONDS)
            .until(new Callable<List<FlowRule>>() {
                @Override
                public List<FlowRule> call() throws Exception {
                    return FlowRuleManager.getRules();
                }
            }, Matchers.hasSize(1));

        rules = FlowRuleManager.getRules();
        Assert.assertEquals(rules.get(0).getMaxQueueingTimeMs(), maxQueueingTimeMs);
        String value = subCommands.get(ruleKey);
        List<FlowRule> flowRulesValuesInRedis = buildFlowConfigParser().convert(value);
        Assert.assertEquals(flowRulesValuesInRedis.size(), 1);
        Assert.assertEquals(flowRulesValuesInRedis.get(0).getMaxQueueingTimeMs(), maxQueueingTimeMs);
    }

    @Test
    public void testInitAndParseFlowRuleSuccess() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        String value = stringRedisCommands.get(ruleKey);
        List<FlowRule> flowRules = buildFlowConfigParser().convert(value);
        Assert.assertEquals(flowRules.size(), 1);
        stringRedisCommands.del(ruleKey);
    }

    @Test
    public void testReadResourceFail() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        stringRedisCommands.del(ruleKey);
        String value = stringRedisCommands.get(ruleKey);
        Assert.assertNull(value);
    }

    @After
    public void clearResource() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        stringRedisCommands.del(ruleKey);
        client.shutdown();
        server.stop();
        server = null;
    }

    private Converter<String, List<FlowRule>> buildFlowConfigParser() {
        return source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
    }

    private void initRedisRuleData() {
        String flowRulesJson =
            "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, "
                + "\"refResource\":null, "
                +
                "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":500, \"controller\":null}]";
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        String ok = stringRedisCommands.set(ruleKey, flowRulesJson);
        Assert.assertEquals(ok, "OK");
    }
}
