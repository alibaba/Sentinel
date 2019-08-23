package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Liu Yiming
 * @date 2019-07-14 22:28
 */
public final class AdaptiveRuleManager {

    private static final Map<String, Set<AdaptiveRule>> adaptiveRules = new ConcurrentHashMap<String, Set<AdaptiveRule>>();

    private static final RulePropertyListener LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<AdaptiveRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }


    public static Set<AdaptiveRule> getRules(ResourceWrapper resource) {
        return adaptiveRules.get(resource.getName());
    }

    public static void loadRules(List<AdaptiveRule> rules) {
        try{
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.warn("[AdaptiveRuleManager] Unexpected error when loading adaptive rules", e);
        }
    }

    private static class RulePropertyListener implements PropertyListener<List<AdaptiveRule>> {

        @Override
        public void configUpdate(List<AdaptiveRule> conf) {
            Map<String, Set<AdaptiveRule>> rules = AdaptiveRuleUtil.buildAdaptiveRuleMap(conf);
            if (rules != null) {
                adaptiveRules.clear();
                adaptiveRules.putAll(rules);
            }
            RecordLog.info("[AdaptiveRuleManager] Adaptive rules received: " + adaptiveRules);
        }

        @Override
        public void configLoad(List<AdaptiveRule> conf) {
            Map<String, Set<AdaptiveRule>> rules = AdaptiveRuleUtil.buildAdaptiveRuleMap(conf);
            if (rules != null) {
                adaptiveRules.clear();
                adaptiveRules.putAll(rules);
            }
            RecordLog.info("[AdaptiveRuleManager] Adaptive rules received: " + adaptiveRules);
        }
    }
}
