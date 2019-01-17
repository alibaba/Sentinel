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
package com.alibaba.csp.sentinel.cluster.flow.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterParamMetric;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.cluster.server.util.ClusterRuleUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * Manager for cluster parameter flow rules.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterParamFlowRuleManager {

    /**
     * The default cluster parameter flow rule property supplier that creates a new
     * dynamic property for a specific namespace to manually do rule management.
     */
    public static final Function<String, SentinelProperty<List<ParamFlowRule>>> DEFAULT_PROPERTY_SUPPLIER =
        new Function<String, SentinelProperty<List<ParamFlowRule>>>() {
            @Override
            public SentinelProperty<List<ParamFlowRule>> apply(String namespace) {
                return new DynamicSentinelProperty<>();
            }
        };

    /**
     * (id, clusterParamRule)
     */
    private static final Map<Long, ParamFlowRule> PARAM_RULES = new ConcurrentHashMap<>();
    /**
     * (namespace, [flowId...])
     */
    private static final Map<String, Set<Long>> NAMESPACE_FLOW_ID_MAP = new ConcurrentHashMap<>();
    /**
     * (flowId, namespace)
     */
    private static final Map<Long, String> FLOW_NAMESPACE_MAP = new ConcurrentHashMap<>();

    /**
     * (namespace, property-listener wrapper)
     */
    private static final Map<String, NamespaceFlowProperty<ParamFlowRule>> PROPERTY_MAP = new ConcurrentHashMap<>();
    /**
     * Cluster parameter flow rule property supplier for a specific namespace.
     */
    private static volatile Function<String, SentinelProperty<List<ParamFlowRule>>> propertySupplier
        = DEFAULT_PROPERTY_SUPPLIER;

    private static final Object UPDATE_LOCK = new Object();

    static {
        initDefaultProperty();
    }

    private static void initDefaultProperty() {
        SentinelProperty<List<ParamFlowRule>> defaultProperty = new DynamicSentinelProperty<>();
        String defaultNamespace = ServerConstants.DEFAULT_NAMESPACE;
        registerPropertyInternal(defaultNamespace, defaultProperty);
    }

    public static void setPropertySupplier(
        Function<String, SentinelProperty<List<ParamFlowRule>>> propertySupplier) {
        ClusterParamFlowRuleManager.propertySupplier = propertySupplier;
    }

    public static String getNamespace(long flowId) {
        return FLOW_NAMESPACE_MAP.get(flowId);
    }

    /**
     * Listen to the {@link SentinelProperty} for cluster {@link ParamFlowRule}s.
     * The property is the source of cluster {@link ParamFlowRule}s for a specific namespace.
     *
     * @param namespace namespace to register
     */
    public static void register2Property(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (propertySupplier == null) {
            RecordLog.warn(
                "[ClusterParamFlowRuleManager] Cluster param rule property supplier is absent, cannot register "
                    + "property");
            return;
        }
        SentinelProperty<List<ParamFlowRule>> property = propertySupplier.apply(namespace);
        if (property == null) {
            RecordLog.warn(
                "[ClusterParamFlowRuleManager] Wrong created property from cluster param rule property supplier, "
                    + "ignoring");
            return;
        }
        synchronized (UPDATE_LOCK) {
            RecordLog.info("[ClusterParamFlowRuleManager] Registering new property to cluster param rule manager"
                + " for namespace <{0}>", namespace);
            registerPropertyInternal(namespace, property);
        }
    }

    public static void registerPropertyIfAbsent(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (!PROPERTY_MAP.containsKey(namespace)) {
            synchronized (UPDATE_LOCK) {
                if (!PROPERTY_MAP.containsKey(namespace)) {
                    register2Property(namespace);
                }
            }
        }
    }

    private static void registerPropertyInternal(/*@NonNull*/ String namespace, /*@Valid*/
                                                              SentinelProperty<List<ParamFlowRule>> property) {
        NamespaceFlowProperty<ParamFlowRule> oldProperty = PROPERTY_MAP.get(namespace);
        if (oldProperty != null) {
            oldProperty.getProperty().removeListener(oldProperty.getListener());
        }
        PropertyListener<List<ParamFlowRule>> listener = new ParamRulePropertyListener(namespace);
        property.addListener(listener);
        PROPERTY_MAP.put(namespace, new NamespaceFlowProperty<>(namespace, property, listener));
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null) {
            resetNamespaceFlowIdMapFor(namespace);
        }
    }

    public static void removeProperty(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        synchronized (UPDATE_LOCK) {
            NamespaceFlowProperty<ParamFlowRule> property = PROPERTY_MAP.get(namespace);
            if (property != null) {
                property.getProperty().removeListener(property.getListener());
                PROPERTY_MAP.remove(namespace);
            }
            RecordLog.info("[ClusterParamFlowRuleManager] Removing property from cluster flow rule manager"
                + " for namespace <{0}>", namespace);
        }
    }

    private static void removePropertyListeners() {
        for (NamespaceFlowProperty<ParamFlowRule> property : PROPERTY_MAP.values()) {
            property.getProperty().removeListener(property.getListener());
        }
    }

    private static void restorePropertyListeners() {
        for (NamespaceFlowProperty<ParamFlowRule> p : PROPERTY_MAP.values()) {
            p.getProperty().removeListener(p.getListener());
            p.getProperty().addListener(p.getListener());
        }
    }

    private static void resetNamespaceFlowIdMapFor(/*@Valid*/ String namespace) {
        NAMESPACE_FLOW_ID_MAP.put(namespace, new HashSet<Long>());
    }

    private static void clearAndResetRulesFor(/*@Valid*/ String namespace) {
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet != null && !flowIdSet.isEmpty()) {
            for (Long flowId : flowIdSet) {
                PARAM_RULES.remove(flowId);
                FLOW_NAMESPACE_MAP.remove(flowId);
            }
            flowIdSet.clear();
        } else {
            resetNamespaceFlowIdMapFor(namespace);
        }
    }

    private static void clearAndResetRulesConditional(/*@Valid*/ String namespace, Predicate<Long> predicate) {
        Set<Long> oldIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (oldIdSet != null && !oldIdSet.isEmpty()) {
            for (Long flowId : oldIdSet) {
                if (predicate.test(flowId)) {
                    PARAM_RULES.remove(flowId);
                    FLOW_NAMESPACE_MAP.remove(flowId);
                    ClusterParamMetricStatistics.removeMetric(flowId);
                }
            }
            oldIdSet.clear();
        }
    }

    public static ParamFlowRule getParamRuleById(Long id) {
        if (!ClusterRuleUtil.validId(id)) {
            return null;
        }
        return PARAM_RULES.get(id);
    }

    public static Set<Long> getFlowIdSet(String namespace) {
        if (StringUtil.isEmpty(namespace)) {
            return new HashSet<>();
        }
        Set<Long> set = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (set == null) {
            return new HashSet<>();
        }
        return new HashSet<>(set);
    }

    public static List<ParamFlowRule> getAllParamRules() {
        return new ArrayList<>(PARAM_RULES.values());
    }

    /**
     * Get all cluster parameter flow rules within a specific namespace.
     *
     * @param namespace a valid namespace
     * @return cluster parameter flow rules within the provided namespace
     */
    public static List<ParamFlowRule> getParamRules(String namespace) {
        if (StringUtil.isEmpty(namespace)) {
            return new ArrayList<>();
        }
        List<ParamFlowRule> rules = new ArrayList<>();
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null || flowIdSet.isEmpty()) {
            return rules;
        }
        for (Long flowId : flowIdSet) {
            ParamFlowRule rule = PARAM_RULES.get(flowId);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * Load parameter flow rules for a specific namespace. The former rules of the namespace will be replaced.
     *
     * @param namespace a valid namespace
     * @param rules rule list
     */
    public static void loadRules(String namespace, List<ParamFlowRule> rules) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        NamespaceFlowProperty<ParamFlowRule> property = PROPERTY_MAP.get(namespace);
        if (property != null) {
            property.getProperty().updateValue(rules);
        }
    }

    /**
     * Get connected count for associated namespace of given {@code flowId}.
     *
     * @param flowId existing rule ID
     * @return connected count
     */
    public static int getConnectedCount(long flowId) {
        if (flowId <= 0) {
            return 0;
        }
        String namespace = FLOW_NAMESPACE_MAP.get(flowId);
        if (namespace == null) {
            return 0;
        }
        return ConnectionManager.getConnectedCount(namespace);
    }

    private static class ParamRulePropertyListener implements PropertyListener<List<ParamFlowRule>> {

        private final String namespace;

        public ParamRulePropertyListener(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public void configLoad(List<ParamFlowRule> conf) {
            applyClusterParamRules(conf, namespace);
            RecordLog.info("[ClusterParamFlowRuleManager] Cluster parameter rules loaded for namespace <{0}>: {1}",
                namespace, PARAM_RULES);
        }

        @Override
        public void configUpdate(List<ParamFlowRule> conf) {
            applyClusterParamRules(conf, namespace);
            RecordLog.info("[ClusterParamFlowRuleManager] Cluster parameter rules received for namespace <{0}>: {1}",
                namespace, PARAM_RULES);
        }
    }

    private static void applyClusterParamRules(List<ParamFlowRule> list, /*@Valid*/ String namespace) {
        if (list == null || list.isEmpty()) {
            clearAndResetRulesFor(namespace);
            return;
        }
        final ConcurrentHashMap<Long, ParamFlowRule> ruleMap = new ConcurrentHashMap<>();

        Set<Long> flowIdSet = new HashSet<>();

        for (ParamFlowRule rule : list) {
            if (!rule.isClusterMode()) {
                continue;
            }
            if (!ParamFlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                    "[ClusterParamFlowRuleManager] Ignoring invalid param flow rule when loading new flow rules: "
                        + rule);
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            ParamFlowRuleUtil.fillExceptionFlowItems(rule);

            ParamFlowClusterConfig clusterConfig = rule.getClusterConfig();
            // Flow id should not be null after filtered.
            Long flowId = clusterConfig.getFlowId();
            if (flowId == null) {
                continue;
            }
            ruleMap.put(flowId, rule);
            FLOW_NAMESPACE_MAP.put(flowId, namespace);
            flowIdSet.add(flowId);

            // Prepare cluster parameter metric from valid rule ID.
            ClusterParamMetricStatistics.putMetricIfAbsent(flowId,
                new ClusterParamMetric(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs()));
        }

        // Cleanup unused cluster parameter metrics.
        clearAndResetRulesConditional(namespace, new Predicate<Long>() {
            @Override
            public boolean test(Long flowId) {
                return !ruleMap.containsKey(flowId);
            }
        });

        PARAM_RULES.putAll(ruleMap);
        NAMESPACE_FLOW_ID_MAP.put(namespace, flowIdSet);
    }

    private ClusterParamFlowRuleManager() {}
}
