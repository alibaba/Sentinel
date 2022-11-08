package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ResponseTimeCircuitBreaker;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
public class DefaultDegradeRuleManager {

    public static final String DEFAULT_KEY = "*";

    private static volatile Map<String, List<CircuitBreaker>> circuitBreakers = new ConcurrentHashMap<>();
    private static volatile Set<DegradeRule> rules = new HashSet<>();

    private static final DefaultDegradeRuleManager.RulePropertyListener LISTENER = new DefaultDegradeRuleManager.RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty
            = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }


    static List<CircuitBreaker> getDefaultCircuitBreakers(String resourceName) {
        List<CircuitBreaker> circuitBreakers = DefaultDegradeRuleManager.circuitBreakers.get(resourceName);
        if (circuitBreakers == null && !rules.isEmpty()) {
            return DefaultDegradeRuleManager.circuitBreakers.computeIfAbsent(resourceName,
                    r -> rules.stream().map(DefaultDegradeRuleManager::newCircuitBreakerFrom).collect(Collectors.toList()));
        }
        return circuitBreakers;
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
            RecordLog.error("[DefaultDegradeRuleManager] Unexpected error when loading degrade rules", e);
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


    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private synchronized void reloadFrom(List<DegradeRule> list) {

            if (list == null) {
                return;
            }

            Set<DegradeRule> rules = new HashSet<>();
            List<CircuitBreaker> cbs = new ArrayList<>();

            for (DegradeRule rule : list) {
                if (!isValidDefaultRule(rule)) {
                    RecordLog.warn("[DefaultDegradeRuleManager] Ignoring invalid rule when loading new rules: {}", rule);
                } else {

                    if (StringUtil.isBlank(rule.getLimitApp())) {
                        rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                    }
                    CircuitBreaker cb = newCircuitBreakerFrom(rule);
                    cbs.add(cb);
                    rules.add(rule);
                }
            }

            Map<String, List<CircuitBreaker>> cbMap = new ConcurrentHashMap<>(8);

            DefaultDegradeRuleManager.circuitBreakers.forEach((k, v) -> cbMap.put(k, cbs));

            DefaultDegradeRuleManager.rules = rules;
            DefaultDegradeRuleManager.circuitBreakers = cbMap;
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultDegradeRuleManager] Degrade rules has been updated to: {}", rules);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultDegradeRuleManager] Degrade rules loaded: {}", rules);
        }
    }
}
