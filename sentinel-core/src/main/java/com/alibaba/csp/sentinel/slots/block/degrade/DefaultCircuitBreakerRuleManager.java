/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The rule manager for universal default circuit breaker rule.
 *
 * @author wuwen
 * @author Eric Zhao
 * @since 2.0.0
 */
public final class DefaultCircuitBreakerRuleManager {

    public static final String DEFAULT_KEY = "*";

    private static volatile Map<String, List<CircuitBreaker>> circuitBreakers = new ConcurrentHashMap<>();

    private static volatile Set<DegradeRule> rules = new HashSet<>();

    /**
     * Resources in this set will not be affected by default rules.
     */
    private static final Set<String> excludedResource = ConcurrentHashMap.newKeySet();

    private static final DefaultCircuitBreakerRuleManager.RulePropertyListener LISTENER
        = new DefaultCircuitBreakerRuleManager.RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for default circuit breaker rules.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<DegradeRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("Registering new property to DefaultCircuitBreakerRuleManager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    static List<CircuitBreaker> getDefaultCircuitBreakers(String resourceName) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        List<CircuitBreaker> circuitBreakers = DefaultCircuitBreakerRuleManager.circuitBreakers.get(resourceName);
        if (circuitBreakers == null && !rules.isEmpty() && !excludedResource.contains(resourceName)) {
            circuitBreakers = new ArrayList<>();
            for (DegradeRule rule : rules) {
                circuitBreakers.add(DefaultCircuitBreakerRuleManager.newCircuitBreakerFrom(rule));
            }
            DefaultCircuitBreakerRuleManager.circuitBreakers.put(resourceName, circuitBreakers);
            return circuitBreakers;
        }
        return circuitBreakers;
    }

    /**
     * Exclude the resource that does not require default rules.
     *
     * @param resourceName the name of resource that does not require default rules
     */
    public static void addExcludedResource(String resourceName) {
        if (StringUtil.isEmpty(resourceName)) {
            return;
        }
        excludedResource.add(resourceName);
    }

    public static void removeExcludedResource(String resourceName) {
        if (StringUtil.isEmpty(resourceName)) {
            return;
        }
        excludedResource.remove(resourceName);
    }

    public static void clearExcludedResource() {
        excludedResource.clear();
    }

    /**
     * Load default circuit breaker rules, former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static boolean loadRules(List<DegradeRule> rules) {
        try {
            return currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.error("[DefaultCircuitBreakerRuleManager] Unexpected error when loading default rules", e);
            return false;
        }

    }

    public static boolean isValidDefaultRule(DegradeRule rule) {
        if (!DegradeRuleManager.isValidRule(rule)) {
            return false;
        }
        return rule.getResource().equals(DEFAULT_KEY);
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

    static List<CircuitBreaker> getCircuitBreakers(String resourceName) {
        return circuitBreakers.get(resourceName);
    }

    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private synchronized void reloadFrom(List<DegradeRule> list) {

            if (list == null || list.isEmpty()) {
                // clearing all rules
                DefaultCircuitBreakerRuleManager.circuitBreakers = new ConcurrentHashMap<>();
                DefaultCircuitBreakerRuleManager.rules = new HashSet<>();
                return;
            }

            Set<DegradeRule> rules = new HashSet<DegradeRule>();
            for (DegradeRule rule : list) {
                if (!isValidDefaultRule(rule)) {
                    RecordLog.warn(
                        "[DefaultCircuitBreakerRuleManager] Ignoring invalid rule when loading new rules: {}", rule);
                } else {
                    if (StringUtil.isBlank(rule.getLimitApp())) {
                        rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                    }
                    // TODO: Set a special ID for default circuit breaker rule (so that it could be identified)

                    rules.add(rule);
                }
            }

            Map<String, List<CircuitBreaker>> cbMap = new ConcurrentHashMap<String, List<CircuitBreaker>>(8);
            for (String resourceNameKey : DefaultCircuitBreakerRuleManager.circuitBreakers.keySet()) {
                List<CircuitBreaker> cbs = new ArrayList<CircuitBreaker>();
                for (DegradeRule rule : rules) {
                    CircuitBreaker cb = getExistingSameCbOrNew(rule);
                    cbs.add(cb);
                }
                cbMap.put(resourceNameKey, cbs);
            }

            DefaultCircuitBreakerRuleManager.rules = rules;
            DefaultCircuitBreakerRuleManager.circuitBreakers = cbMap;
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultCircuitBreakerRuleManager] Default circuit breaker rules has been updated to: {}",
                rules);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultCircuitBreakerRuleManager] Default circuit breaker rules loaded: {}", rules);
        }
    }
}
