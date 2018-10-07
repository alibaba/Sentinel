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

import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link RedisConnectionConfig}.
 *
 * @author tiger
 */
public class RedisConnectionConfigTest {

    @Test
    public void testRedisDefaultPropertySuccess() {
        String host = "localhost";
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redis(host).build();
        Assert.assertEquals(host, redisConnectionConfig.getHost());
        Assert.assertEquals(RedisConnectionConfig.DEFAULT_REDIS_PORT, redisConnectionConfig.getPort());
        Assert.assertEquals(RedisConnectionConfig.DEFAULT_TIMEOUT_MILLISECONDS, redisConnectionConfig.getTimeout());
    }

    @Test
    public void testRedisClientNamePropertySuccess() {
        String host = "localhost";
        String clientName = "clientName";
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redis(host)
            .withClientName("clientName")
            .build();
        Assert.assertEquals(redisConnectionConfig.getClientName(), clientName);
    }

    @Test
    public void testRedisTimeOutPropertySuccess() {
        String host = "localhost";
        long timeout = 70 * 1000;
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redis(host)
            .withTimeout(timeout)
            .build();
        Assert.assertEquals(redisConnectionConfig.getTimeout(), timeout);
    }

    @Test
    public void testRedisSentinelDefaultPortSuccess() {
        String host = "localhost";
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redisSentinel(host)
            .withPassword("211233")
            .build();
        Assert.assertNull(redisConnectionConfig.getHost());
        Assert.assertEquals(1, redisConnectionConfig.getRedisSentinels().size());
        Assert.assertEquals(RedisConnectionConfig.DEFAULT_SENTINEL_PORT,
            redisConnectionConfig.getRedisSentinels().get(0).getPort());
    }

    @Test
    public void testRedisSentinelMoreThanOneServerSuccess() {
        String host = "localhost";
        String host2 = "server2";
        int port2 = 1879;
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redisSentinel(host)
            .withRedisSentinel(host2, port2)
            .build();
        Assert.assertNull(redisConnectionConfig.getHost());
        Assert.assertEquals(2, redisConnectionConfig.getRedisSentinels().size());
    }

    @Test
    public void testRedisSentinelMoreThanOneDuplicateServerSuccess() {
        String host = "localhost";
        String host2 = "server2";
        int port2 = 1879;
        RedisConnectionConfig redisConnectionConfig = RedisConnectionConfig.Builder.redisSentinel(host)
            .withRedisSentinel(host2, port2)
            .withRedisSentinel(host2, port2)
            .withPassword("211233")
            .build();
        Assert.assertNull(redisConnectionConfig.getHost());
        Assert.assertEquals(3, redisConnectionConfig.getRedisSentinels().size());
    }
}
