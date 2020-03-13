package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisProcessorFactoryManager;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClusterProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;

public class ClusterJedisTestProcessor implements RedisTestProcessor {
    private static volatile JedisCluster jedisCluster;

    @Override
    public void initRedisConf() throws NoSuchFieldException, IllegalAccessException{
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

        JedisClusterProcessorFactory factory = (JedisClusterProcessorFactory) RedisProcessorFactoryManager.getFactory();

        Field field = JedisClusterProcessorFactory.class.getDeclaredField("jedisCluster");
        field.setAccessible(true);
        jedisCluster = (JedisCluster) field.get(factory);
    }

    @Override
    public FlowRule getRule(String namespace, Long flowId) {
        Map<String, String> map = jedisCluster.hgetAll(LuaUtil.toConfigKey(namespace, flowId));
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

    @Override
    public Map<String, String> getBucketCount(String namespace, Long flowId) {
        Map<String, String> map = jedisCluster.hgetAll(LuaUtil.toTokenKey(namespace, flowId));

        return map;
    }
}
