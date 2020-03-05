package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessor;
import com.alibaba.csp.sentinel.cluster.redis.RedisProcessorFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import java.util.*;

public class RedisFlowRuleManager {
    public static volatile boolean publishRuleToRedis = true;

    private static Map<String, List<FlowRule>> flowRules = new HashMap<>();
    private static Map<Long, FlowRule> flowIdToRule = new HashMap();
    private static final RedisFlowPropertyListener LISTENER = new RedisFlowPropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<>();

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
        RedisProcessorFactory factory = RedisProcessorFactoryManager.getFactory();
        if (factory == null) {
            RecordLog.warn(
                    "[RedisFlowRuleManager]  cannot get RedisProcessorFactory, please init redis config");
            return;
        }

        confRules = clearInvalidRule(confRules);
        RedisProcessor redisProcessor = factory.getProcessor();
        resetRedisRuleAndMetrics(confRules, redisProcessor);
        clearExpiredRuleAndMetrics(confRules, redisProcessor);
        updateLocaleRule(confRules);
        redisProcessor.close();
    }

    private static List<FlowRule> clearInvalidRule(List<FlowRule> confRules) {
        if(confRules == null) {
            return new ArrayList<>();
        }

        List<FlowRule> validRules = new ArrayList<>();
        for (FlowRule rule : confRules) {
            if(!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            validRules.add(rule);
        }
        return validRules;
    }

    private static void resetRedisRuleAndMetrics(List<FlowRule> confRules, RedisProcessor redisProcessor) {
        if(!RedisFlowRuleManager.publishRuleToRedis) {
            return ;
        }
        for (FlowRule rule : confRules) {
            FlowRule existRule = RedisFlowRuleManager.flowIdToRule.get(rule.getClusterConfig().getFlowId());
            if (existRule != null && !isChangeRule(existRule, rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] would not publish to redis on same flow rule: " + rule);
                continue;
            }
            redisProcessor.resetRedisRuleAndMetrics(rule);
        }
    }

    private static void clearExpiredRuleAndMetrics(List<FlowRule> confRules, RedisProcessor redisProcessor) {
        Set<Long> newFlowIds = new HashSet<>();
        for (FlowRule confRule : confRules) {
            newFlowIds.add(confRule.getClusterConfig().getFlowId());
        }
        Set<Long> deleteFlowIds = new HashSet<>();
        for (Map.Entry<Long, FlowRule> oldRule : RedisFlowRuleManager.flowIdToRule.entrySet()) {
            if(!newFlowIds.contains(oldRule.getKey())) {
                deleteFlowIds.add(oldRule.getKey());
            }
        }
        redisProcessor.clearRuleAndMetrics(deleteFlowIds);
    }

    private static void updateLocaleRule(List<FlowRule> confRule) {
        RedisFlowRuleManager.flowIdToRule.clear();
        for (FlowRule rule : confRule) {
            RedisFlowRuleManager.flowIdToRule.put(rule.getClusterConfig().getFlowId(), rule);
        }
        RedisFlowRuleManager.flowRules = FlowRuleUtil.buildFlowRuleMap(new ArrayList<FlowRule>(RedisFlowRuleManager.flowIdToRule.values()));
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
