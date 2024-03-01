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
package com.alibaba.csp.sentinel.webflow.rule;

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
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;
import com.alibaba.csp.sentinel.webflow.param.ParamRegexCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guanyu
 * @since 1.10.0
 */
public final class WebFlowRuleManager {

    /**
     * Web flow rule map: (resource, [rules...])
     */
    private static final Map<String, Set<WebFlowRule>> WEB_FLOW_RULE_MAP = new ConcurrentHashMap<String, Set<WebFlowRule>>();

    private static final Map<String, List<ParamFlowRule>> CONVERTED_WEB_FLOW_RULE_MAP = new ConcurrentHashMap<String, List<ParamFlowRule>>();

    private static final WebFlowRulePropertyListener LISTENER = new WebFlowRulePropertyListener();

    private static final Set<Integer> FIELD_REQUIRED_SET = new HashSet<Integer>(
            Arrays.asList(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_URL_PARAM,
                    SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HEADER,
                    SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_COOKIE,
                    SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_BODY_PARAM,
                    SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_PATH_PARAM)
    );
    private static SentinelProperty<Set<WebFlowRule>> currentProperty = new DynamicSentinelProperty<Set<WebFlowRule>>();

    static {
        currentProperty.addListener(LISTENER);
    }

    private WebFlowRuleManager() {
    }

    public static void register2Property(SentinelProperty<Set<WebFlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[WebFlowRuleManager] Registering new property to web flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load all provided web flow rules into memory, while
     * previous rules will be replaced.
     *
     * @param rules rule set
     * @return true if updated, otherwise false
     */
    public static boolean loadRules(Set<WebFlowRule> rules) {
        return currentProperty.updateValue(rules);
    }

    public static Set<WebFlowRule> getRules() {
        Set<WebFlowRule> rules = new HashSet<WebFlowRule>();
        for (Set<WebFlowRule> ruleSet : WEB_FLOW_RULE_MAP.values()) {
            rules.addAll(ruleSet);
        }
        return rules;
    }

    public static Set<WebFlowRule> getRulesForResource(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return new HashSet<WebFlowRule>();
        }
        Set<WebFlowRule> set = WEB_FLOW_RULE_MAP.get(resourceName);
        if (set == null) {
            return new HashSet<WebFlowRule>();
        }
        return new HashSet<WebFlowRule>(set);
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
            return new ArrayList<ParamFlowRule>();
        }
        return CONVERTED_WEB_FLOW_RULE_MAP.get(resourceName);
    }

    public static boolean hasRules(String resourceName) {
        List<ParamFlowRule> rules = CONVERTED_WEB_FLOW_RULE_MAP.get(resourceName);
        return rules != null && !rules.isEmpty();
    }

    public static boolean isValidRule(WebFlowRule rule) {
        if (rule == null || StringUtil.isBlank(rule.getResource()) || rule.getResourceMode() < 0 || rule.getControlBehavior() < 0) {
            return false;
        }
        if (rule.getCount() == null || rule.getCount() < 0) {
            return false;
        }
        if (rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER
                && rule.getMaxQueueingTimeoutMs() < 0) {
            return false;
        }
        if (rule.getIntervalMs() <= 0) {
            return false;
        }
        WebParamItem item = rule.getParamItem();
        if (item != null) {
            return isValidParamItem(item);
        }
        return true;
    }

    static boolean isValidParamItem(/*@NonNull*/ WebParamItem item) {
        if (item.getParseStrategy() < 0) {
            return false;
        }
        // Check required field name for item types.
        if (FIELD_REQUIRED_SET.contains(item.getParseStrategy()) && StringUtil.isBlank(item.getFieldName())) {
            return false;
        }
        return StringUtil.isEmpty(item.getPattern()) || item.getMatchStrategy() >= 0;
    }

    private static final class WebFlowRulePropertyListener implements PropertyListener<Set<WebFlowRule>> {

        @Override
        public void configUpdate(Set<WebFlowRule> conf) {
            applyWebFlowRuleInternal(conf);
            RecordLog.info("[WebFlowRuleManager] Web flow rules received: " + WEB_FLOW_RULE_MAP);
        }

        @Override
        public void configLoad(Set<WebFlowRule> conf) {
            applyWebFlowRuleInternal(conf);
            RecordLog.info("[WebFlowRuleManager] Web flow rules loaded: " + WEB_FLOW_RULE_MAP);
        }

        /*private int getIdxInternal(Map<String, Integer> idxMap, String resourceName) {
            // Prepare index map.
            if (!idxMap.containsKey(resourceName)) {
                idxMap.put(resourceName, 0);
            }
            return idxMap.get(resourceName);
        }*/

        private void cacheRegexPattern(/*@NonNull*/ WebParamItem item) {
            String pattern = item.getPattern();
            if (StringUtil.isNotEmpty(pattern) &&
                    item.getMatchStrategy() == SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_REGEX) {
                if (ParamRegexCache.getRegexPattern(pattern) == null) {
                    ParamRegexCache.addRegexPattern(pattern);
                }
            }
        }

        private synchronized void applyWebFlowRuleInternal(Set<WebFlowRule> conf) {
            if (conf == null || conf.isEmpty()) {
                applyToConvertedParamMap(new HashSet<ParamFlowRule>());
                WEB_FLOW_RULE_MAP.clear();
                return;
            }
            Map<String, Set<WebFlowRule>> webFlowRuleMap = new ConcurrentHashMap<String, Set<WebFlowRule>>();
            Map<String, Integer> idxMap = new HashMap<String, Integer>();
            Set<ParamFlowRule> paramFlowRules = new HashSet<ParamFlowRule>();
            Map<String, List<WebFlowRule>> noParamMap = new HashMap<String, List<WebFlowRule>>();

            for (WebFlowRule rule : conf) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[WebFlowRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }
                String resourceName = rule.getResource();
                if (rule.getParamItem() == null) {
                    // Cache the rules with no parameter config, then skip.
                    List<WebFlowRule> noParamList = noParamMap.get(resourceName);
                    if (noParamList == null) {
                        noParamList = new ArrayList<WebFlowRule>();
                        noParamMap.put(resourceName, noParamList);
                    }
                    noParamList.add(rule);
                } else {
                    /*int idx = getIdxInternal(idxMap, resourceName);
                    if (paramFlowRules.add(WebFlowRuleConverter.applyToParamRule(rule, idx))) {
                        idxMap.put(rule.getResource(), idx + 1);
                    }*/
                    // Convert to parameter flow rule.
                    paramFlowRules.add(WebFlowRuleConverter.applyToParamRule(rule));
                    cacheRegexPattern(rule.getParamItem());
                }
                // Apply to the web param rule map.
                Set<WebFlowRule> ruleSet = webFlowRuleMap.get(resourceName);
                if (ruleSet == null) {
                    ruleSet = new HashSet<WebFlowRule>();
                    webFlowRuleMap.put(resourceName, ruleSet);
                }
                ruleSet.add(rule);
            }
            // Handle non-param mode rules.
            for (Map.Entry<String, List<WebFlowRule>> e : noParamMap.entrySet()) {
                List<WebFlowRule> rules = e.getValue();
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                for (WebFlowRule rule : rules) {
                    // Always use the default param key.
                    paramFlowRules.add(WebFlowRuleConverter.applyNonParamToParamRule(rule));
                }
            }

            applyToConvertedParamMap(paramFlowRules);

            WEB_FLOW_RULE_MAP.clear();
            WEB_FLOW_RULE_MAP.putAll(webFlowRuleMap);
        }

        private void applyToConvertedParamMap(Set<ParamFlowRule> paramFlowRules) {
            Map<String, List<ParamFlowRule>> newRuleMap = ParamFlowRuleUtil.buildParamRuleMap(
                    new ArrayList<ParamFlowRule>(paramFlowRules));
            if (newRuleMap == null || newRuleMap.isEmpty()) {
                // No parameter flow rules, so clear all the metrics.
                for (String resource : CONVERTED_WEB_FLOW_RULE_MAP.keySet()) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                }
                RecordLog.info("[WebFlowRuleManager] No web param rules, clearing parameter metrics of previous rules");
                CONVERTED_WEB_FLOW_RULE_MAP.clear();
                return;
            }

            // Clear unused parameter metrics.
            for (Map.Entry<String, List<ParamFlowRule>> entry : CONVERTED_WEB_FLOW_RULE_MAP.entrySet()) {
                String resource = entry.getKey();
                if (!newRuleMap.containsKey(resource)) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                    continue;
                }
                List<ParamFlowRule> newRuleList = newRuleMap.get(resource);
                List<ParamFlowRule> oldRuleList = new ArrayList<ParamFlowRule>(entry.getValue());
                oldRuleList.removeAll(newRuleList);
                for (ParamFlowRule rule : oldRuleList) {
                    ParameterMetric metric = ParameterMetricStorage.getParamMetricForResource(resource);
                    if (null != metric) {
                        metric.clearForRule(rule);
                    }
                }
            }

            // Apply to converted rule map.
            CONVERTED_WEB_FLOW_RULE_MAP.clear();
            CONVERTED_WEB_FLOW_RULE_MAP.putAll(newRuleMap);

            RecordLog.info("[WebFlowRuleManager] Converted internal param rules: " + CONVERTED_WEB_FLOW_RULE_MAP);
        }
    }
}
