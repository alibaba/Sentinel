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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Manager for frequent ("hot-spot") parameter flow rules.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class ParamFlowRuleManager {

    private static final Map<String, List<ParamFlowRule>> paramFlowRules
        = new ConcurrentHashMap<String, List<ParamFlowRule>>();

    private final static RulePropertyListener PROPERTY_LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<ParamFlowRule>> currentProperty
        = new DynamicSentinelProperty<List<ParamFlowRule>>();

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
     * Listen to the {@link SentinelProperty} for {@link ParamFlowRule}s. The property is the source
     * of {@link ParamFlowRule}s. Parameter flow rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen
     */
    public static void register2Property(SentinelProperty<List<ParamFlowRule>> property) {
        synchronized (PROPERTY_LISTENER) {
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
            RecordLog.info("[ParamFlowRuleManager] New property has been registered to hot param rule manager");
        }
    }

    public static List<ParamFlowRule> getRulesOfResource(String resourceName) {
        return paramFlowRules.get(resourceName);
    }

    public static boolean hasRules(String resourceName) {
        List<ParamFlowRule> rules = paramFlowRules.get(resourceName);
        return rules != null && !rules.isEmpty();
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<ParamFlowRule> getRules() {
        List<ParamFlowRule> rules = new ArrayList<ParamFlowRule>();
        for (Map.Entry<String, List<ParamFlowRule>> entry : paramFlowRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    private static Object parseValue(String value, String classType) {
        if (value == null) {
            throw new IllegalArgumentException("Null value");
        }
        if (StringUtil.isBlank(classType)) {
            // If the class type is not provided, then treat it as string.
            return value;
        }
        // Handle primitive type.
        if (int.class.toString().equals(classType) || Integer.class.getName().equals(classType)) {
            return Integer.parseInt(value);
        } else if (boolean.class.toString().equals(classType) || Boolean.class.getName().equals(classType)) {
            return Boolean.parseBoolean(value);
        } else if (long.class.toString().equals(classType) || Long.class.getName().equals(classType)) {
            return Long.parseLong(value);
        } else if (double.class.toString().equals(classType) || Double.class.getName().equals(classType)) {
            return Double.parseDouble(value);
        } else if (float.class.toString().equals(classType) || Float.class.getName().equals(classType)) {
            return Float.parseFloat(value);
        } else if (byte.class.toString().equals(classType) || Byte.class.getName().equals(classType)) {
            return Byte.parseByte(value);
        } else if (short.class.toString().equals(classType) || Short.class.getName().equals(classType)) {
            return Short.parseShort(value);
        } else if (char.class.toString().equals(classType)) {
            char[] array = value.toCharArray();
            return array.length > 0 ? array[0] : null;
        }

        return value;
    }

    static class RulePropertyListener implements PropertyListener<List<ParamFlowRule>> {

        @Override
        public void configUpdate(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateHotParamRules(list);
            if (rules != null) {
                paramFlowRules.clear();
                paramFlowRules.putAll(rules);
            }
            RecordLog.info("[ParamFlowRuleManager] Hot spot parameter flow rules received: " + paramFlowRules);
        }

        @Override
        public void configLoad(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateHotParamRules(list);
            if (rules != null) {
                paramFlowRules.clear();
                paramFlowRules.putAll(rules);
            }
            RecordLog.info("[ParamFlowRuleManager] Hot spot parameter flow rules received: " + paramFlowRules);
        }

        private Map<String, List<ParamFlowRule>> aggregateHotParamRules(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> newRuleMap = new ConcurrentHashMap<String, List<ParamFlowRule>>();

            if (list == null || list.isEmpty()) {
                // No parameter flow rules, so clear all the metrics.
                ParamFlowSlot.getMetricsMap().clear();
                RecordLog.info("[ParamFlowRuleManager] No parameter flow rules, clearing all parameter metrics");
                return newRuleMap;
            }

            for (ParamFlowRule rule : list) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[ParamFlowRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }

                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                }

                if (rule.getParamFlowItemList() == null) {
                    rule.setParamFlowItemList(new ArrayList<ParamFlowItem>());
                }

                Map<Object, Integer> itemMap = parseHotItems(rule.getParamFlowItemList());
                rule.setParsedHotItems(itemMap);

                String resourceName = rule.getResource();
                List<ParamFlowRule> ruleList = newRuleMap.get(resourceName);
                if (ruleList == null) {
                    ruleList = new ArrayList<ParamFlowRule>();
                    newRuleMap.put(resourceName, ruleList);
                }
                ruleList.add(rule);
            }

            // Clear unused hot param metrics.
            Set<String> previousResources = paramFlowRules.keySet();
            for (String resource : previousResources) {
                if (!newRuleMap.containsKey(resource)) {
                    ParamFlowSlot.clearHotParamMetricForName(resource);
                }
            }

            return newRuleMap;
        }
    }

    static Map<Object, Integer> parseHotItems(List<ParamFlowItem> items) {
        Map<Object, Integer> itemMap = new HashMap<Object, Integer>();
        if (items == null || items.isEmpty()) {
            return itemMap;
        }
        for (ParamFlowItem item : items) {
            // Value should not be null.
            Object value;
            try {
                value = parseValue(item.getObject(), item.getClassType());
            } catch (Exception ex) {
                RecordLog.warn("[ParamFlowRuleManager] Failed to parse value for item: " + item, ex);
                continue;
            }
            if (item.getCount() == null || item.getCount() < 0 || value == null) {
                RecordLog.warn("[ParamFlowRuleManager] Ignoring invalid exclusion parameter item: " + item);
                continue;
            }
            itemMap.put(value, item.getCount());
        }
        return itemMap;
    }

    static boolean isValidRule(ParamFlowRule rule) {
        return rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0
            && rule.getParamIdx() != null && rule.getParamIdx() >= 0;
    }

    private ParamFlowRuleManager() {}
}

