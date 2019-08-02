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
package com.alibaba.csp.sentinel.slots.block.global;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.csp.sentinel.config.SentinelConfig.GLOBAL_RULE_RESOURCE_NAME;
import static com.alibaba.csp.sentinel.slots.block.global.GlobalRuleType.DEGRADE;
import static com.alibaba.csp.sentinel.slots.block.global.GlobalRuleType.FLOW;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class GlobalRuleManager {


    private static final Map<GlobalRuleType, AbstractRule> globalRules = new ConcurrentHashMap<>();

    private static SentinelProperty<List<AbstractRule>> currentProperty = new DynamicSentinelProperty<List<AbstractRule>>();
    private static PropertyListener<List<AbstractRule>> LISTENER = new RulePropertyListener();

    static {
        currentProperty.addListener(LISTENER);
    }


    /**
     * Listen to the {@link SentinelProperty} for {@link AbstractRule}s. The property is the source of {@link AbstractRule}s.
     * Flow rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<AbstractRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[GlobalRuleManager] Registering new property to global rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load new rules, former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<AbstractRule> rules) {
        currentProperty.updateValue(rules);
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<AbstractRule> getRules() {
        List<AbstractRule> rules = new ArrayList<AbstractRule>();
        for (Map.Entry<GlobalRuleType, AbstractRule> entry : globalRules.entrySet()) {
            rules.add(entry.getValue());
        }
        return rules;
    }

    /**
     * Get global rule by {@link GlobalRuleType} type
     *
     * @param ruleType
     * @return
     */
    public static AbstractRule getRule(GlobalRuleType ruleType) {
        return globalRules.get(ruleType);
    }


    private static class RulePropertyListener implements PropertyListener<List<AbstractRule>> {

        @Override
        public void configUpdate(List<AbstractRule> value) {
            if(!GlobalRuleConfig.isGlobalRuleSwitchOpen()){
                RecordLog.warn("[GlobalRuleManager] config update: the global rule switch is closed");
            }
            resolvedValidRules(value);
            RecordLog.info("[GlobalRuleManager] Global rules received: " + globalRules);
        }

        @Override
        public void configLoad(List<AbstractRule> value) {
            if(!GlobalRuleConfig.isGlobalRuleSwitchOpen()){
                RecordLog.warn("[GlobalRuleManager] config load: the global rule switch is closed");
            }
            resolvedValidRules(value);
            RecordLog.info("[GlobalRuleManager] Global rules loaded: " + globalRules);

        }


        private void resolvedValidRules(List<AbstractRule> rules) {

            globalRules.clear();
            AbstractRule flowRule = null;
            AbstractRule degradeRule = null;

            if (rules != null) {
                for (AbstractRule rule : rules) {
                    if (rule instanceof FlowRule && isValidGlobalRule(rule)) {
                        flowRule = rule;
                    } else if (rule instanceof DegradeRule && isValidGlobalRule(rule)) {
                        degradeRule = rule;
                    }
                }

                if (flowRule != null) {
                    List<FlowRule> flowRules = new ArrayList<>(1);
                    flowRules.add((FlowRule) flowRule);
                    Map<String, List<FlowRule>> flowRuleMap = FlowRuleUtil.buildFlowRuleMap(flowRules);
                    if (flowRuleMap.get(flowRule.getResource()) != null) {
                        globalRules.put(FLOW, flowRuleMap.get(flowRule.getResource()).get(0));
                    }
                }
                if (degradeRule != null) {
                    List<DegradeRule> degradeRules = new ArrayList<>(1);
                    degradeRules.add((DegradeRule) degradeRule);
                    Map<String, Set<DegradeRule>> degradeRulesMap = DegradeRuleManager.loadDegradeConf(degradeRules);
                    if (degradeRulesMap.get(degradeRule.getResource()) != null) {
                        globalRules.put(DEGRADE, degradeRulesMap.get(degradeRule.getResource()).iterator().next());
                    }

                }


            }
        }

        private boolean isValidGlobalRule(AbstractRule rule) {
            return SentinelConfig.getConfig(GLOBAL_RULE_RESOURCE_NAME).equals(rule.getResource());
        }

    }



}
