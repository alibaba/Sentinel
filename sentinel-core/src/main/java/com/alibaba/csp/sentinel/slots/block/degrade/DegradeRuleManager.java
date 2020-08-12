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
package com.alibaba.csp.sentinel.slots.block.degrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * The rule manager for circuit breaking rules ({@link DegradeRule}).
 *
 * @author youji.zj
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public final class DegradeRuleManager {

    private static volatile Map<String, List<CircuitBreaker>> circuitBreakers = new HashMap<>();
    private static volatile Map<String, Set<DegradeRule>> ruleMap = new HashMap<>();

    private static final RulePropertyListener LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty
        = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link DegradeRule}s. The property is the source
     * of {@link DegradeRule}s. Degrade rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<DegradeRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[DegradeRuleManager] Registering new property to degrade rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    static List<CircuitBreaker> getCircuitBreakers(String resourceName) {
        return circuitBreakers.get(resourceName);
    }

    public static boolean hasConfig(String resource) {
        if (resource == null) {
            return false;
        }
        return circuitBreakers.containsKey(resource);
    }

    /**
     * <p>Get existing circuit breaking rules.</p>
     * <p>Note: DO NOT modify the rules from the returned list directly.
     * The behavior is <strong>undefined</strong>.</p>
     *
     * @return list of existing circuit breaking rules, or empty list if no rules were loaded
     */
    public static List<DegradeRule> getRules() {
        List<DegradeRule> rules = new ArrayList<>();
        for (Map.Entry<String, Set<DegradeRule>> entry : ruleMap.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    public static Set<DegradeRule> getRulesOfResource(String resource) {
        AssertUtil.assertNotBlank(resource, "resource name cannot be blank");
        return ruleMap.get(resource);
    }

    /**
     * Load {@link DegradeRule}s, former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<DegradeRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.error("[DegradeRuleManager] Unexpected error when loading degrade rules", e);
        }
    }

    /**
     * Set degrade rules for provided resource. Former rules of the resource will be replaced.
     *
     * @param resourceName valid resource name
     * @param rules        new rule set to load
     * @return whether the rules has actually been updated
     * @since 1.5.0
     */
    public static boolean setRulesForResource(String resourceName, Set<DegradeRule> rules) {
        AssertUtil.notEmpty(resourceName, "resourceName cannot be empty");
        try {
            Map<String, Set<DegradeRule>> newRuleMap = new HashMap<>(ruleMap);
            if (rules == null) {
                newRuleMap.remove(resourceName);
            } else {
                Set<DegradeRule> newSet = new HashSet<>();
                for (DegradeRule rule : rules) {
                    if (isValidRule(rule) && resourceName.equals(rule.getResource())) {
                        newSet.add(rule);
                    }
                }
                newRuleMap.put(resourceName, newSet);
            }
            List<DegradeRule> allRules = new ArrayList<>();
            for (Set<DegradeRule> set : newRuleMap.values()) {
                allRules.addAll(set);
            }
            return currentProperty.updateValue(allRules);
        } catch (Throwable e) {
            RecordLog.error("[DegradeRuleManager] Unexpected error when setting circuit breaking"
                + " rules for resource: " + resourceName, e);
            return false;
        }
    }

    private static CircuitBreaker getExistingSameCbOrNew(/*@Valid*/ DegradeRule rule) {
        List<CircuitBreaker> cbs = getCircuitBreakers(rule.getResource());
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

    /**
     * Create a circuit breaker instance from provided circuit breaking rule.
     *
     * @param rule a valid circuit breaking rule
     * @return new circuit breaker based on provided rule; null if rule is invalid or unsupported type
     */
    private static CircuitBreaker newCircuitBreakerFrom(/*@Valid*/ DegradeRule rule) {
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

    public static boolean isValidRule(DegradeRule rule) {
        boolean baseValid = rule != null && !StringUtil.isBlank(rule.getResource())
            && rule.getCount() >= 0 && rule.getTimeWindow() > 0;
        if (!baseValid) {
            return false;
        }
        if (rule.getMinRequestAmount() <= 0 || rule.getStatIntervalMs() <= 0) {
            return false;
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

    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private synchronized void reloadFrom(List<DegradeRule> list) {
            Map<String, List<CircuitBreaker>> cbs = buildCircuitBreakers(list);
            Map<String, Set<DegradeRule>> rm = new HashMap<>(cbs.size());

            for (Map.Entry<String, List<CircuitBreaker>> e : cbs.entrySet()) {
                assert e.getValue() != null && !e.getValue().isEmpty();

                Set<DegradeRule> rules = new HashSet<>(e.getValue().size());
                for (CircuitBreaker cb : e.getValue()) {
                    rules.add(cb.getRule());
                }
                rm.put(e.getKey(), rules);
            }

            DegradeRuleManager.circuitBreakers = cbs;
            DegradeRuleManager.ruleMap = rm;
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules has been updated to: " + ruleMap);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules loaded: " + ruleMap);
        }

        private Map<String, List<CircuitBreaker>> buildCircuitBreakers(List<DegradeRule> list) {
            Map<String, List<CircuitBreaker>> cbMap = new HashMap<>(8);
            if (list == null || list.isEmpty()) {
                return cbMap;
            }
            for (DegradeRule rule : list) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[DegradeRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }

                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                }
                CircuitBreaker cb = getExistingSameCbOrNew(rule);
                if (cb == null) {
                    RecordLog.warn("[DegradeRuleManager] Unknown circuit breaking strategy, ignoring: " + rule);
                    continue;
                }

                String resourceName = rule.getResource();

                List<CircuitBreaker> cbList = cbMap.get(resourceName);
                if (cbList == null) {
                    cbList = new ArrayList<>();
                    cbMap.put(resourceName, cbList);
                }
                cbList.add(cb);
            }
            return cbMap;
        }
    }
}
