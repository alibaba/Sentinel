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

    private static Map<String, List<FlowRule>> SOURCE_TO_FLOW_RULES = new HashMap<>();
    private static Map<Long, FlowRule> FLOW_ID_TO_RULE = new HashMap();
    private static Map<Long, String> FLOW_ID_TO_NAMESPACE = new HashMap<>();

    private static final Map<String, SentinelProperty<List<FlowRule>>> NAMESPACE_TO_PROPERTY = new HashMap<>();
    private static final Map<String, RedisFlowPropertyListener> NAMESPACE_TO_LISTENER = new HashMap<>();

    public static void registerNamespace(String namespace) {
        register2Property(namespace, new DynamicSentinelProperty<List<FlowRule>>());
    }

    public static void register2Property(String namespace, SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (RedisFlowRuleManager.class) {
            RecordLog.info("[RedisFlowRuleManager] Registering new property to flow rule manager");
            RedisFlowPropertyListener listener = RedisFlowRuleManager.NAMESPACE_TO_LISTENER.get(namespace);
            if(listener == null) {
                listener = new RedisFlowPropertyListener(namespace);
                RedisFlowRuleManager.NAMESPACE_TO_LISTENER.put(namespace, listener);
            }
            property.addListener(listener);
            RedisFlowRuleManager.NAMESPACE_TO_PROPERTY.remove(namespace);
            RedisFlowRuleManager.NAMESPACE_TO_PROPERTY.put(namespace, property);
        }
    }

    public static void loadRules(String namespace, List<FlowRule> rules) {
        SentinelProperty<List<FlowRule>> property = RedisFlowRuleManager.NAMESPACE_TO_PROPERTY.get(namespace);
        if(property == null) {
            RecordLog.warn(
                    "[RedisFlowRuleManager] namespace not register");
            return;
        }
        property.updateValue(rules);
    }

    private static void updateRule(String namespace, List<FlowRule> newRules) {
        RedisProcessorFactory factory = RedisProcessorFactoryManager.getFactory();
        if (factory == null) {
            RecordLog.warn(
                    "[RedisFlowRuleManager]  cannot get RedisProcessorFactory, please init redis config");
            return;
        }

        newRules = clearInvalidRule(namespace, newRules);
        RedisProcessor redisProcessor = factory.getProcessor();
        resetRedisRuleAndMetrics(namespace, newRules, redisProcessor);
        clearRedisExpiredRuleAndMetrics(namespace, newRules, redisProcessor);
        updateLocaleRule(namespace, newRules);
        redisProcessor.close();
    }

    private static List<FlowRule> clearInvalidRule(String namespace, List<FlowRule> newRules) {
        if(newRules == null) {
            return new ArrayList<>();
        }

        List<FlowRule> validRules = new ArrayList<>();
        for (FlowRule rule : newRules) {
            if(!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            String existNamespace = FLOW_ID_TO_NAMESPACE.get(rule.getClusterConfig().getFlowId());
            if(existNamespace != null && !namespace.equals(existNamespace)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] flowId exist in other namespace: " + rule);
                continue;
            }
            validRules.add(rule);
        }
        return validRules;
    }

    private static void resetRedisRuleAndMetrics(String namespace, List<FlowRule> newRules, RedisProcessor redisProcessor) {
        if(!RedisFlowRuleManager.publishRuleToRedis) {
            return ;
        }
        for (FlowRule rule : newRules) {
            FlowRule existRule = RedisFlowRuleManager.FLOW_ID_TO_RULE.get(rule.getClusterConfig().getFlowId());
            if (existRule != null && !isChangeRule(existRule, rule)) {
                RecordLog.warn(
                        "[RedisFlowRuleManager] would not publish to redis on same flow rule: " + rule);
                continue;
            }
            redisProcessor.resetRedisRuleAndMetrics(namespace, rule);
        }
    }

    private static Set<Long> getExpiredFlowIds(String namespace, List<FlowRule> newRules) {
        Set<Long> expiredFlowIds = new HashSet<>();
        for (Map.Entry<Long, String> entry : RedisFlowRuleManager.FLOW_ID_TO_NAMESPACE.entrySet()) {
            if(namespace.equals(entry.getValue())) {
                expiredFlowIds.add(entry.getKey());
            }
        }

        Set<Long> newFlowIds = new HashSet<>();
        for (FlowRule newRule : newRules) {
            newFlowIds.add(newRule.getClusterConfig().getFlowId());
        }
        expiredFlowIds.removeAll(newFlowIds);
        return expiredFlowIds;
    }

    private static void clearRedisExpiredRuleAndMetrics(String namespace, List<FlowRule> newRules, RedisProcessor redisProcessor) {
        Set<Long> expiredFlowIds = getExpiredFlowIds(namespace, newRules);
        redisProcessor.clearRuleAndMetrics(namespace, expiredFlowIds);
    }

    private static void updateLocaleRule(String namespace, List<FlowRule> newRules) {
        Set<Long> expiredFlowIds = getExpiredFlowIds(namespace, newRules);

        Map<Long, FlowRule> newFlowIdToRule = new HashMap<>();
        newFlowIdToRule.putAll(RedisFlowRuleManager.FLOW_ID_TO_RULE);
        Map<Long, String> newFlowIdToNamespace = new HashMap<>();
        newFlowIdToNamespace.putAll(RedisFlowRuleManager.FLOW_ID_TO_NAMESPACE);
        Map<String, List<FlowRule>> newSourceToRules = new HashMap<>();
        newSourceToRules.putAll(RedisFlowRuleManager.SOURCE_TO_FLOW_RULES);
        // clear expired rule
        for (Long oldFlowId : expiredFlowIds) {
            newFlowIdToNamespace.remove(oldFlowId);
            newFlowIdToRule.remove(oldFlowId);
        }
        // add new rule
        for (FlowRule rule : newRules) {
            newFlowIdToNamespace.put(rule.getClusterConfig().getFlowId(), namespace);
            newFlowIdToRule.put(rule.getClusterConfig().getFlowId(), rule);
        }
        // clear expired rule from sourceRules
        for (Map.Entry<String, List<FlowRule>> sourceRules : newSourceToRules.entrySet()) {
            Iterator<FlowRule>  iterator = sourceRules.getValue().iterator();
            while (iterator.hasNext()) {
                FlowRule rule = iterator.next();
                if(expiredFlowIds.contains(rule.getClusterConfig().getFlowId())) {
                    iterator.remove();
                }
            }
        }
        // add new rule to sourceRules
        Map<String, List<FlowRule>> addSourceFlowRuleMap = FlowRuleUtil.buildFlowRuleMap(new ArrayList<>(newRules));
        for (Map.Entry<String, List<FlowRule>> addSourceFlows : addSourceFlowRuleMap.entrySet()) {
            List<FlowRule> existSourceRules = newSourceToRules.get(addSourceFlows.getKey());
            if(existSourceRules == null) {
                newSourceToRules.put(addSourceFlows.getKey(), addSourceFlows.getValue());
            } else {
                existSourceRules.addAll(addSourceFlows.getValue());
            }
        }

        RedisFlowRuleManager.FLOW_ID_TO_RULE = newFlowIdToRule;
        RedisFlowRuleManager.FLOW_ID_TO_NAMESPACE = newFlowIdToNamespace;
        RedisFlowRuleManager.SOURCE_TO_FLOW_RULES = newSourceToRules;
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
        return FLOW_ID_TO_RULE.get(flowId);
    }

    public static String getNamespace(long flowId) {
        return FLOW_ID_TO_NAMESPACE.get(flowId);
    }

    public static Map<String, List<FlowRule>> getFlowRuleMap() {
        return SOURCE_TO_FLOW_RULES;
    }

    private static final class RedisFlowPropertyListener implements PropertyListener<List<FlowRule>> {

        private String namespace;

        public RedisFlowPropertyListener(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public synchronized void configUpdate(List<FlowRule> flowRules) {
            updateRule(namespace, flowRules);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> flowRules) {
            updateRule(namespace, flowRules);
        }
    }
}
