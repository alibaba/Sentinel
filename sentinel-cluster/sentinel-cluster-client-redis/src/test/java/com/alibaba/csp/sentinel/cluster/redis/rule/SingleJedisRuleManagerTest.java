package com.alibaba.csp.sentinel.cluster.redis.rule;

import com.alibaba.csp.sentinel.cluster.redis.RedisTestProcessor;
import com.alibaba.csp.sentinel.cluster.redis.SingleJedisTestProcessor;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import junit.framework.TestCase;
import org.junit.Test;


import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleJedisRuleManagerTest {
//    @Test
    public void testRedisFlowRuleManager() throws NoSuchFieldException, IllegalAccessException {
        RedisTestProcessor testProcessor = new SingleJedisTestProcessor();

        testProcessor.initRedisConf();

        String namespace = "namespace";
        String sourceName = "base-service";
        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(2).setWindowIntervalMs(1000)));
        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(102L).setSampleCount(4).setWindowIntervalMs(2000)));

        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(103L).setSampleCount(10).setWindowIntervalMs(5000)));
        RedisFlowRuleManager.registerNamespace(namespace);
        RedisFlowRuleManager.loadRules(namespace, rules);

        FlowRule rule = testProcessor.getRule(namespace, 101L);
        assertEquals(rule.getClusterConfig().getSampleCount(), 2);
        rule = testProcessor.getRule(namespace, 102L);
        assertEquals(rule.getClusterConfig().getSampleCount(), 4);
        Set<Long> flowIds = getFlowIdBySource(sourceName);
        assertEquals(flowIds.size(), 3);
        assertTrue(flowIds.contains(101L));
        assertTrue(flowIds.contains(102L));
        assertTrue(flowIds.contains(103L));

        rules = new ArrayList<>();
        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(4).setWindowIntervalMs(1000)));
        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(102L).setSampleCount(8).setWindowIntervalMs(2000)));

        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(104L).setSampleCount(10).setWindowIntervalMs(500)));
        RedisFlowRuleManager.loadRules(namespace, rules);
        rule = testProcessor.getRule(namespace, 101L);
        assertEquals(rule.getClusterConfig().getSampleCount(), 4);
        rule = testProcessor.getRule(namespace, 102L);
        assertEquals(rule.getClusterConfig().getSampleCount(), 8);
        rule = testProcessor.getRule(namespace, 103L);
        assertEquals(rule, null);

        flowIds = getFlowIdBySource(sourceName);
        assertEquals(flowIds.size(), 3);
        assertTrue(flowIds.contains(101L));
        assertTrue(flowIds.contains(102L));
        assertTrue(flowIds.contains(104L));

        String namespace2 = "namespace2";
        RedisFlowRuleManager.registerNamespace(namespace2);
        rules.clear();

        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(10).setWindowIntervalMs(1000)));

        rules.add(new FlowRule(sourceName)
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(105L).setSampleCount(10).setWindowIntervalMs(500)));
        RedisFlowRuleManager.loadRules(namespace2, rules);

        rule = testProcessor.getRule(namespace, 101L);
        assertEquals(rule.getClusterConfig().getSampleCount(), 4);
        flowIds = getFlowIdBySource(sourceName);
        assertEquals(flowIds.size(), 4);
        TestCase.assertTrue(flowIds.contains(101L));
        TestCase.assertTrue(flowIds.contains(102L));
        TestCase.assertTrue(flowIds.contains(104L));
        TestCase.assertTrue(flowIds.contains(105L));
    }

    public Set<Long> getFlowIdBySource(String sourceName) {
        List<FlowRule> rules = RedisFlowRuleManager.getFlowRuleMap().get(sourceName);
        Set<Long> flowIds = new HashSet<>();
        for (FlowRule rule : rules) {
            flowIds.add(rule.getClusterConfig().getFlowId());
        }
        return flowIds;
    }
}
