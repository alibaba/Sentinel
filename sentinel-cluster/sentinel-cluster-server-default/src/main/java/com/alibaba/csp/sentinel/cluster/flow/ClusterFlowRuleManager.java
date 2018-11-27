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
package com.alibaba.csp.sentinel.cluster.flow;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.cluster.server.util.ClusterRuleUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterFlowRuleManager {

    private static final Map<Long, FlowRule> FLOW_RULES = new ConcurrentHashMap<>();

    private static final PropertyListener<List<FlowRule>> PROPERTY_LISTENER = new FlowRulePropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link FlowRule}s.
     * The property is the source of cluster {@link FlowRule}s.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        synchronized (PROPERTY_LISTENER) {
            RecordLog.info("[ClusterFlowRuleManager] Registering new property to cluster flow rule manager");
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
        }
    }

    public static FlowRule getFlowRuleById(Long id) {
        if (!ClusterRuleUtil.validId(id)) {
            return null;
        }
        return FLOW_RULES.get(id);
    }

    private static Map<Long, FlowRule> buildClusterFlowRuleMap(List<FlowRule> list) {
        Map<Long, FlowRule> ruleMap = new ConcurrentHashMap<>();
        if (list == null || list.isEmpty()) {
            return ruleMap;
        }

        for (FlowRule rule : list) {
            if (!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                    "[ClusterFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            // Flow id should not be null after filtered.
            Long flowId = rule.getClusterConfig().getFlowId();
            if (flowId == null) {
                continue;
            }
            ruleMap.put(flowId, rule);

            // Prepare cluster metric from valid flow ID.
            ClusterMetricStatistics.putMetricIfAbsent(flowId, new ClusterMetric(100, 1));
        }

        // Cleanup unused cluster metrics.
        Set<Long> previousSet = FLOW_RULES.keySet();
        for (Long id : previousSet) {
            if (!ruleMap.containsKey(id)) {
                ClusterMetricStatistics.removeMetric(id);
            }
        }

        return ruleMap;
    }

    private static final class FlowRulePropertyListener implements PropertyListener<List<FlowRule>> {

        @Override
        public void configUpdate(List<FlowRule> conf) {
            Map<Long, FlowRule> rules = buildClusterFlowRuleMap(conf);
            if (rules != null) {
                FLOW_RULES.clear();
                FLOW_RULES.putAll(rules);
            }
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules received: " + FLOW_RULES);
        }

        @Override
        public void configLoad(List<FlowRule> conf) {
            Map<Long, FlowRule> rules = buildClusterFlowRuleMap(conf);
            if (rules != null) {
                FLOW_RULES.clear();
                FLOW_RULES.putAll(rules);
            }
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules loaded: " + FLOW_RULES);
        }
    }

    private ClusterFlowRuleManager() {}
}
