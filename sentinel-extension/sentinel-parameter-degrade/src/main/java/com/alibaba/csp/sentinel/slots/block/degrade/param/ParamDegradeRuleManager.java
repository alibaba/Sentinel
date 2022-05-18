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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ResponseTimeCircuitBreaker;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeUtil.DEFAULT_OTHER_KEY;
import static com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeUtil.KEY_SPLIT;

/**
 * Manager for frequent ("hot-spot") parameter degrade rules.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class ParamDegradeRuleManager {

    /**
     * first key is resourceName
     * second key is itemInfo，{paramIndex}_{itemValue} or {paramIndex}_{SENTINEL_OTHER}
     */
    private static volatile Map<String, Map<String, List<CircuitBreaker>>> CIRCUIT_BREAKERS = new HashMap<>();

    private static volatile Map<String, Set<ParamDegradeRule>> PARAM_DEGRADE_RULES = new ConcurrentHashMap<>();

    private final static RulePropertyListener PROPERTY_LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<ParamDegradeRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * Load parameter flow rules. Former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<ParamDegradeRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.info("[ParamDegradeRuleManager] Failed to load rules", e);
        }
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link ParamDegradeRule}s. The
     * property is the source of {@link ParamDegradeRule}s. Parameter flow rules
     * can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen
     */
    public static void register2Property(SentinelProperty<List<ParamDegradeRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (PROPERTY_LISTENER) {
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
            RecordLog.info("[ParamFlowRuleManager] New property has been registered to hot param rule manager");
        }
    }


    /**
     * traverse the args to find the right CircuitBreaker
     * if cannot find the right arg value, try to find {paramIndex}_{SENTINEL-OTHER} key
     * if one resource has more than one parameter degrade rule, find the first one(by args order) and return
     *
     * @param resourceName name of resource
     * @param args         args of current method
     * @return
     */
    public static List<CircuitBreaker> getCircuitBreakers(String resourceName, Object[] args) {
        if (!CIRCUIT_BREAKERS.containsKey(resourceName) || args == null || args.length == 0) {
            return Collections.emptyList();
        }

        Map<String, List<CircuitBreaker>> itemMap = CIRCUIT_BREAKERS.get(resourceName);

        for (int index = 0; index < args.length; index++) {
            String key = index + KEY_SPLIT + args[index];
            if (itemMap.containsKey(key)) {
                return itemMap.get(key);
            }

            String otherKey = index + KEY_SPLIT + DEFAULT_OTHER_KEY;
            if (itemMap.containsKey(otherKey)) {
                return itemMap.get(otherKey);
            }
        }

        return Collections.emptyList();
    }

    /**
     * find the CircuitBreaker by ruleKey and itemKey
     *
     * @param degradeKey key generated by resourceName
     * @param itemKey    key generated by item
     * @return
     */
    static List<CircuitBreaker> getCircuitBreakers(String degradeKey, String itemKey) {
        if (CIRCUIT_BREAKERS.containsKey(degradeKey)) {
            return CIRCUIT_BREAKERS.get(degradeKey).get(itemKey);
        }

        return Collections.emptyList();
    }

    /**
     * find the CircuitBreaker by ruleKey and itemKey
     *
     * @param degradeKey key generated by resourceName
     * @return
     */
    static Map<String, List<CircuitBreaker>> getCircuitBreakers(String degradeKey) {
        if (CIRCUIT_BREAKERS.containsKey(degradeKey)) {
            return CIRCUIT_BREAKERS.get(degradeKey);
        }

        return new HashMap<>();
    }

    /**
     * @param resourceName
     * @return
     */
    public static boolean hasRules(String resourceName) {
        Set<ParamDegradeRule> rules = PARAM_DEGRADE_RULES.get(resourceName);
        return rules != null && !rules.isEmpty();
    }

    public static boolean isValidRule(ParamDegradeRule rule) {
        boolean baseValid = rule != null && !StringUtil.isBlank(rule.getResource())
                && rule.getCount() >= 0 && rule.getTimeWindow() > 0;
        if (!baseValid) {
            return false;
        }
        if (rule.getMinRequestAmount() <= 0 || rule.getStatIntervalMs() <= 0) {
            return false;
        }

        for (ParamDegradeItem item : rule.getParamDegradeItemList()) {
            if (StringUtil.isBlank(item.getObject()) || StringUtil.isBlank(item.getClassType())) {
                return false;
            }
        }
        switch (rule.getGrade()) {
            case RuleConstant.DEGRADE_GRADE_RT:
                return rule.getSlowRatioThreshold() >= 0 && rule.getSlowRatioThreshold() <= 1;
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO:
                return rule.getCount() <= 1;
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<ParamDegradeRule> getRules() {
        List<ParamDegradeRule> rules = new ArrayList<>();
        for (Map.Entry<String, Set<ParamDegradeRule>> entry : PARAM_DEGRADE_RULES.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    private static CircuitBreaker newCircuitBreakerFrom(/*@Valid*/ ParamDegradeRule rule, ParamDegradeItem item) {
        ParamDegradeRule cloneRule = rule.cloneWithoutItem();
        cloneRule.setCount(item.getCount());
        return newCircuitBreakerFrom(cloneRule);
    }

    private static CircuitBreaker newCircuitBreakerFrom(/*@Valid*/ ParamDegradeRule rule) {
        switch (rule.getGrade()) {
            case RuleConstant.DEGRADE_GRADE_RT:
                return new ResponseTimeCircuitBreaker(rule);
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO:
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT:
                return new ExceptionCircuitBreaker(rule);
            default:
                return null;
        }
    }

    private static CircuitBreaker getExistingSameCbOrNew(/*@Valid*/ ParamDegradeRule rule, ParamDegradeItem item) {
        List<CircuitBreaker> cbs = getCircuitBreakers(getDegradeKey(rule), getItemKey(rule, item));
        if (cbs == null || cbs.isEmpty()) {
            return newCircuitBreakerFrom(rule, item);
        }
        for (CircuitBreaker cb : cbs) {
            if (rule.equals(cb.getRule())) {
                // Reuse the circuit breaker if the rule remains unchanged.
                return cb;
            }
        }
        return newCircuitBreakerFrom(rule, item);
    }

    private static CircuitBreaker getExistingSameCbOrNew(/*@Valid*/ ParamDegradeRule rule, String key) {
        List<CircuitBreaker> cbs = getCircuitBreakers(getDegradeKey(rule), key);
        if (cbs == null || cbs.isEmpty()) {
            return newCircuitBreakerFrom(rule);
        }
        for (CircuitBreaker cb : cbs) {
            if (rule.equals(cb.getRule())) {
                // Reuse the circuit breaker if the rule remains unchanged.
                return cb;
            }
        }
        return newCircuitBreakerFrom(rule);
    }

    public static String getDegradeKey(ParamDegradeRule rule) {
        return rule.getResource();
    }

    public static String getItemKey(ParamDegradeRule rule, ParamDegradeItem item) {
        return rule.getParamIdx() + KEY_SPLIT + item.getObject();
    }

    public static String getItemKey(ParamDegradeRule rule, String itemKey) {
        return rule.getParamIdx() + KEY_SPLIT + itemKey;
    }


    static class RulePropertyListener implements PropertyListener<List<ParamDegradeRule>> {

        private synchronized void reloadFrom(List<ParamDegradeRule> list) {
            Map<String, Map<String, List<CircuitBreaker>>> cbs = buildCircuitBreakers(list);
            Map<String, Set<ParamDegradeRule>> rm = new HashMap<>(cbs.size());

            for (Map.Entry<String, Map<String, List<CircuitBreaker>>> e : cbs.entrySet()) {
                assert e.getValue() != null && !e.getValue().isEmpty();

                Set<ParamDegradeRule> rules = new HashSet<>(e.getValue().size());
                for (Map.Entry<String, List<CircuitBreaker>> entry : e.getValue().entrySet()) {
                    assert entry.getValue() != null && !entry.getValue().isEmpty();

                    // There are several rules for one resource name, store only the one with paramDegradeItemList
                    if (entry.getKey().contains(DEFAULT_OTHER_KEY)) {
                        List<CircuitBreaker> cbList = entry.getValue();
                        for (CircuitBreaker cb : cbList) {
                            rules.add((ParamDegradeRule) cb.getRule());
                        }
                        break;
                    }
                }
                rm.put(e.getKey(), rules);
            }

            ParamDegradeRuleManager.CIRCUIT_BREAKERS = cbs;
            ParamDegradeRuleManager.PARAM_DEGRADE_RULES = rm;
        }

        @Override
        public void configUpdate(List<ParamDegradeRule> list) {
            reloadFrom(list);
            RecordLog.info("[ParamDegradeRuleManager] Parameter flow rules received: " + PARAM_DEGRADE_RULES);
        }

        @Override
        public void configLoad(List<ParamDegradeRule> list) {
            reloadFrom(list);
            RecordLog.info("[ParamDegradeRuleManager] Parameter flow rules received: " + PARAM_DEGRADE_RULES);
        }

        /**
         * for one ParamDegradeRule, if it has 2 items, there will be 3 CircuitBreakers generated.
         * 1. {paramIndex}_{item1_value} 2. {paramIndex}_{item2_value} 3. {paramIndex}_{SENTINEL-OTHER}
         *
         * @param list ruleList
         * @return
         */
        private Map<String, Map<String, List<CircuitBreaker>>> buildCircuitBreakers(List<ParamDegradeRule> list) {
            Map<String, Map<String, List<CircuitBreaker>>> cbMap = new HashMap<>(8);
            if (list == null || list.isEmpty()) {
                return cbMap;
            }
            for (ParamDegradeRule rule : list) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[DegradeRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }

                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                }
                //build {paramIndex}_{paramValue} key, to process the value of param those are not hitted.
                for (ParamDegradeItem item : rule.getParamDegradeItemList()) {
                    buildCircuitBreakers(cbMap, rule, getExistingSameCbOrNew(rule, item), getItemKey(rule, item));
                }

                //build {paramIndex}_{SENTINEL_OTHER} key, to process the value of param those are not hitted.
                buildCircuitBreakers(cbMap, rule, getExistingSameCbOrNew(rule, DEFAULT_OTHER_KEY), getItemKey(rule, DEFAULT_OTHER_KEY));
            }
            return cbMap;
        }

        private void buildCircuitBreakers(Map<String, Map<String, List<CircuitBreaker>>> cbMap, ParamDegradeRule rule, CircuitBreaker existingSameCbOrNew, String itemKey) {
            if (existingSameCbOrNew == null) {
                RecordLog.warn("[DegradeRuleManager] Unknown circuit breaking strategy, ignoring: " + rule);
                return;
            }
            Map<String, List<CircuitBreaker>> itemRuleListMap = cbMap.computeIfAbsent(rule.getResource(), k -> new HashMap<>());
            List<CircuitBreaker> itemRuleList = itemRuleListMap.computeIfAbsent(itemKey, k -> new ArrayList<>());
            itemRuleList.add(existingSameCbOrNew);
        }
    }

    private ParamDegradeRuleManager() {
    }
}
