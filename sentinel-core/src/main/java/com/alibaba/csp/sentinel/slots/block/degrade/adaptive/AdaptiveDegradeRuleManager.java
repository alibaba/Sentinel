package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptive Rule Manager.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeRuleManager {

    private static volatile ConcurrentHashMap<String, AdaptiveDegradeRule> adaptiveRuleMap = new ConcurrentHashMap<>();
    private static volatile ConcurrentHashMap<String, AdaptiveServerMetric> adaptiveMetricMap = new ConcurrentHashMap<>();

    private static final AdaptiveRulePropertyListener LISTENER = new AdaptiveRulePropertyListener();

    private static SentinelProperty<AdaptiveDegradeRule> currentProperty
            = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    public static void register2Property(SentinelProperty<AdaptiveDegradeRule> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[AdaptiveDegradeRuleManager] Registering new property to adaptive degrade rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    public static AdaptiveDegradeRule getRule(String name) {
        AssertUtil.notNull(name, "name cannot be null");
        return adaptiveRuleMap.computeIfAbsent(name, key -> new AdaptiveDegradeRule(name));
    }

    public static void setServerMetric(String name, AdaptiveServerMetric serverMetric) {
        AssertUtil.notNull(name, "name cannot be null");
        AssertUtil.notNull(serverMetric, "serverMetric cannot be null");
        adaptiveMetricMap.put(name, serverMetric);
    }

    public static AdaptiveServerMetric getServerMetric(String name) {
        AssertUtil.notNull(name, "name cannot be null");
        return adaptiveMetricMap.get(name);
    }

    static void setAdaptiveRule(AdaptiveDegradeRule adaptiveDegradeRule){
        adaptiveRuleMap.put(adaptiveDegradeRule.getResource(), adaptiveDegradeRule);
    }

    private static class AdaptiveRulePropertyListener implements PropertyListener<AdaptiveDegradeRule> {

        @Override
        public void configUpdate(AdaptiveDegradeRule value) {
            if (value == null) {
                return;
            }
            RecordLog.info("[AdaptiveDegradeRuleManager] Resource:{},status of adaptive function activation:{}",value.getResource(),value.isEnabled());
            adaptiveRuleMap.put(value.getResource(), value);
        }

        @Override
        public void configLoad(AdaptiveDegradeRule value) {
            if (value == null) {
                return;
            }
            RecordLog.info("[AdaptiveDegradeRuleManager] Resource:{},status of adaptive function activation:{}",value.getResource(),value.isEnabled());
            adaptiveRuleMap.put(value.getResource(), value);
        }
    }
}
