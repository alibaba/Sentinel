package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisFlowRuleManager {
    public static volatile boolean publishRuleToRedis = true;

    private static Map<String, List<FlowRule>> flowRules = new HashMap<String, List<FlowRule>>();
    private static Map<Long, FlowRule> flowIdToRule = new HashMap();
    private static final RedisFlowPropertyListener LISTENER = new RedisFlowPropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();

    static {
        currentProperty.addListener(LISTENER);
    }

    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[FlowRuleManager] Registering new property to flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    public static void loadRules(List<FlowRule> rules) {
        currentProperty.updateValue(rules);
    }

    private static void updateRule(List<FlowRule> confRules) {
        RedisClientFactory factory = RedisClientFactoryManager.getFactory();
        if (factory == null) {
            RecordLog.warn(
                    "[RedisFlowRuleManager]  cannot get RedisClientFactory, please init redis config");
            return;
        }

        RedisClient redisClient = factory.getClient();

        confRules = confRules == null ? new ArrayList<FlowRule>() : confRules;
        Map<Long, FlowRule> oldRules = RedisFlowRuleManager.flowIdToRule;
        updateLocaleRule(confRules);
        resetRedisRuleAndMetrics(oldRules, redisClient);
        clearInvalidRuleAndMetrics(oldRules, redisClient);

        redisClient.close();
    }

    private static void updateLocaleRule(List<FlowRule> confRule) {
        RedisFlowRuleManager.flowIdToRule = new ConcurrentHashMap<>();

        for (FlowRule rule : confRule) {
            if(!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }

            RedisFlowRuleManager.flowIdToRule.put(rule.getClusterConfig().getFlowId(), rule);
        }

        RedisFlowRuleManager.flowRules = FlowRuleUtil.buildFlowRuleMap(new ArrayList<FlowRule>(RedisFlowRuleManager.flowIdToRule.values()));
    }

    private static void resetRedisRuleAndMetrics(Map<Long, FlowRule> oldRules, RedisClient redisClient) {
        if(!RedisFlowRuleManager.publishRuleToRedis) {
            return ;
        }

        for (FlowRule rule : RedisFlowRuleManager.flowIdToRule.values()) {
            FlowRule existRule = oldRules.get(rule.getClusterConfig().getFlowId());
            if (existRule != null && !isChangeRule(existRule, rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] would not publish to redis on same flow rule: " + rule);
                continue;
            }
            redisClient.resetRedisRuleAndMetrics(rule);
        }
    }

    private static void clearInvalidRuleAndMetrics(Map<Long, FlowRule> oldRules, RedisClient redisClient) {
        Set<Long> deleteFlowIds = new HashSet<>();
        for (Map.Entry<Long, FlowRule> oldRuleEntry : oldRules.entrySet()) {
            if(!RedisFlowRuleManager.flowIdToRule.containsKey(oldRuleEntry.getKey())) {
                deleteFlowIds.add(oldRuleEntry.getKey());
            }
        }
        redisClient.clearRuleAndMetrics(deleteFlowIds);
    }

    private static boolean isChangeRule(FlowRule oldRule, FlowRule newRule) {
        ClusterFlowConfig oldClusterConfig = oldRule.getClusterConfig();
        ClusterFlowConfig newClusterConfig = newRule.getClusterConfig();

        return !oldClusterConfig.getFlowId().equals(newClusterConfig.getFlowId())
                || oldClusterConfig.getSampleCount() != newClusterConfig.getSampleCount()
                || oldClusterConfig.getWindowIntervalMs() != newClusterConfig.getWindowIntervalMs()
                || Double.doubleToLongBits(oldRule.getCount()) != Double.doubleToLongBits(newRule.getCount());
    }

    public static FlowRule getFlowRule(long flowId) {
        return flowIdToRule.get(flowId);
    }

    public static Map<String, List<FlowRule>> getFlowRuleMap() {
        return flowRules;
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
