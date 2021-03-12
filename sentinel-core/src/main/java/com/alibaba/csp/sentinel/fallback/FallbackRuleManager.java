package com.alibaba.csp.sentinel.fallback;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Return fallback config when blocked:
 * <ol>
 * <li>requests from specified caller</li>
 * <li>no specified caller</li>
 * </ol>
 * </p>
 *
 * @ClassName: FallbackRuleManager
 * @Author Mason.MA
 * @Package com.alibaba.csp.sentinel.fallback
 * @Date 2021/3/10 14:21
 * @Version 1.0
 */
public class FallbackRuleManager {

    FallbackRuleManager(){}
    /**
     * No matter what type of block(flow/degrade/param/authority) it is.It is block.
     * So for a single resource,has only one customized fallback config.
     */
    private static final Map<String, FallbackRule> BLOCK_FALLBACK = new ConcurrentHashMap<String, FallbackRule>();
    private static final FallbackPropertyListener FALLBACK_PROPERTY_LISTENER = new FallbackPropertyListener();
    private static SentinelProperty<List<FallbackRule>> currentProperty = new DynamicSentinelProperty<List<FallbackRule>>();


    static {
        currentProperty.addListener(FALLBACK_PROPERTY_LISTENER);
    }

    public static void register2Property(SentinelProperty<List<FallbackRule>> property) {
        AssertUtil.notNull(property, "property can't be null");
        synchronized (FALLBACK_PROPERTY_LISTENER) {
            currentProperty.removeListener(FALLBACK_PROPERTY_LISTENER);
            property.addListener(FALLBACK_PROPERTY_LISTENER);
            currentProperty = property;
        }
    }

    public static FallbackRule getFallbackRule(String resource) {
        return BLOCK_FALLBACK.get(resource);
    }

    /**
     * manually load
     * @param fallbacks
     */
    public static void loadFallbacks(List<FallbackRule> fallbacks) {
        currentProperty.updateValue(fallbacks);
    }


    private static class FallbackPropertyListener implements PropertyListener<List<FallbackRule>> {

        @Override
        public void configUpdate(List<FallbackRule> fallbackRules) {
            Map<String, FallbackRule> fallbacks = buildFallbackMap(fallbackRules);
            BLOCK_FALLBACK.clear();
            BLOCK_FALLBACK.putAll(fallbacks);
            RecordLog.info("[FallbackManager] fallbacks received: " + fallbacks);
        }

        @Override
        public void configLoad(List<FallbackRule> value) {
            Map<String, FallbackRule> fallbacks = buildFallbackMap(value);
            BLOCK_FALLBACK.clear();
            BLOCK_FALLBACK.putAll(fallbacks);
            RecordLog.info("[FallbackManager] fallbacks loaded: " + fallbacks);
        }
    }

    private static Map<String, FallbackRule> buildFallbackMap(List<FallbackRule> fallbackRules) {
        Map<String, FallbackRule> newMap = new ConcurrentHashMap<>();
        if (null == fallbackRules || fallbackRules.isEmpty()) {
            return newMap;
        }
        for (FallbackRule fallbackRule : fallbackRules) {
            newMap.put(fallbackRule.getResource(), fallbackRule);
        }
        return newMap;
    }
}
