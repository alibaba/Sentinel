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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * @author Eric Zhao
 */
public final class ParamFlowRuleUtil {

    /**
     * Check whether the provided rule is valid.
     *
     * @param rule any parameter rule
     * @return true if valid, otherwise false
     */
    public static boolean isValidRule(ParamFlowRule rule) {
        return rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0
            && rule.getGrade() >= 0 && rule.getParamIdx() != null
            && rule.getBurstCount() >= 0 && rule.getControlBehavior() >= 0
            && rule.getDurationInSec() > 0 && rule.getMaxQueueingTimeMs() >= 0
            && checkCluster(rule) & checkRegexField(rule);
    }

    private static boolean checkCluster(/*@PreChecked*/ ParamFlowRule rule) {
        if (!rule.isClusterMode()) {
            return true;
        }
        ParamFlowClusterConfig clusterConfig = rule.getClusterConfig();
        if (clusterConfig == null) {
            return false;
        }
        if (!FlowRuleUtil.isWindowConfigValid(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs())) {
            return false;
        }
        return validClusterRuleId(clusterConfig.getFlowId());
    }

    private static boolean checkRegexField(ParamFlowRule rule) {
        if (!RuleManager.checkRegexResourceField(rule)) {
            return false;
        }
        if (rule.isRegex()) {
            return !rule.isClusterMode() && rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
        }
        return true;
    }

    public static boolean validClusterRuleId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Fill the parameter rule with parsed items.
     *
     * @param rule valid parameter rule
     */
    public static void fillExceptionFlowItems(ParamFlowRule rule) {
        if (rule != null) {
            if (rule.getParamFlowItemList() == null) {
                rule.setParamFlowItemList(new ArrayList<ParamFlowItem>());
            }

            Map<Object, Integer> itemMap = parseHotItems(rule.getParamFlowItemList());
            rule.setParsedHotItems(itemMap);
        }
    }

    /**
     * Build the flow rule map from raw list of flow rules, grouping by resource name.
     *
     * @param list raw list of flow rules
     * @return constructed new flow rule map; empty map if list is null or empty, or no valid rules
     * @since 1.6.1
     */
    public static Map<String, List<ParamFlowRule>> buildParamRuleMap(List<ParamFlowRule> list) {
        return buildParamRuleMap(list, null);
    }

    /**
     * Build the parameter flow rule map from raw list of rules, grouping by resource name.
     *
     * @param list          raw list of parameter flow rules
     * @param filter        rule filter
     * @return constructed new parameter flow rule map; empty map if list is null or empty, or no wanted rules
     * @since 1.6.1
     */
    public static Map<String, List<ParamFlowRule>> buildParamRuleMap(List<ParamFlowRule> list,
                                                                     Predicate<ParamFlowRule> filter) {
        return buildParamRuleMap(list, filter, true);
    }

    /**
     * Build the parameter flow rule map from raw list of rules, grouping by resource name.
     *
     * @param list          raw list of parameter flow rules
     * @param filter        rule filter
     * @param shouldSort    whether the rules should be sorted
     * @return constructed new parameter flow rule map; empty map if list is null or empty, or no wanted rules
     * @since 1.6.1
     */
    public static Map<String, List<ParamFlowRule>> buildParamRuleMap(List<ParamFlowRule> list,
                                                                     Predicate<ParamFlowRule> filter,
                                                                     boolean shouldSort) {
        return buildParamRuleMap(list, EXTRACT_RESOURCE, filter, shouldSort);
    }

    /**
     * Build the rule map from raw list of parameter flow rules, grouping by provided group function.
     *
     * @param list          raw list of parameter flow rules
     * @param groupFunction grouping function of the map (by key)
     * @param filter        rule filter
     * @param shouldSort    whether the rules should be sorted
     * @param <K>           type of key
     * @return constructed new rule map; empty map if list is null or empty, or no wanted rules
     * @since 1.6.1
     */
    public static <K> Map<K, List<ParamFlowRule>> buildParamRuleMap(List<ParamFlowRule> list,
                                                                    Function<ParamFlowRule, K> groupFunction,
                                                                    Predicate<ParamFlowRule> filter,
                                                                    boolean shouldSort) {
        AssertUtil.notNull(groupFunction, "groupFunction should not be null");
        Map<K, List<ParamFlowRule>> newRuleMap = new ConcurrentHashMap<>();
        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }
        Map<K, Set<ParamFlowRule>> tmpMap = new ConcurrentHashMap<>();

        for (ParamFlowRule rule : list) {
            if (!ParamFlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn("[ParamFlowRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                continue;
            }
            if (filter != null && !filter.test(rule)) {
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            ParamFlowRuleUtil.fillExceptionFlowItems(rule);

            K key = groupFunction.apply(rule);
            if (key == null) {
                continue;
            }
            Set<ParamFlowRule> flowRules = tmpMap.get(key);

            if (flowRules == null) {
                // Use hash set here to remove duplicate rules.
                flowRules = new HashSet<>();
                tmpMap.put(key, flowRules);
            }

            flowRules.add(rule);
        }
        for (Entry<K, Set<ParamFlowRule>> entries : tmpMap.entrySet()) {
            List<ParamFlowRule> rules = new ArrayList<>(entries.getValue());
            if (shouldSort) {
                // TODO: Sort the rules.
            }
            newRuleMap.put(entries.getKey(), rules);
        }

        return newRuleMap;
    }

    static Map<Object, Integer> parseHotItems(List<ParamFlowItem> items) {
        if (items == null || items.isEmpty()) {
            return new HashMap<>();
        }
        Map<Object, Integer> itemMap = new HashMap<>(items.size());
        for (ParamFlowItem item : items) {
            // Value should not be null.
            Object value;
            try {
                value = parseItemValue(item.getObject(), item.getClassType());
            } catch (Exception ex) {
                RecordLog.warn("[ParamFlowRuleUtil] Failed to parse value for item: " + item, ex);
                continue;
            }
            if (item.getCount() == null || item.getCount() < 0 || value == null) {
                RecordLog.warn("[ParamFlowRuleUtil] Ignoring invalid exclusion parameter item: " + item);
                continue;
            }
            itemMap.put(value, item.getCount());
        }
        return itemMap;
    }

    static Object parseItemValue(String value, String classType) {
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

    private static final Function<ParamFlowRule, String> EXTRACT_RESOURCE = new Function<ParamFlowRule, String>() {
        @Override
        public String apply(ParamFlowRule rule) {
            return rule.getResource();
        }
    };

    private ParamFlowRuleUtil() {}
}
