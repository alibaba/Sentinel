package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import com.alibaba.csp.sentinel.cluster.redis.config.RedisProcessorFactoryManager;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;
import java.util.Map;


import static com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants.*;

public class SingleJedisTestProcessor implements RedisTestProcessor {
    private Jedis jedis;

    @Override
    public void initRedisConf() throws NoSuchFieldException, IllegalAccessException {
        ClusterClientConfig clientConfig = ClusterClientConfig.ofSingle(new HostAndPort("192.168.56.102", 6379))
                .setMaxActive(100)
                .setConnectTimeout(2000);
        ClusterClientConfigManager.applyNewConfig(clientConfig);

        JedisProcessorFactory factory = (JedisProcessorFactory) RedisProcessorFactoryManager.getFactory();

        Field field = JedisProcessorFactory.class.getDeclaredField("pool");
        field.setAccessible(true);
        jedis = ((JedisPool) field.get(factory)).getResource();
    }

    @Override
    public FlowRule getRule(String namespace, Long flowId) {

        Map<String, String> map = jedis.hgetAll(LuaUtil.toConfigKey(namespace, flowId));
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
        return jedis.hgetAll(LuaUtil.toTokenKey(namespace, flowId));
    }
}
