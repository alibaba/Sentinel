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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.cb.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.cb.ExceptionCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.cb.RtCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Caches all degrade rules and circuit breakers, and perform degrading checking.
 *
 * @author youji.zj
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class DegradeRuleManager {

    private static final Map<String, List<CircuitBreaker>> circuitBreakers
        = new ConcurrentHashMap<String, List<CircuitBreaker>>();

    private static final RulePropertyListener listener = new RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty
        = new DynamicSentinelProperty<List<DegradeRule>>();

    static {
        currentProperty.addListener(listener);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link DegradeRule}s. The property is the source
     * of {@link DegradeRule}s. Degrade rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<DegradeRule>> property) {
        synchronized (listener) {
            currentProperty.removeListener(listener);
            property.addListener(listener);
            currentProperty = property;
        }
    }

    public static void checkDegrade(ResourceWrapper resource, Context context, DefaultNode node, int count)
        throws BlockException {
        List<CircuitBreaker> cbs = circuitBreakers.get(resource.getName());
        if (cbs == null) {
            return;
        }

        for (CircuitBreaker cb : cbs) {
            if (!cb.canPass()) {
                throw new DegradeException(cb.getRule().getLimitApp());
            }
        }
    }

    public static boolean hasConfig(String resource) {
        return circuitBreakers.containsKey(resource);
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<DegradeRule> getRules() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        for (Map.Entry<String, List<CircuitBreaker>> entry : circuitBreakers.entrySet()) {
            for (CircuitBreaker cb : entry.getValue()) {
                rules.add(cb.getRule());
            }
        }
        return rules;
        // Uncomment this when least version is 1.8.
        //return circuitBreakers.entrySet().stream()
        //    .flatMap(e -> e.getValue().stream())
        //    .map(CircuitBreaker::getRule)
        //    .collect(Collectors.toList());
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
            RecordLog.info(e.getMessage(), e);
        }
    }

    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private void reloadFrom(List<DegradeRule> conf) {
            Map<String, List<CircuitBreaker>> cbs = buildCircuitBreakers(conf);
            if (cbs != null) {
                circuitBreakers.clear();
                circuitBreakers.putAll(cbs);
            }
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules received: " + conf);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules loaded: " + conf);
        }

        private Map<String, List<CircuitBreaker>> buildCircuitBreakers(List<DegradeRule> list) {
            Map<String, List<CircuitBreaker>> map = new ConcurrentHashMap<String, List<CircuitBreaker>>();
            if (list == null) {
                return map;
            }

            for (DegradeRule rule : list) {
                if (notValidRule(rule)) {
                    RecordLog.warn("[DegradeRuleManager] Ignoring invalid degrade rule: " + rule.toString());
                    continue;
                }
                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(FlowRule.LIMIT_APP_DEFAULT);
                }
                CircuitBreaker cb = newCircuitBreakerFrom(rule);
                if (cb == null) {
                    RecordLog.warn("[DegradeRuleManager] Unknown degrade strategy in rule: " + rule.toString());
                    continue;
                }
                String resourceName = rule.getResource();

                // Uncomment this when least version is 1.8.
                // List<CircuitBreaker> l = map.computeIfAbsent(resourceName, k -> new ArrayList<>());

                List<CircuitBreaker> l = map.get(resourceName);
                if (l == null) {
                    l = new ArrayList<CircuitBreaker>();
                    map.put(resourceName, l);
                }
                checkRuleAttributes(rule);
                l.add(cb);
            }

            return map;
        }

        @Deprecated
        private Map<String, List<DegradeRule>> loadDegradeConf(List<DegradeRule> list) {
            if (list == null) {
                return null;
            }
            Map<String, List<DegradeRule>> newRuleMap = new ConcurrentHashMap<String, List<DegradeRule>>();

            for (DegradeRule rule : list) {
                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(FlowRule.LIMIT_APP_DEFAULT);
                }

                String identity = rule.getResource();
                List<DegradeRule> ruleM = newRuleMap.get(identity);
                if (ruleM == null) {
                    ruleM = new ArrayList<DegradeRule>();
                    newRuleMap.put(identity, ruleM);
                }
                ruleM.add(rule);
            }

            return newRuleMap;
        }
    }

    private static void checkRuleAttributes(/*@Valid*/ DegradeRule rule) {
        long max = Constants.TIME_DROP_VALVE;
        if (rule.getGrade() == RuleConstant.DEGRADE_GRADE_RT && rule.getCount() > max) {
            RecordLog.warn(String.format("[DegradeRuleManager] Setting large RT threshold (%.1f ms) in RT degrade mode"
                + " will not take effect since it exceeds the max value (%d ms)", rule.getCount(), max));
        }
    }

    private static boolean notValidRule(DegradeRule rule) {
        return rule == null || StringUtil.isBlank(rule.getResource()) || rule.getCount() < 0;
    }

    /**
     * Create a circuit breaker instance from provided degrade rule.
     *
     * @param rule degrade rule
     * @return new circuit breaker based on provided rule; null if rule is invalid or unsupported type
     */
    private static CircuitBreaker newCircuitBreakerFrom(DegradeRule rule) {
        if (notValidRule(rule)) {
            return null;
        }
        switch (rule.getGrade()) {
            case RuleConstant.DEGRADE_GRADE_RT:
                return new RtCircuitBreaker(rule);
            case RuleConstant.DEGRADE_GRADE_EXCEPTION:
                return new ExceptionCircuitBreaker(rule);
            default:
                return null;
        }
    }

}
