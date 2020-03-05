package com.alibaba.csp.sentinel.cluster.redis.rule;

import com.alibaba.csp.sentinel.cluster.redis.config.RedisFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.csp.sentinel.cluster.redis.RedisClusterTestUtil.initJedisClient;
import static com.alibaba.csp.sentinel.cluster.redis.RedisClusterTestUtil.initRedisConf;
import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;
import static org.junit.Assert.assertEquals;

public class RedisFlowRuleManagerTest {
//    @Test
    public void testRedisFlowRuleManager() throws NoSuchFieldException, IllegalAccessException {
        initRedisConf();

        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("base-service1")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(2).setWindowIntervalMs(1000)));
        rules.add(new FlowRule("base-service2")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(102L).setSampleCount(4).setWindowIntervalMs(2000)));

        rules.add(new FlowRule("base-service3")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(103L).setSampleCount(10).setWindowIntervalMs(5000)));
        RedisFlowRuleManager.loadRules(rules);
        FlowRule rule = getRule(101);
        assertEquals(rule.getClusterConfig().getSampleCount(), 2);
        rule = getRule(102);
        assertEquals(rule.getClusterConfig().getSampleCount(), 4);

        rules = new ArrayList<>();
        rules.add(new FlowRule("base-service1")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(4).setWindowIntervalMs(1000)));
        rules.add(new FlowRule("base-service2")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(102L).setSampleCount(8).setWindowIntervalMs(2000)));
        RedisFlowRuleManager.loadRules(rules);
        rule = getRule(101);
        assertEquals(rule.getClusterConfig().getSampleCount(), 4);
        rule = getRule(102);
        assertEquals(rule.getClusterConfig().getSampleCount(), 8);
        rule = getRule(103);
        assertEquals(rule, null);
    }

    public FlowRule getRule(long flowId) throws NoSuchFieldException, IllegalAccessException {
        JedisCluster jedisCluster = initJedisClient();
        Map<String, String> map = jedisCluster.hgetAll(LuaUtil.toLuaParam(FLOW_RULE_CONFIG_KEY, flowId));
        if(map == null || map.isEmpty())
            return null;

        FlowRule rule = new FlowRule();
        rule.setCount(Double.parseDouble(map.get(THRESHOLD_COUNT_KEY)));

        ClusterFlowConfig clusterConfig = new ClusterFlowConfig();
        clusterConfig.setSampleCount(Integer.parseInt(map.get(SAMPLE_COUNT_KEY)));
        clusterConfig.setWindowIntervalMs(Integer.parseInt(map.get(INTERVAL_IN_MS_KEY)));
        rule.setClusterConfig(clusterConfig);
        return rule;
    }
}
