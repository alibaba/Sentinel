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
package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author jialiang.linjl
 * @since 0.2.0
 */
public final class HotParamRuleManager {

    private static final Map<String, List<HotParamRule>> hotParamRules
        = new ConcurrentHashMap<String, List<HotParamRule>>();

    final static RulePropertyListener listener = new RulePropertyListener();
    private static SentinelProperty<List<HotParamRule>> currentProperty
        = new DynamicSentinelProperty<List<HotParamRule>>();

    static {
        currentProperty.addListener(listener);
    }

    /**
     * Load hot parameter rules. Former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<HotParamRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.info("[HotParamRuleManager] Failed to load rules", e);
        }
    }

    public static List<HotParamRule> getRulesOfResource(String resourceName) {
        return hotParamRules.get(resourceName);
    }

    public static boolean hasRules(String resourceName) {
        List<HotParamRule> rules = hotParamRules.get(resourceName);
        return rules != null && !rules.isEmpty();
    }

    private static Object parseValue(String value, String classType) {
        if (value == null) {
            throw new IllegalArgumentException("Null value");
        }
        if (StringUtil.isBlank(classType)) {
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
            return array.length > 0 ? array[1] : null;
        }

        return value;
    }

    static class RulePropertyListener implements PropertyListener<List<HotParamRule>> {

        @Override
        public void configUpdate(List<HotParamRule> list) {
            Map<String, List<HotParamRule>> rules = aggregateHotParamRules(list);
            if (rules != null) {
                hotParamRules.clear();
                hotParamRules.putAll(rules);
            }
            RecordLog.info("[HotParamRuleManager] Hot-spot parameter flow rules received: " + hotParamRules);
        }

        @Override
        public void configLoad(List<HotParamRule> list) {
            Map<String, List<HotParamRule>> rules = aggregateHotParamRules(list);
            if (rules != null) {
                hotParamRules.clear();
                hotParamRules.putAll(rules);
            }
            RecordLog.info("[HotParamRuleManager] Hot-spot parameter flow rules received: " + hotParamRules);
        }

        private Map<String, List<HotParamRule>> aggregateHotParamRules(List<HotParamRule> list) {
            Map<String, List<HotParamRule>> newRuleMap = new ConcurrentHashMap<String, List<HotParamRule>>();

            if (list == null || list.isEmpty()) {
                return newRuleMap;
            }

            for (HotParamRule rule : list) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[HotParamRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }

                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(FlowRule.LIMIT_APP_DEFAULT);
                }

                if (rule.getHotItemList() == null) {
                    rule.setHotItemList(new ArrayList<HotItem>());
                }

                Map<Object, Integer> itemMap = parseHotItems(rule);
                rule.setParsedHotItems(itemMap);

                String resourceName = rule.getResource();
                List<HotParamRule> ruleList = newRuleMap.get(resourceName);
                if (ruleList == null) {
                    ruleList = new ArrayList<HotParamRule>();
                    newRuleMap.put(resourceName, ruleList);
                }
                ruleList.add(rule);
            }

            return newRuleMap;
        }
    }

    private static Map<Object, Integer> parseHotItems(/*@Valid*/ HotParamRule rule) {
        List<HotItem> items = rule.getHotItemList();
        Map<Object, Integer> itemMap = new HashMap<Object, Integer>();
        if (items == null || items.isEmpty()) {
            return itemMap;
        }
        for (HotItem item : items) {
            // Value should not be null.
            Object value;
            try {
                value = parseValue(item.getObject(), item.getClassType());
            } catch (Exception ex) {
                RecordLog.warn("[HotParamRuleManager] Failed to parse value for item: " + item);
                continue;
            }
            if (item.getCount() == null || item.getCount() < 0 || value == null) {
                RecordLog.warn("[HotParamRuleManager] Ignoring invalid exception parameter item: " + item);
                continue;
            }
            itemMap.put(value, item.getCount());
        }
        return itemMap;
    }

    private static boolean isValidRule(HotParamRule rule) {
        return rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0
            && rule.getParamIdx() != null && rule.getParamIdx() >= 0;
    }

    private HotParamRuleManager() {}
}

