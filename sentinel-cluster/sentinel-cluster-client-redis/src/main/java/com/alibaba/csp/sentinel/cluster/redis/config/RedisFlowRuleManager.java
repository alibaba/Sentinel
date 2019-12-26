package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RedisFlowRuleManager {
    public static volatile boolean publishRuleToRedis = true;
    private static Map<Long, FlowRule> flowRules = new ConcurrentHashMap();
    private static final RedisFlowPropertyListener LISTENER = new RedisFlowPropertyListener();

    public static void addRedisFlowRuleListener() {
        FlowRuleManager.addListener(LISTENER);
    }

    public static FlowRule getFlowRule(long flowId) {
        return flowRules.get(flowId);
    }

    private static void updateRule(List<FlowRule> flowRules) {
        RedisClientFactory factory = RedisClientFactoryManager.getFactory();
        if (factory == null) {
            RecordLog.warn(
                    "[RedisFlowRuleManager]  cannot get RedisClientFactory, please init redis config");
            return;
        }

        Map<Long, FlowRule> oldRules = RedisFlowRuleManager.flowRules;
        RedisClient redisClient = factory.getClient();
        publishFlowRule(flowRules, oldRules, redisClient);
        clearFlowRule(oldRules, redisClient);

        redisClient.close();
    }

    private static void publishFlowRule(List<FlowRule> flowRules, Map<Long, FlowRule> oldRules, RedisClient redisClient) {
        Map<Long, FlowRule> newRules = new ConcurrentHashMap();
        if(flowRules == null) {
            RedisFlowRuleManager.flowRules = newRules;
            return;
        }

        Set<Long> resetFlowMetricsIds = new HashSet<>();
        for (FlowRule rule : flowRules) {
            if(!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }

            newRules.put(rule.getClusterConfig().getFlowId(), rule);
            if(RedisFlowRuleManager.publishRuleToRedis) {
                FlowRule existRule = oldRules.get(rule.getClusterConfig().getFlowId());
                if (existRule != null && !isChangeRule(existRule, rule)) {
                    RecordLog.warn(
                            "[RedisFlowRuleManager] not publish to redis on same flow rule: " + rule);
                    continue;
                }
                redisClient.publishRule(rule);
                resetFlowMetricsIds.add(rule.getClusterConfig().getFlowId());
            }
        }
        redisClient.resetFlowMetrics(resetFlowMetricsIds);
        RedisFlowRuleManager.flowRules = newRules;
    }

    private static void clearFlowRule(Map<Long, FlowRule> oldRules, RedisClient redisClient) {
        Set<Long> deleteFlowIds = new HashSet<>();
        for (Map.Entry<Long, FlowRule> oldRuleEntry : oldRules.entrySet()) {
            if(!RedisFlowRuleManager.flowRules.containsKey(oldRuleEntry.getKey())) {
                deleteFlowIds.add(oldRuleEntry.getKey());
            }
        }
        redisClient.clearRule(deleteFlowIds);
    }

    private static boolean isChangeRule(FlowRule oldRule, FlowRule newRule) {
        ClusterFlowConfig oldClusterConfig = oldRule.getClusterConfig();
        ClusterFlowConfig newClusterConfig = newRule.getClusterConfig();

        return !oldClusterConfig.getFlowId().equals(newClusterConfig.getFlowId())
                || oldClusterConfig.getSampleCount() != newClusterConfig.getSampleCount()
                || oldClusterConfig.getWindowIntervalMs() != newClusterConfig.getWindowIntervalMs()
                || Double.doubleToLongBits(oldRule.getCount()) != Double.doubleToLongBits(newRule.getCount());
    }

    private static final class RedisFlowPropertyListener implements PropertyListener<List<FlowRule>> {

        @Override
        public synchronized void configUpdate(List<FlowRule> flowRules) {
            updateRule(flowRules);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> flowRules) {
            updateRule(flowRules);
        }
    }
}
