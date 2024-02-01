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
package com.alibaba.csp.sentinel.slots.system;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;

import java.util.*;

/**
 * manager of {@link SystemRule}, provide api to load {@link SystemRule}, and get {@link SystemRule}
 *
 * @author jialiang.linjl
 * @author leyou
 * @author guozhong.huang
 */
public final class SystemRuleManager {

    private static volatile double highestSystemLoad = Double.MAX_VALUE;


    private static volatile List<SystemRule> systemRules = new ArrayList<>();


    private final static SystemPropertyListener LISTENER = new SystemPropertyListener();
    private static SentinelProperty<List<SystemRule>> currentProperty = new DynamicSentinelProperty<List<SystemRule>>();

    static {
        currentProperty.addListener(LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link SystemRule}s. The property is the source
     * of {@link SystemRule}s. System rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<SystemRule>> property) {
        synchronized (LISTENER) {
            RecordLog.info("[SystemRuleManager] Registering new property to system rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load {@link SystemRule}s, former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<SystemRule> rules) {
        currentProperty.updateValue(rules);
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<SystemRule> getRules() {
        return systemRules;
    }

    static class SystemPropertyListener extends SimplePropertyListener<List<SystemRule>> {


        @Override
        public void configUpdate(List<SystemRule> value) {
            // rest highestSystemLoad
            highestSystemLoad = Double.MAX_VALUE;

            List<SystemRule> newRules = new ArrayList<>();

            if (value == null || value.isEmpty()) {
                RecordLog.warn("[SystemRuleManager] System rules received is empty");
                systemRules = newRules;
                return;
            }
            // check rule is valid
            Map<SystemMetricType, SystemRule> ruleMap = new HashMap<>(8);
            for (SystemRule rule : value) {
                if (Objects.requireNonNull(rule.getSystemMetricType()) == SystemMetricType.CPU_USAGE) {// triggerCount should be in (0, 1]
                    if (rule.getTriggerCount() < 0 || rule.getTriggerCount() > 1) {
                        RecordLog.warn(String.format("[SystemRuleManager] Ignoring invalid SystemRule: "
                                + "CPU_USAGE %.3f > 1 OR < 0", rule.getTriggerCount()));
                        continue;
                    }
                } else {// triggerCount should be in [0, MAX_INT]
                    if (rule.getTriggerCount() < 0) {
                        RecordLog.warn(String.format("[SystemRuleManager] Ignoring SystemRule due to invalid MetricType: %s,   "
                                + "trigger count: %.3f > 1", rule.getSystemMetricType(), rule.getTriggerCount()));
                        continue;
                    }
                }

                SystemMetricType systemMetricType = rule.getSystemMetricType();
                SystemRule systemRule = ruleMap.get(systemMetricType);
                if (systemRule != null) {
                    if (rule.getTriggerCount() < systemRule.getTriggerCount()) {
                        // replace with the low threshold rule
                        ruleMap.put(systemMetricType, rule);
                    }
                } else {
                    ruleMap.put(systemMetricType, rule);
                }
            }
            newRules = new ArrayList<>(ruleMap.values());
            // update highestSystemLoad
            if (ruleMap.get(SystemMetricType.LOAD) != null) {
                highestSystemLoad = ruleMap.get(SystemMetricType.LOAD).getTriggerCount();
            }
            systemRules = newRules;
            RecordLog.info("[SystemRuleManager] Flow rules received: {}", newRules);

        }
    }

    public static double getSystemLoadThreshold() {
        return highestSystemLoad;
    }
}
