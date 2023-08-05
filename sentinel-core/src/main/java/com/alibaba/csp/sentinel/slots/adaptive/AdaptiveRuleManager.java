package com.alibaba.csp.sentinel.slots.adaptive;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ElonTusk
 * @name AdaptiveRuleManager
 * @date 2023/8/2 14:08
 */
public class AdaptiveRuleManager {
    private static volatile Map<String, AdaptiveRule> adaptiveRules = new ConcurrentHashMap<>();

    private static final AdaptivePropertyListener LISTENER = new AdaptivePropertyListener();

    private static SentinelProperty<List<AdaptiveRule>> currentProperty = new DynamicSentinelProperty<>();

    private static AdaptiveListener adaptiveListener = null;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-adaptive-limiter-task", true));

    static {
        adaptiveListener = new AdaptiveListener();
        scheduler.scheduleAtFixedRate(adaptiveListener, 0, 1, TimeUnit.SECONDS);
        currentProperty.addListener(LISTENER);
    }

    public static Map<String, AdaptiveRule> getAdaptiveRules() {
        return adaptiveRules;
    }

    public static void adaptiveLimit(ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized) {
        if (resourceWrapper == null) {
            return;
        }
        if (resourceWrapper.getEntryType() != EntryType.IN) {
            return;
        }

        AdaptiveRule adaptiveRule = adaptiveRules.get(resourceWrapper.getName());
        int times = adaptiveRule.incrementTimes();
        if (times > RuleConstant.ADAPTIVE_LIMIT_THRESHOLD) {
            AdaptiveLimiter.adaptiveLimit(adaptiveRule, node);
        }
    }

    public static void loadRules(List<AdaptiveRule> rules) {
        for (AdaptiveRule rule : rules) {
            adaptiveRules.put(rule.getResource(), rule);
        }
    }


    private static final class AdaptivePropertyListener implements PropertyListener<List<AdaptiveRule>> {

        @Override
        public void configUpdate(List<AdaptiveRule> value) {
            if (value != null) {
                adaptiveRules = AdaptiveRuleUtil.buildAdaptiveRuleMap(value);
                RecordLog.info("[AdaptiveRuleManager] Adaptive rule updated: {}", value);
            } else {
                RecordLog.info("[AdaptiveRuleManager] Adaptive rule update failed");
            }
        }

        @Override
        public void configLoad(List<AdaptiveRule> value) {
            if (value != null) {
                adaptiveRules = AdaptiveRuleUtil.buildAdaptiveRuleMap(value);
                RecordLog.info("[AdaptiveRuleManager] Adaptive rule loaded: {}", value);
            } else {
                RecordLog.info("[AdaptiveRuleManager] Adaptive rule load failed");
            }
        }
    }

}
