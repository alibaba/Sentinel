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

import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterParamMetric;
import com.alibaba.csp.sentinel.cluster.server.util.ClusterRuleUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterParamFlowRuleManager {

    private static final Map<Long, ParamFlowRule> PARAM_RULES = new ConcurrentHashMap<>();

    private final static RulePropertyListener PROPERTY_LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<ParamFlowRule>> currentProperty
        = new DynamicSentinelProperty<List<ParamFlowRule>>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link ParamFlowRule}s.
     * The property is the source of {@link ParamFlowRule}s.
     *
     * @param property the property to listen
     */
    public static void register2Property(SentinelProperty<List<ParamFlowRule>> property) {
        synchronized (PROPERTY_LISTENER) {
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
            RecordLog.info("[ClusterParamFlowRuleManager] New property has been registered to cluster param rule manager");
        }
    }

    public static ParamFlowRule getParamFlowRuleById(Long id) {
        if (!ClusterRuleUtil.validId(id)) {
            return null;
        }
        return PARAM_RULES.get(id);
    }

    static class RulePropertyListener implements PropertyListener<List<ParamFlowRule>> {

        @Override
        public void configUpdate(List<ParamFlowRule> conf) {
            Map<Long, ParamFlowRule> rules = buildClusterRuleMap(conf);
            if (rules != null) {
                PARAM_RULES.clear();
                PARAM_RULES.putAll(rules);
            }
            RecordLog.info("[ClusterFlowRuleManager] Cluster param flow rules received: " + PARAM_RULES);
        }

        @Override
        public void configLoad(List<ParamFlowRule> conf) {
            Map<Long, ParamFlowRule> rules = buildClusterRuleMap(conf);
            if (rules != null) {
                PARAM_RULES.clear();
                PARAM_RULES.putAll(rules);
            }
            RecordLog.info("[ClusterFlowRuleManager] Cluster param flow rules received: " + PARAM_RULES);
        }
    }

    private static Map<Long, ParamFlowRule> buildClusterRuleMap(List<ParamFlowRule> list) {
        Map<Long, ParamFlowRule> ruleMap = new ConcurrentHashMap<>();
        if (list == null || list.isEmpty()) {
            return ruleMap;
        }

        for (ParamFlowRule rule : list) {
            if (!rule.isClusterMode()) {
                continue;
            }
            if (!ParamFlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                    "[ClusterParamFlowRuleManager] Ignoring invalid param flow rule when loading new flow rules: " + rule);
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
            ClusterParamMetricStatistics.putMetricIfAbsent(flowId, new ClusterParamMetric(100, 1));
        }

        // Cleanup unused cluster metrics.
        Set<Long> previousSet = PARAM_RULES.keySet();
        for (Long id : previousSet) {
            if (!ruleMap.containsKey(id)) {
                ClusterParamMetricStatistics.removeMetric(id);
            }
        }

        return ruleMap;
    }

    private ClusterParamFlowRuleManager() {}
}
