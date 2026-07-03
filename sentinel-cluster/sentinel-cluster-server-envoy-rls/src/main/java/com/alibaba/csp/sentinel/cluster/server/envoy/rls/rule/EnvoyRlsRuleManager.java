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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class EnvoyRlsRuleManager {

    private static final ConcurrentMap<String, EnvoyRlsRule> RULE_MAP = new ConcurrentHashMap<>();

    private static final PropertyListener<List<EnvoyRlsRule>> PROPERTY_LISTENER = new EnvoyRlsRulePropertyListener();
    private static SentinelProperty<List<EnvoyRlsRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for Envoy RLS rules. The property is the source of {@link EnvoyRlsRule}.
     *
     * @param property the property to listen
     */
    public static void register2Property(SentinelProperty<List<EnvoyRlsRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (PROPERTY_LISTENER) {
            RecordLog.info("[EnvoyRlsRuleManager] Registering new property to Envoy rate limit service rule manager");
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Load Envoy RLS rules, while former rules will be replaced.
     *
     * @param rules new rules to load
     * @return true if there are actual changes, otherwise false
     */
    public static boolean loadRules(List<EnvoyRlsRule> rules) {
        return currentProperty.updateValue(rules);
    }

    public static List<EnvoyRlsRule> getRules() {
        return new ArrayList<>(RULE_MAP.values());
    }

    static final class EnvoyRlsRulePropertyListener extends SimplePropertyListener<List<EnvoyRlsRule>> {

        @Override
        public synchronized void configUpdate(List<EnvoyRlsRule> conf) {
            Map<String, EnvoyRlsRule> ruleMap = generateRuleMap(conf);

            List<FlowRule> flowRules = ruleMap.values().stream()
                .flatMap(e -> EnvoySentinelRuleConverter.toSentinelFlowRules(e).stream())
                .collect(Collectors.toList());

            RULE_MAP.clear();
            RULE_MAP.putAll(ruleMap);
            RecordLog.info("[EnvoyRlsRuleManager] Envoy RLS rules loaded: {}", flowRules);

            // Use the "default" namespace.
            ClusterFlowRuleManager.loadRules(ServerConstants.DEFAULT_NAMESPACE, flowRules);
        }

        Map<String, EnvoyRlsRule> generateRuleMap(List<EnvoyRlsRule> conf) {
            if (conf == null || conf.isEmpty()) {
                return new HashMap<>(2);
            }
            Map<String, EnvoyRlsRule> map = new HashMap<>(conf.size());
            for (EnvoyRlsRule rule : conf) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[EnvoyRlsRuleManager] Ignoring invalid rule when loading new RLS rules: " + rule);
                    continue;
                }
                if (map.containsKey(rule.getDomain())) {
                    RecordLog.warn("[EnvoyRlsRuleManager] Ignoring duplicate RLS rule for specific domain: " + rule);
                    continue;
                }
                map.put(rule.getDomain(), rule);
            }
            return map;
        }
    }

    /**
     * Check whether the given Envoy RLS rule is valid.
     *
     * @param rule the rule to check
     * @return true if the rule is valid, otherwise false
     */
    public static boolean isValidRule(EnvoyRlsRule rule) {
        if (rule == null || StringUtil.isBlank(rule.getDomain())) {
            return false;
        }
        List<EnvoyRlsRule.ResourceDescriptor> descriptors = rule.getDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            return false;
        }
        for (EnvoyRlsRule.ResourceDescriptor descriptor : descriptors) {
            if (descriptor == null || descriptor.getCount() == null || descriptor.getCount() < 0) {
                return false;
            }
            Set<EnvoyRlsRule.KeyValueResource> resources = descriptor.getResources();
            if (resources == null || resources.isEmpty()) {
                return false;
            }
            for (EnvoyRlsRule.KeyValueResource resource : resources) {
                if (resource == null ||
                    StringUtil.isBlank(resource.getKey()) || StringUtil.isBlank(resource.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private EnvoyRlsRuleManager() {}
}
