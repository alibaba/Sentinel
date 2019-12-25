package com.alibaba.csp.sentinel.cluster.redis.flow;

import org.junit.Test;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.redis.RedisClusterTokenService;
import static com.alibaba.csp.sentinel.cluster.redis.RedisClusterTestUtil.*;

public class RedisClusterTokenServiceChecker {

    @Test
    public void checkRequestTokenByRedisCluster() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        long flowId = 100L;
        int count = 5;
        int sampleCount = 4;
        int windowIntervalMs = 2000;

        initRedisConf();
        initJedisClient();
        initRule(flowId, count, sampleCount, windowIntervalMs);

        RedisClusterTokenService tokenService = new RedisClusterTokenService();

        for(int  i = 0; i < 100; i++) {
            TokenResult rs = tokenService.requestToken(100L, 1, false);

            checkRedis();
            System.out.println("status: " + rs.getStatus());
            Thread.sleep(100);
        }

        System.out.println("#################################");
        Thread.sleep(1000 * 2);

        for(int  i = 0; i < 100; i++) {
            TokenResult rs = tokenService.requestToken(100L, 1, false);

            checkRedis();
            System.out.println("status: " + rs.getStatus());
            Thread.sleep(100);
        }
    }
}
