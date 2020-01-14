package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.redis.config.*;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClusterClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RedisClusterTestUtil {

    public static void initRedisConf() {
        List<HostAndPort> clusterNodes = new ArrayList<>();
        clusterNodes.add(new HostAndPort("192.168.56.102", 7000));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7001));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7002));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7003));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7004));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7005));
        ClusterClientConfig clientConfig = ClusterClientConfig.ofCluster(clusterNodes)
                .setMaxActive(100)
                .setConnectTimeout(2000);
        ClusterClientConfigManager.applyNewConfig(clientConfig);

    }

    public static void initRule(long flowId, int count, int sampleCount, int windowIntervalMs) {
        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("redis-client-cluster-test")
                .setCount(count)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(flowId).setSampleCount(sampleCount).setWindowIntervalMs(windowIntervalMs)));
        RedisFlowRuleManager.loadRules(rules);
    }

    private static volatile JedisCluster jedisCluster;
    public static JedisCluster initJedisClient() throws NoSuchFieldException, IllegalAccessException {
        if(jedisCluster == null) {
            synchronized (RedisClusterTestUtil.class) {
                if(jedisCluster == null) {
                    JedisClusterClientFactory factory = (JedisClusterClientFactory) RedisClientFactoryManager.getFactory();

                    Field field = JedisClusterClientFactory.class.getDeclaredField("jedisCluster");
                    field.setAccessible(true);
                    jedisCluster = (JedisCluster) field.get(factory);
                }
            }
        }
        return jedisCluster;
    }

    public static void checkRedis() {
        Map<String, String> buckets = jedisCluster.hgetAll(ClientConstants.FLOW_CHECKER_TOKEN_KEY + "{100}");

        for (Map.Entry<String, String> entry : buckets.entrySet()) {
            System.out.print("(" +  entry.getKey() + ":" + entry.getValue() + ")     ");
        }
        System.out.println();
    }




}
