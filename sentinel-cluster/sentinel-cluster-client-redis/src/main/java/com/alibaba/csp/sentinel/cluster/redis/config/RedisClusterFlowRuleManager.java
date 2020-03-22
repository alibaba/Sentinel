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

public class RedisClusterFlowRuleManager {
    public static volatile boolean publishRuleToRedis = true;

    private static Map<String, List<FlowRule>> sourceToFlowRules = new HashMap<>();
    private static Map<Long, FlowRule> flowIdToRule = new HashMap();
    private static Map<Long, String> flowIdToNamespace = new HashMap<>();

    private static final Map<String, SentinelProperty<List<FlowRule>>> NAMESPACE_TO_PROPERTY = new HashMap<>();
    private static final Map<String, RedisClusterFlowPropertyListener> NAMESPACE_TO_LISTENER = new HashMap<>();

    public static void registerNamespace(String namespace) {
        register2Property(namespace, new DynamicSentinelProperty<List<FlowRule>>());
    }

    public static void register2Property(String namespace, SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (RedisClusterFlowRuleManager.class) {
            RecordLog.info("[RedisClusterFlowRuleManager] Registering new property to flow rule manager");
            RedisClusterFlowPropertyListener listener = RedisClusterFlowRuleManager.NAMESPACE_TO_LISTENER.get(namespace);
            if(listener == null) {
                listener = new RedisClusterFlowPropertyListener(namespace);
                RedisClusterFlowRuleManager.NAMESPACE_TO_LISTENER.put(namespace, listener);
            }
            property.addListener(listener);
            RedisClusterFlowRuleManager.NAMESPACE_TO_PROPERTY.remove(namespace);
            RedisClusterFlowRuleManager.NAMESPACE_TO_PROPERTY.put(namespace, property);
        }
    }

    public static void loadRules(String namespace, List<FlowRule> rules) {
        SentinelProperty<List<FlowRule>> property = RedisClusterFlowRuleManager.NAMESPACE_TO_PROPERTY.get(namespace);
        if(property == null) {
            RecordLog.warn(
                    "[RedisClusterFlowRuleManager] namespace not register");
            return;
        }
        property.updateValue(rules);
    }

    private static void updateRule(String namespace, List<FlowRule> newRules) {
        RedisProcessorFactory factory = RedisProcessorFactoryManager.getFactory();
        if (factory == null) {
            RecordLog.warn(
                    "[RedisClusterFlowRuleManager]  cannot get RedisProcessorFactory, please init redis config");
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
                        "[RedisClusterFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            String existNamespace = flowIdToNamespace.get(rule.getClusterConfig().getFlowId());
            if(existNamespace != null && !namespace.equals(existNamespace)) {
                RecordLog.warn(
                        "[RedisClusterFlowRuleManager] Ignoring flowId exist in other namespace: " + rule);
                continue;
            }
            validRules.add(rule);
        }
        return validRules;
    }

    private static void resetRedisRuleAndMetrics(String namespace, List<FlowRule> newRules, RedisProcessor redisProcessor) {
        if(!RedisClusterFlowRuleManager.publishRuleToRedis) {
            return ;
        }
        for (FlowRule rule : newRules) {
            FlowRule existRule = RedisClusterFlowRuleManager.flowIdToRule.get(rule.getClusterConfig().getFlowId());
            if (existRule != null && !isChangeRule(existRule, rule)) {
                RecordLog.warn(
                        "[RedisClusterFlowRuleManager] would not publish to redis on same flow rule: " + rule);
                continue;
            }
            redisProcessor.resetRedisRuleAndMetrics(namespace, rule);
        }
    }

    private static Set<Long> getExpiredFlowIds(String namespace, List<FlowRule> newRules) {
        Set<Long> expiredFlowIds = new HashSet<>();
        for (Map.Entry<Long, String> entry : RedisClusterFlowRuleManager.flowIdToNamespace.entrySet()) {
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

        Map<Long, FlowRule> newFlowIdToRule = new HashMap<>(RedisClusterFlowRuleManager.flowIdToRule.size());
        newFlowIdToRule.putAll(RedisClusterFlowRuleManager.flowIdToRule);
        Map<Long, String> newFlowIdToNamespace = new HashMap<>(RedisClusterFlowRuleManager.flowIdToNamespace.size());
        newFlowIdToNamespace.putAll(RedisClusterFlowRuleManager.flowIdToNamespace);
        Map<String, List<FlowRule>> newSourceToRules = new HashMap<>(RedisClusterFlowRuleManager.sourceToFlowRules.size());
        newSourceToRules.putAll(RedisClusterFlowRuleManager.sourceToFlowRules);
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

        RedisClusterFlowRuleManager.flowIdToRule = newFlowIdToRule;
        RedisClusterFlowRuleManager.flowIdToNamespace = newFlowIdToNamespace;
        RedisClusterFlowRuleManager.sourceToFlowRules = newSourceToRules;
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

    public static String getNamespace(long flowId) {
        return flowIdToNamespace.get(flowId);
    }

    public static Map<String, List<FlowRule>> getFlowRuleMap() {
        return sourceToFlowRules;
    }

    private static final class RedisClusterFlowPropertyListener implements PropertyListener<List<FlowRule>> {

        private String namespace;

        public RedisClusterFlowPropertyListener(String namespace) {
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
