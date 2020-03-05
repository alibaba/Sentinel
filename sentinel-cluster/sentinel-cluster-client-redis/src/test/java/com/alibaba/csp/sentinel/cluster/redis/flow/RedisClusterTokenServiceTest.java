package com.alibaba.csp.sentinel.cluster.redis.flow;

import com.alibaba.csp.sentinel.cluster.redis.RedisClusterTokenService;
import org.junit.Test;

import static com.alibaba.csp.sentinel.cluster.redis.RedisClusterTestUtil.*;
import static com.alibaba.csp.sentinel.cluster.redis.RedisFlowTestUtil.assertResultBlock;
import static com.alibaba.csp.sentinel.cluster.redis.RedisFlowTestUtil.assertResultPass;

public class RedisClusterTokenServiceTest {
//    @Test
    public void testRequestTokenByRedisCluster() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        long flowId = 100L;
        int count = 5;
        int sampleCount = 2;
        int windowIntervalMs = 1000;

        initRedisConf();
        initJedisClient();
        initRule(flowId, count, sampleCount, windowIntervalMs);
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
    }



}

