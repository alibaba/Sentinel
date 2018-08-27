package com.alibaba.csp.sentinel.datasource.redis;

import ai.grakn.redismock.RedisServer;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class RedisDataSourceTest {

    private static RedisServer server = null;

    private RedisClient client;

    private String ruleKey = "sentinel.flow.rulekey";

    private String channel = "sentinel.flow.channel";

    @Before
    public void buildResource() {
        try {
            // bind to a random port
            server = RedisServer.newRedisServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigParser<String, List<FlowRule>> flowConfigParser = buildFlowConfigParser();
        client = RedisClient.create("redis://localhost");
        initRedisRuleData();
        DataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource<List<FlowRule>>(client, ruleKey, channel, flowConfigParser);
        FlowRuleManager.register2Property(redisDataSource.getProperty());
    }

    @Test
    public void pub_msg_and_receive_success() {
        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertTrue(rules.size() == 0);
        int maxQueueingTimeMs = 480;
        StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();
        String flowRules = "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, \"refResource\":null, " +
                "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":" + maxQueueingTimeMs + ", \"controller\":null}]";
        connection.sync().publish(channel, flowRules);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rules = FlowRuleManager.getRules();
        Assert.assertTrue(rules.get(0).getMaxQueueingTimeMs() == maxQueueingTimeMs);
    }


    @Test
    public void init_and_parse_flow_rule_success() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        String value = stringRedisCommands.get(ruleKey);
        List<FlowRule> flowRules = buildFlowConfigParser().parse(value);
        Assert.assertTrue(flowRules.size() == 1);
        stringRedisCommands.del(ruleKey);
    }

    @Test
    public void read_resource_fail() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        stringRedisCommands.del(ruleKey);
        String value = stringRedisCommands.get(ruleKey);
        Assert.assertTrue(value == null);
    }


    @After
    public void clearResource() {
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        stringRedisCommands.del(ruleKey);
        client.shutdown();
        server.stop();
        server = null;
    }

    private ConfigParser<String, List<FlowRule>> buildFlowConfigParser() {
        return new ConfigParser<String, List<FlowRule>>() {
            @Override
            public List<FlowRule> parse(String source) {
                return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                });
            }
        };
    }

    private void initRedisRuleData() {
        String flowRulesJson = "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, \"refResource\":null, " +
                "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":500, \"controller\":null}]";
        RedisCommands<String, String> stringRedisCommands = client.connect().sync();
        String ok = stringRedisCommands.set(ruleKey, flowRulesJson);
        Assert.assertTrue(ok.equals("OK"));
    }
}
