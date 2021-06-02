/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayRegexCache;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleUtil;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetric;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetricStorage;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class GatewayRuleManager {

    /**
     * Gateway flow rule map: (resource, [rules...])
     */
    private static final Map<String, Set<GatewayFlowRule>> GATEWAY_RULE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<ParamFlowRule>> CONVERTED_PARAM_RULE_MAP = new ConcurrentHashMap<>();

    private static final GatewayRulePropertyListener LISTENER = new GatewayRulePropertyListener();
    private static final Set<Integer> FIELD_REQUIRED_SET = new HashSet<>(
            Arrays.asList(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM,
                    SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER,
                    SentinelGatewayConstants.PARAM_PARSE_STRATEGY_COOKIE)
    );
    private static SentinelProperty<Set<GatewayFlowRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    private GatewayRuleManager() {
    }

    public static void register2Property(SentinelProperty<Set<GatewayFlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[GatewayRuleManager] Registering new property to gateway flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load all provided gateway rules into memory, while
     * previous rules will be replaced.
     *
     * @param rules rule set
     * @return true if updated, otherwise false
     */
    public static boolean loadRules(Set<GatewayFlowRule> rules) {
        return currentProperty.updateValue(rules);
    }

    public static Set<GatewayFlowRule> getRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        for (Set<GatewayFlowRule> ruleSet : GATEWAY_RULE_MAP.values()) {
            rules.addAll(ruleSet);
        }
        return rules;
    }

    public static Set<GatewayFlowRule> getRulesForResource(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return new HashSet<>();
        }
        Set<GatewayFlowRule> set = GATEWAY_RULE_MAP.get(resourceName);
        if (set == null) {
            return new HashSet<>();
        }
        return new HashSet<>(set);
    }

    /**
     * <p>Get all converted parameter rules.</p>
     * <p>Note: caller SHOULD NOT modify the list and rules.</p>
     *
     * @param resourceName valid resource name
     * @return converted parameter rules
     */
    public static List<ParamFlowRule> getConvertedParamRules(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return new ArrayList<>();
        }
        return CONVERTED_PARAM_RULE_MAP.get(resourceName);
    }

    public static boolean isValidRule(GatewayFlowRule rule) {
        if (rule == null || StringUtil.isBlank(rule.getResource()) || rule.getResourceMode() < 0
                || rule.getGrade() < 0 || rule.getCount() < 0 || rule.getBurst() < 0 || rule.getControlBehavior() < 0) {
            return false;
        }
        if (rule.getGrade() == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER
                && rule.getMaxQueueingTimeoutMs() < 0) {
            return false;
        }
        if (rule.getIntervalSec() <= 0) {
            return false;
        }
        GatewayParamFlowItem item = rule.getParamItem();
        if (item != null) {
            return isValidParamItem(item);
        }
        return true;
    }

    static boolean isValidParamItem(/*@NonNull*/ GatewayParamFlowItem item) {
        if (item.getParseStrategy() < 0) {
            return false;
        }
        // Check required field name for item types.
        if (FIELD_REQUIRED_SET.contains(item.getParseStrategy()) && StringUtil.isBlank(item.getFieldName())) {
            return false;
        }
        return StringUtil.isEmpty(item.getPattern()) || item.getMatchStrategy() >= 0;
    }

    private static final class GatewayRulePropertyListener implements PropertyListener<Set<GatewayFlowRule>> {

        @Override
        public void configUpdate(Set<GatewayFlowRule> conf) {
            applyGatewayRuleInternal(conf);
            RecordLog.info("[GatewayRuleManager] Gateway flow rules received: {}", GATEWAY_RULE_MAP);
        }

        @Override
        public void configLoad(Set<GatewayFlowRule> conf) {
            applyGatewayRuleInternal(conf);
            RecordLog.info("[GatewayRuleManager] Gateway flow rules loaded: {}", GATEWAY_RULE_MAP);
        }

        private int getIdxInternal(Map<String, Integer> idxMap, String resourceName) {
            // Prepare index map.
            if (!idxMap.containsKey(resourceName)) {
                idxMap.put(resourceName, 0);
            }
            return idxMap.get(resourceName);
        }

        private void cacheRegexPattern(/*@NonNull*/ GatewayParamFlowItem item) {
            String pattern = item.getPattern();
            if (StringUtil.isNotEmpty(pattern) &&
                    item.getMatchStrategy() == SentinelGatewayConstants.PARAM_MATCH_STRATEGY_REGEX) {
                if (GatewayRegexCache.getRegexPattern(pattern) == null) {
                    GatewayRegexCache.addRegexPattern(pattern);
                }
            }
        }

        private synchronized void applyGatewayRuleInternal(Set<GatewayFlowRule> conf) {
            if (conf == null || conf.isEmpty()) {
                applyToConvertedParamMap(new HashSet<ParamFlowRule>());
                GATEWAY_RULE_MAP.clear();
                return;
            }
            Map<String, Set<GatewayFlowRule>> gatewayRuleMap = new ConcurrentHashMap<>();
            Map<String, Integer> idxMap = new HashMap<>();
            Set<ParamFlowRule> paramFlowRules = new HashSet<>();
            Map<String, List<GatewayFlowRule>> noParamMap = new HashMap<>();

            for (GatewayFlowRule rule : conf) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[GatewayRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }
                String resourceName = rule.getResource();
                if (rule.getParamItem() == null) {
                    // Cache the rules with no parameter config, then skip.
                    List<GatewayFlowRule> noParamList = noParamMap.get(resourceName);
                    if (noParamList == null) {
                        noParamList = new ArrayList<>();
                        noParamMap.put(resourceName, noParamList);
                    }
                    noParamList.add(rule);
                } else {
                    int idx = getIdxInternal(idxMap, resourceName);
                    // Convert to parameter flow rule.
                    if (paramFlowRules.add(GatewayRuleConverter.applyToParamRule(rule, idx))) {
                        idxMap.put(rule.getResource(), idx + 1);
                    }
                    cacheRegexPattern(rule.getParamItem());
                }
                // Apply to the gateway rule map.
                Set<GatewayFlowRule> ruleSet = gatewayRuleMap.get(resourceName);
                if (ruleSet == null) {
                    ruleSet = new HashSet<>();
                    gatewayRuleMap.put(resourceName, ruleSet);
                }
                ruleSet.add(rule);
            }
            // Handle non-param mode rules.
            for (Map.Entry<String, List<GatewayFlowRule>> e : noParamMap.entrySet()) {
                List<GatewayFlowRule> rules = e.getValue();
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                for (GatewayFlowRule rule : rules) {
                    int idx = getIdxInternal(idxMap, e.getKey());
                    // Always use the same index (the last position).
                    paramFlowRules.add(GatewayRuleConverter.applyNonParamToParamRule(rule, idx));
                }
            }

            applyToConvertedParamMap(paramFlowRules);

            GATEWAY_RULE_MAP.clear();
            GATEWAY_RULE_MAP.putAll(gatewayRuleMap);
        }

        private void applyToConvertedParamMap(Set<ParamFlowRule> paramFlowRules) {
            Map<String, List<ParamFlowRule>> newRuleMap = ParamFlowRuleUtil.buildParamRuleMap(
                    new ArrayList<>(paramFlowRules));
            if (newRuleMap == null || newRuleMap.isEmpty()) {
                // No parameter flow rules, so clear all the metrics.
                for (String resource : CONVERTED_PARAM_RULE_MAP.keySet()) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                }
                RecordLog.info("[GatewayRuleManager] No gateway rules, clearing parameter metrics of previous rules");
                CONVERTED_PARAM_RULE_MAP.clear();
                return;
            }

            // Clear unused parameter metrics.
            for (Map.Entry<String, List<ParamFlowRule>> entry : CONVERTED_PARAM_RULE_MAP.entrySet()) {
                String resource = entry.getKey();
                if (!newRuleMap.containsKey(resource)) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                    continue;
                }
                List<ParamFlowRule> newRuleList = newRuleMap.get(resource);
                List<ParamFlowRule> oldRuleList = new ArrayList<>(entry.getValue());
                oldRuleList.removeAll(newRuleList);
                for (ParamFlowRule rule : oldRuleList) {
                    ParameterMetric metric = ParameterMetricStorage.getParamMetricForResource(resource);
                    if (null != metric) {
                        metric.clearForRule(rule);
                    }
                }
            }

            // Apply to converted rule map.
            CONVERTED_PARAM_RULE_MAP.clear();
            CONVERTED_PARAM_RULE_MAP.putAll(newRuleMap);

            RecordLog.info("[GatewayRuleManager] Converted internal param rules: {}", CONVERTED_PARAM_RULE_MAP);
        }
    }
}
