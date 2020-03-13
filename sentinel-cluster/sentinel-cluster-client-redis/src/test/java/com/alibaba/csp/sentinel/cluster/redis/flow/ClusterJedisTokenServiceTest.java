package com.alibaba.csp.sentinel.cluster.redis.flow;

import com.alibaba.csp.sentinel.cluster.redis.ClusterJedisTestProcessor;
import com.alibaba.csp.sentinel.cluster.redis.RedisClusterTokenService;
import com.alibaba.csp.sentinel.cluster.redis.RedisTestProcessor;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


import static com.alibaba.csp.sentinel.cluster.redis.RedisFlowTestUtil.assertResultBlock;
import static com.alibaba.csp.sentinel.cluster.redis.RedisFlowTestUtil.assertResultPass;
import static org.junit.Assert.assertEquals;

public class ClusterJedisTokenServiceTest {
//    @Test
    public void testRequestTokenByRedisCluster() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        long flowId = 100L;
        int count = 5;
        int sampleCount = 2;
        int windowIntervalMs = 1000;

        RedisTestProcessor testProcessor = new ClusterJedisTestProcessor();
        testProcessor.initRedisConf();

        String namespace = "ClusterJedisTokenServiceTest";
        RedisFlowRuleManager.registerNamespace(namespace);
        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("hello")
                .setCount(count)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(flowId).setSampleCount(sampleCount).setWindowIntervalMs(windowIntervalMs)));
        RedisFlowRuleManager.loadRules(namespace, rules);

        int bucketLength = windowIntervalMs / sampleCount;

        RedisClusterTokenService tokenService = new RedisClusterTokenService();

        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        Thread.sleep(bucketLength);
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultBlock(tokenService.requestToken(flowId, 1, false));
        assertResultBlock(tokenService.requestToken(flowId, 1, false));

        Thread.sleep(bucketLength);
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        Thread.sleep(bucketLength);
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultPass(tokenService.requestToken(flowId, 1, false));
        assertResultBlock(tokenService.requestToken(flowId, 1, false));
        assertResultBlock(tokenService.requestToken(flowId, 1, false));

        assertEquals(testProcessor.getBucketCount(namespace, flowId).size(), 2);

    }



}

