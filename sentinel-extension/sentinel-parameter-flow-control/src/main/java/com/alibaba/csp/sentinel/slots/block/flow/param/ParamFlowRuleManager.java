/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Manager for frequent ("hot-spot") parameter flow rules.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class ParamFlowRuleManager {

    private static final Map<String, List<ParamFlowRule>> PARAM_FLOW_RULES = new ConcurrentHashMap<>();

    private final static RulePropertyListener PROPERTY_LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<ParamFlowRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * Load parameter flow rules. Former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<ParamFlowRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.info("[ParamFlowRuleManager] Failed to load rules", e);
        }
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link ParamFlowRule}s. The
     * property is the source of {@link ParamFlowRule}s. Parameter flow rules
     * can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen
     */
    public static void register2Property(SentinelProperty<List<ParamFlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (PROPERTY_LISTENER) {
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
            RecordLog.info("[ParamFlowRuleManager] New property has been registered to hot param rule manager");
        }
    }

    public static List<ParamFlowRule> getRulesOfResource(String resourceName) {
        return new ArrayList<>(PARAM_FLOW_RULES.get(resourceName));
    }

    public static boolean hasRules(String resourceName) {
        List<ParamFlowRule> rules = PARAM_FLOW_RULES.get(resourceName);
        return rules != null && !rules.isEmpty();
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<ParamFlowRule> getRules() {
        List<ParamFlowRule> rules = new ArrayList<>();
        for (Map.Entry<String, List<ParamFlowRule>> entry : PARAM_FLOW_RULES.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    static class RulePropertyListener implements PropertyListener<List<ParamFlowRule>> {

        @Override
        public void configUpdate(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateAndPrepareParamRules(list);
            if (rules != null) {
                PARAM_FLOW_RULES.clear();
                PARAM_FLOW_RULES.putAll(rules);
            }
            RecordLog.info("[ParamFlowRuleManager] Parameter flow rules received: {}", PARAM_FLOW_RULES);
        }

        @Override
        public void configLoad(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateAndPrepareParamRules(list);
            if (rules != null) {
                PARAM_FLOW_RULES.clear();
                PARAM_FLOW_RULES.putAll(rules);
            }
            RecordLog.info("[ParamFlowRuleManager] Parameter flow rules received: {}", PARAM_FLOW_RULES);
        }

        private Map<String, List<ParamFlowRule>> aggregateAndPrepareParamRules(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> newRuleMap = ParamFlowRuleUtil.buildParamRuleMap(list);
            if (newRuleMap == null || newRuleMap.isEmpty()) {
                // No parameter flow rules, so clear all the metrics.
                ParameterMetricStorage.getMetricsMap().clear();
                RecordLog.info("[ParamFlowRuleManager] No parameter flow rules, clearing all parameter metrics");
                return newRuleMap;
            }

            // Clear unused parameter metrics.
            for (Map.Entry<String, List<ParamFlowRule>> entry : PARAM_FLOW_RULES.entrySet()) {
                String resource = entry.getKey();
                if (!newRuleMap.containsKey(resource)) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                    continue;
                }
                List<ParamFlowRule> newRuleList = newRuleMap.get(resource);
                List<ParamFlowRule> oldRuleList = new ArrayList<>(entry.getValue());
                oldRuleList.removeAll(newRuleList);
                for (ParamFlowRule rule : oldRuleList) {
                    ParameterMetricStorage.getParamMetricForResource(resource).clearForRule(rule);
                }
            }

            return newRuleMap;
        }
    }

    private ParamFlowRuleManager() {}
}
