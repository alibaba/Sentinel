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

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class EnvoySentinelRuleConverter {

    /**
     * Currently we use "|" to separate each key/value entries.
     */
    public static final String SEPARATOR = "|";

    /**
     * Convert the {@link EnvoyRlsRule} to a list of Sentinel flow rules.
     *
     * @param rule a valid Envoy RLS rule
     * @return converted rules
     */
    public static List<FlowRule> toSentinelFlowRules(EnvoyRlsRule rule) {
        if (!EnvoyRlsRuleManager.isValidRule(rule)) {
            throw new IllegalArgumentException("Not a valid RLS rule");
        }
        return rule.getDescriptors().stream()
            .map(e -> toSentinelFlowRule(rule.getDomain(), e))
            .collect(Collectors.toList());
    }

    public static FlowRule toSentinelFlowRule(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {
        // One descriptor could have only one rule.
        String identifier = generateKey(domain, descriptor);
        long flowId = generateFlowId(identifier);
        return new FlowRule(identifier)
            .setCount(descriptor.getCount())
            .setClusterMode(true)
            .setClusterConfig(new ClusterFlowConfig()
                .setFlowId(flowId)
                .setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL)
                .setSampleCount(1)
                .setFallbackToLocalWhenFail(false));
    }

    public static long generateFlowId(String key) {
        if (StringUtil.isBlank(key)) {
            return -1L;
        }
        // Add offset to avoid negative ID.
        return (long) Integer.MAX_VALUE + key.hashCode();
    }

    public static String generateKey(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {
        AssertUtil.assertNotBlank(domain, "domain cannot be blank");
        AssertUtil.notNull(descriptor, "EnvoyRlsRule.ResourceDescriptor cannot be null");
        AssertUtil.assertNotEmpty(descriptor.getResources(), "resources in descriptor cannot be null");

        StringBuilder sb = new StringBuilder(domain);
        for (EnvoyRlsRule.KeyValueResource resource : descriptor.getResources()) {
            sb.append(SEPARATOR).append(resource.getKey()).append(SEPARATOR).append(resource.getValue());
        }
        return sb.toString();
    }

    private EnvoySentinelRuleConverter() {}
}
