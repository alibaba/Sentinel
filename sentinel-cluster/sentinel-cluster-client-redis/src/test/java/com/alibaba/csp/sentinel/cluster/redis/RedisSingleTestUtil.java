package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.config.*;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisProcessorFactory;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RedisSingleTestUtil {
    public static void initRedisConf() {
        ClusterClientConfig clientConfig = ClusterClientConfig.ofSingle(new HostAndPort("192.168.56.102", 6379))
                .setMaxActive(100)
                .setConnectTimeout(2000);
        ClusterClientConfigManager.applyNewConfig(clientConfig);
    }

    private static volatile Jedis jedis;
    public static Jedis initJedisClient() throws NoSuchFieldException, IllegalAccessException {
        if(jedis == null) {
            synchronized (RedisClusterTestUtil.class) {
                if(jedis == null) {
                    JedisProcessorFactory factory = (JedisProcessorFactory) RedisProcessorFactoryManager.getFactory();

                    Field field = JedisProcessorFactory.class.getDeclaredField("pool");
                    field.setAccessible(true);
                    jedis = ((JedisPool) field.get(factory)).getResource();
                }
            }
        }
        return jedis;
    }


    public static void initRule(long flowId, int count, int sampleCount, int windowIntervalMs) {
        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("redis-client-cluster-test")
                .setCount(count)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(flowId).setSampleCount(sampleCount).setWindowIntervalMs(windowIntervalMs)));
        RedisFlowRuleManager.loadRules(rules);
    }
}
