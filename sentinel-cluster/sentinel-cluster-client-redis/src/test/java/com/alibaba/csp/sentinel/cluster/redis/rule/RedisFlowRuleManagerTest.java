package com.alibaba.csp.sentinel.cluster.redis.rule;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.redis.config.*;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClusterClient;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisFlowRuleManagerTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        ClusterStateManager.setToClient();

//        List<HostAndPort> sentinels = new ArrayList<>();
//        sentinels.add(new HostAndPort("192.168.56.102", 7003));
//        sentinels.add(new HostAndPort("192.168.56.102", 7004));
//        sentinels.add(new HostAndPort("192.168.56.102", 7005));
//        ClusterClientConfig clientConfig = ClusterClientConfig.ofSentinel(sentinels, "mymaster")
//                .setMaxActive(30)
//                .setConnectTimeout(2000);
//        ClusterClientConfigManager.applyNewConfig(clientConfig);


        List<HostAndPort> clusterNodes = new ArrayList<>();
        clusterNodes.add(new HostAndPort("192.168.56.102", 7000));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7001));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7002));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7003));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7004));
        clusterNodes.add(new HostAndPort("192.168.56.102", 7005));
        ClusterClientConfig clientConfig = ClusterClientConfig.ofCluster(clusterNodes)
                .setMaxActive(30)
                .setConnectTimeout(2000);
        ClusterClientConfigManager.applyNewConfig(clientConfig);

        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("base-service")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(100L).setSampleCount(2).setWindowIntervalMs(1000)));
        rules.add(new FlowRule("base-service")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(101L).setSampleCount(4).setWindowIntervalMs(2000)));

        rules.add(new FlowRule("base-service")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(102L).setSampleCount(10).setWindowIntervalMs(5000)));
        FlowRuleManager.loadRules(rules);
        checkRule("100","101","102");

        rules = new ArrayList<>();
        rules.add(new FlowRule("base-service")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(100L).setSampleCount(2)));
        FlowRuleManager.loadRules(rules);
        checkRule("100","101","102");
    }

    private static void checkRule(String... flowIds) throws NoSuchFieldException, IllegalAccessException {
        System.out.println("-------  start  --------");
        JedisClusterClient client = (JedisClusterClient) RedisClientFactoryManager.getFactory().getClient();
        Field field = JedisClusterClient.class.getDeclaredField("jedisCluster");
        field.setAccessible(true);
        JedisCluster jedisCluster = (JedisCluster) field.get(client);

        for (String flowId : flowIds) {
            System.out.print(flowId + " --> ");
            Map<String, String> result = jedisCluster.hgetAll(LuaUtil.toLuaParam(ClientConstants.FLOW_RULE_CONFIG_KEY, flowId));
            for (Map.Entry<String, String> entry : result.entrySet()) {
                System.out.print(entry.getKey() + ":" + entry.getValue() + "    ");
            }
            System.out.println();
        }

        System.out.println("-------  end  --------");
    }
}
