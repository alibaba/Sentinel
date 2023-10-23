/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import io.opensergo.ConfigKind;
import io.opensergo.proto.fault_tolerance.v1.CircuitBreakerStrategy;
import io.opensergo.proto.fault_tolerance.v1.ConcurrencyLimitStrategy;
import io.opensergo.proto.fault_tolerance.v1.FaultToleranceRule;
import io.opensergo.proto.fault_tolerance.v1.FaultToleranceRule.FaultToleranceRuleTargetRef;
import io.opensergo.proto.fault_tolerance.v1.RateLimitStrategy;
import io.opensergo.proto.fault_tolerance.v1.ThrottlingStrategy;
import io.opensergo.util.TimeUnitUtils;

/**
 * @author Eric Zhao
 */
public class OpenSergoRuleAggregator {

    /**
     * (SentinelRuleKind, SentinelProperty)
     */
    private final Map<String, SentinelProperty> dataSourceMap;

    public OpenSergoRuleAggregator(Map<String, SentinelProperty> dataSourceMap) {
        this.dataSourceMap = Collections.unmodifiableMap(dataSourceMap);
    }

    /**
     * (strategyKindSimpleName, [rules that contain this kind of strategy])
     */
    private volatile Map<String, List<FaultToleranceRule>> ftRuleMapByStrategyKind = new HashMap<>();
    /**
     * (name, rateLimitStrategy)
     */
    private volatile Map<String, RateLimitStrategy> rateLimitStrategyMap = new HashMap<>();
    private volatile Map<String, ThrottlingStrategy> throttlingStrategyMap = new HashMap<>();
    private volatile Map<String, ConcurrencyLimitStrategy> concurrencyLimitStrategyMap = new HashMap<>();
    private volatile Map<String, CircuitBreakerStrategy> circuitBreakerStrategyMap = new HashMap<>();

    public synchronized boolean updateFaultToleranceRuleList(List<FaultToleranceRule> rules) {
        Map<String, List<FaultToleranceRule>> map = new HashMap<>(4);

        if (rules != null && !rules.isEmpty()) {
            for (FaultToleranceRule rule : rules) {
                Set<String> kinds = rule.getStrategiesList().stream()
                        .map(e -> e.getKind()).collect(Collectors.toSet());
                kinds.forEach(kindName -> map.computeIfAbsent(kindName, v -> new ArrayList<>()).add(rule));
            }
        }
        this.ftRuleMapByStrategyKind = map;

        // TODO: check whether the rules have been changed
        handleFlowRuleUpdate();
        handleCircuitBreakerRuleUpdate();

        return true;
    }

    public synchronized boolean updateRateLimitStrategy(List<RateLimitStrategy> strategies) {
        Map<String, RateLimitStrategy> map = new HashMap<>(4);
        if (strategies != null && !strategies.isEmpty()) {
            strategies.forEach(s -> map.put(s.getName(), s));
        }
        this.rateLimitStrategyMap = map;

        return handleFlowRuleUpdate();
    }

    public synchronized boolean updateThrottlingStrategy(List<ThrottlingStrategy> strategies) {
        Map<String, ThrottlingStrategy> map = new HashMap<>(4);
        if (strategies != null && !strategies.isEmpty()) {
            strategies.forEach(s -> map.put(s.getName(), s));
        }
        this.throttlingStrategyMap = map;

        return handleFlowRuleUpdate();
    }

    public synchronized boolean updateCircuitBreakerStrategy(List<CircuitBreakerStrategy> strategies) {
        Map<String, CircuitBreakerStrategy> map = new HashMap<>(4);
        if (strategies != null && !strategies.isEmpty()) {
            strategies.forEach(s -> map.put(s.getName(), s));
        }
        this.circuitBreakerStrategyMap = map;

        return handleCircuitBreakerRuleUpdate();
    }

    public synchronized boolean updateConcurrencyLimitStrategy(List<ConcurrencyLimitStrategy> strategies) {
        Map<String, ConcurrencyLimitStrategy> map = new HashMap<>(4);
        if (strategies != null && !strategies.isEmpty()) {
            strategies.forEach(s -> map.put(s.getName(), s));
        }
        this.concurrencyLimitStrategyMap = map;

        return handleFlowRuleUpdate();
    }

    private boolean handleFlowRuleUpdate() {
        List<FlowRule> rules = new ArrayList<>();

        List<FlowRule> rulesFromRateLimitStrategies = assembleFlowRulesFromRateLimitStrategies(
                ftRuleMapByStrategyKind.get(ConfigKind.RATE_LIMIT_STRATEGY.getSimpleKindName()), rateLimitStrategyMap);
        List<FlowRule> rulesFromThrottlingStrategies = assembleFlowRulesFromThrottlingStrategies(
                ftRuleMapByStrategyKind.get(ConfigKind.THROTTLING_STRATEGY.getSimpleKindName()), throttlingStrategyMap);
        List<FlowRule> rulesConcurrencyLimitStrategies = assembleFlowRulesFromConcurrencyLimitStrategies(
                ftRuleMapByStrategyKind
                        .get(ConfigKind.CONCURRENCY_LIMIT_STRATEGY.getSimpleKindName()), concurrencyLimitStrategyMap);

        rules.addAll(rulesFromRateLimitStrategies);
        rules.addAll(rulesFromThrottlingStrategies);
        rules.addAll(rulesConcurrencyLimitStrategies);

        // Update rules to upstream data-source.
        return dataSourceMap.get(OpenSergoSentinelConstants.KIND_FLOW_RULE).updateValue(rules);
    }

    private boolean handleCircuitBreakerRuleUpdate() {
        List<DegradeRule> rules = assembleDegradeRulesFromCbStrategies(
                ftRuleMapByStrategyKind.get(ConfigKind.CIRCUIT_BREAKER_STRATEGY.getSimpleKindName()),
                circuitBreakerStrategyMap);

        // Update rules to upstream data-source.
        return dataSourceMap.get(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE).updateValue(rules);
    }

    private List<FlowRule> assembleFlowRulesFromRateLimitStrategies(List<FaultToleranceRule> ftRules,
                                                                    Map<String, RateLimitStrategy> rateLimitStrategyMap) {
        List<FlowRule> rules = new ArrayList<>();
        if (ftRules == null || ftRules.isEmpty()) {
            return rules;
        }
        for (FaultToleranceRule ftRule : ftRules) {
            List<RateLimitStrategy> strategies = ftRule.getStrategiesList().stream()
                    .filter(e -> e.getKind().equals(ConfigKind.RATE_LIMIT_STRATEGY.getSimpleKindName()))
                    .map(e -> rateLimitStrategyMap.get(e.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (strategies.isEmpty()) {
                continue;
            }

            for (FaultToleranceRuleTargetRef targetRef : ftRule.getTargetsList()) {
                String resourceName = targetRef.getTargetResourceName();

                for (RateLimitStrategy strategy : strategies) {
                    FlowRule flowRule = new FlowRule(resourceName);
                    try {
                        flowRule = fillFlowRuleWithRateLimitStrategy(flowRule, strategy);
                        if (flowRule != null) {
                            rules.add(flowRule);
                        }
                    } catch (Exception ex) {
                        RecordLog.warn("Ignoring OpenSergo RateLimitStrategy due to covert failure, "
                                + "resourceName={}, strategy={}", resourceName, strategy);
                    }
                }
            }
        }
        return rules;
    }

    private List<FlowRule> assembleFlowRulesFromConcurrencyLimitStrategies(List<FaultToleranceRule> ftRules,
                                                                           Map<String, ConcurrencyLimitStrategy> concurrencyLimitStrategyMap) {
        List<FlowRule> rules = new ArrayList<>();
        if (ftRules == null || ftRules.isEmpty()) {
            return rules;
        }
        for (FaultToleranceRule ftRule : ftRules) {
            List<ConcurrencyLimitStrategy> strategies = ftRule.getStrategiesList().stream()
                    .filter(e -> e.getKind().equals(ConfigKind.CONCURRENCY_LIMIT_STRATEGY.getSimpleKindName()))
                    .map(e -> concurrencyLimitStrategyMap.get(e.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (strategies.isEmpty()) {
                continue;
            }

            for (FaultToleranceRuleTargetRef targetRef : ftRule.getTargetsList()) {
                String resourceName = targetRef.getTargetResourceName();

                for (ConcurrencyLimitStrategy strategy : strategies) {
                    FlowRule flowRule = new FlowRule(resourceName);
                    try {
                        flowRule = fillFlowRuleWithConcurrencyLimitStrategy(flowRule, strategy);
                        if (flowRule != null) {
                            rules.add(flowRule);
                        }
                    } catch (Exception ex) {
                        RecordLog.warn("Ignoring OpenSergo ConcurrencyLimitStrategy due to covert failure, "
                                + "resourceName={}, strategy={}", resourceName, strategy);
                    }
                }
            }
        }
        return rules;
    }

    private List<FlowRule> assembleFlowRulesFromThrottlingStrategies(List<FaultToleranceRule> ftRules,
                                                                     Map<String, ThrottlingStrategy> throttlingStrategyMap) {
        List<FlowRule> rules = new ArrayList<>();
        if (ftRules == null || ftRules.isEmpty()) {
            return rules;
        }
        for (FaultToleranceRule ftRule : ftRules) {
            List<ThrottlingStrategy> strategies = ftRule.getStrategiesList().stream()
                    .filter(e -> e.getKind().equals(ConfigKind.THROTTLING_STRATEGY.getSimpleKindName()))
                    .map(e -> throttlingStrategyMap.get(e.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (strategies.isEmpty()) {
                continue;
            }

            for (FaultToleranceRuleTargetRef targetRef : ftRule.getTargetsList()) {
                String resourceName = targetRef.getTargetResourceName();

                for (ThrottlingStrategy strategy : strategies) {
                    FlowRule flowRule = new FlowRule(resourceName);
                    fillFlowRuleWithThrottlingStrategy(flowRule, strategy);
                    rules.add(flowRule);
                }
            }
        }
        return rules;
    }

    private List<DegradeRule> assembleDegradeRulesFromCbStrategies(List<FaultToleranceRule> ftRules,
                                                                   Map<String, CircuitBreakerStrategy> strategyMap) {
        List<DegradeRule> rules = new ArrayList<>();
        if (ftRules == null || ftRules.isEmpty()) {
            return rules;
        }
        for (FaultToleranceRule ftRule : ftRules) {
            List<CircuitBreakerStrategy> strategies = ftRule.getStrategiesList().stream()
                    .filter(e -> e.getKind().equals(ConfigKind.CIRCUIT_BREAKER_STRATEGY.getSimpleKindName()))
                    .map(e -> strategyMap.get(e.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (strategies.isEmpty()) {
                continue;
            }

            for (FaultToleranceRuleTargetRef targetRef : ftRule.getTargetsList()) {
                String resourceName = targetRef.getTargetResourceName();

                for (CircuitBreakerStrategy strategy : strategies) {
                    DegradeRule degradeRule = new DegradeRule(resourceName);
                    try {
                        degradeRule = fillDegradeRuleWithCbStrategy(degradeRule, strategy);
                        if (degradeRule != null) {
                            rules.add(degradeRule);
                        }
                    } catch (Exception ex) {
                        RecordLog.warn("Ignoring OpenSergo CircuitBreakerStrategy due to covert failure, "
                                + "resourceName={}, strategy={}", resourceName, strategy);
                    }
                }
            }
        }
        return rules;
    }

    private FlowRule fillFlowRuleWithRateLimitStrategy(FlowRule rule, RateLimitStrategy strategy) {
        if (rule == null || strategy == null) {
            return rule;
        }
        rule.setCount(strategy.getThreshold());
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // TODO: support global rate limiting (limitMode=Global).
        rule.setClusterMode(false);
        // Relation strategy.
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        // Control behavior.
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        return rule;
    }

    private FlowRule fillFlowRuleWithConcurrencyLimitStrategy(FlowRule rule, ConcurrencyLimitStrategy strategy) {
        if (rule == null || strategy == null) {
            return rule;
        }
        rule.setCount(strategy.getMaxConcurrency());
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule.setClusterMode(false);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        return rule;
    }

    private FlowRule fillFlowRuleWithThrottlingStrategy(FlowRule rule, ThrottlingStrategy strategy) {
        if (rule == null || strategy == null) {
            return rule;
        }

        // round-up
        double countPerSec = Math.ceil(1000.0 / strategy.getMinIntervalMillisOfRequests());
        rule.setCount(countPerSec);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setClusterMode(false);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule.setMaxQueueingTimeMs((int) strategy.getQueueTimeoutMillis());

        return rule;
    }

    private DegradeRule fillDegradeRuleWithCbStrategy(DegradeRule rule, CircuitBreakerStrategy strategy) {
        if (rule == null || strategy == null) {
            return rule;
        }
        switch (strategy.getStrategy()) {
            case STRATEGY_SLOW_REQUEST_RATIO:
                rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
                rule.setSlowRatioThreshold(strategy.getTriggerRatio());
                // maxAllowedRt
                rule.setCount(strategy.getSlowCondition().getMaxAllowedRtMillis());
                break;
            case STRATEGY_ERROR_REQUEST_RATIO:
                rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
                rule.setCount(strategy.getTriggerRatio());
                break;
            default:
                throw new IllegalArgumentException("unknown strategy type: " + strategy.getStrategy());
        }
        int recoveryTimeoutSec = (int) (TimeUnitUtils.convertToMillis(strategy.getRecoveryTimeout(),
                strategy.getRecoveryTimeoutTimeUnit()) / 1000);
        rule.setTimeWindow(recoveryTimeoutSec);
        int statIntervalMs = (int) TimeUnitUtils.convertToMillis(strategy.getStatDuration(),
                strategy.getStatDurationTimeUnit());
        rule.setStatIntervalMs(statIntervalMs);
        rule.setMinRequestAmount(strategy.getMinRequestAmount());
        return rule;
    }

}
