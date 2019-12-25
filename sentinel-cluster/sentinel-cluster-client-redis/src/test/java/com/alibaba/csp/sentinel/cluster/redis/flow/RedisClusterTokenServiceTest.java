package com.alibaba.csp.sentinel.cluster.redis.flow;

import com.alibaba.csp.sentinel.cluster.redis.RedisClusterTokenService;
import com.alibaba.csp.sentinel.cluster.redis.config.*;
import com.alibaba.csp.sentinel.cluster.redis.jedis.*;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;
import com.alibaba.csp.sentinel.node.IntervalProperty;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisClusterTokenServiceTest {
    private static JedisCluster jedisCluster;


    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        initRedisConf();
        initJedisClient();
        initRule();
        checkRule("100");

        List<FlowRule> rules = new ArrayList<>();
        rules.add(new FlowRule("base-service")
                .setCount(5)
                .setClusterMode(true)
                .setClusterConfig(new ClusterFlowConfig().setFlowId(100L).setSampleCount(4)));
        FlowRuleManager.loadRules(rules);

        RedisClusterTokenService tokenService = new RedisClusterTokenService();

        IntervalProperty.INTERVAL = 2000;

        long st = System.currentTimeMillis();

        for(int  i = 0; i < 100; i++) {
            tokenService.requestToken(100L, 1, false);
            checkRedis(st);
            Thread.sleep(100);
        }

        System.out.println("#################################");
        Thread.sleep(1000 * 2);


        for(int  i = 0; i < 100; i++) {
            tokenService.requestToken(100L, 1, false);

            checkRedis(st);

            Thread.sleep(100);
        }


        /*
        ExecutorService service = Executors.newFixedThreadPool(8);

        for(int  i = 0; i < 500; i++) {
            service.execute(() -> {
                tokenService.requestToken(100L, 1, false);
                checkRedis(pool,st);
            });
        }

        Thread.sleep(1000 * 2);
        System.out.println("=====================================");
        for(int  i = 0; i < 5000; i++) {
            service.execute(() -> {
                tokenService.requestToken(100L, 1, false);
                checkRedis(pool,st);
            });
        }
        service.shutdown();*/

    }

    private static void initJedisClient() throws NoSuchFieldException, IllegalAccessException {

        JedisClusterClientFactory factory = (JedisClusterClientFactory) RedisClientFactoryManager.getFactory();

            Field field = factory.getClass().getDeclaredField("jedisCluster");
            field.setAccessible(true);
            jedisCluster = (JedisCluster) field.get(factory);


    }

    private static void checkRedis(long st) {

        Map<String, String> buckets = jedisCluster.hgetAll(ClientConstants.FLOW_CHECKER_TOKEN_KEY + "{100}");


        for (Map.Entry<String, String> entry : buckets.entrySet()) {
            System.out.print("(" + (System.currentTimeMillis() - st)/1000 + ":" +  entry.getKey() + " - " + entry.getValue() + ")     ");
        }
        System.out.println();

    }


    private static void initRedisConf() {
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

    private static void initRule() {
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
    }

    private static void checkRule(String... flowIds) throws NoSuchFieldException, IllegalAccessException {
        System.out.println("-------  start  --------");


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
