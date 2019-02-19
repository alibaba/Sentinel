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

import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.cluster.server.util.ClusterRuleUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * Manager for cluster flow rules.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterFlowRuleManager {

    /**
     * The default cluster flow rule property supplier that creates a new dynamic property
     * for a specific namespace to do rule management manually.
     */
    public static final Function<String, SentinelProperty<List<FlowRule>>> DEFAULT_PROPERTY_SUPPLIER =
        new Function<String, SentinelProperty<List<FlowRule>>>() {
            @Override
            public SentinelProperty<List<FlowRule>> apply(String namespace) {
                return new DynamicSentinelProperty<>();
            }
        };

    /**
     * (flowId, clusterRule)
     */
    private static final Map<Long, FlowRule> FLOW_RULES = new ConcurrentHashMap<>();
    /**
     * (namespace, [flowId...])
     */
    private static final Map<String, Set<Long>> NAMESPACE_FLOW_ID_MAP = new ConcurrentHashMap<>();
    /**
     * <p>This map (flowId, namespace) is used for getting connected count
     * when checking a specific rule in {@code ruleId}:</p>
     *
     * <pre>
     * ruleId -> namespace -> connection group -> connected count
     * </pre>
     */
    private static final Map<Long, String> FLOW_NAMESPACE_MAP = new ConcurrentHashMap<>();

    /**
     * (namespace, property-listener wrapper)
     */
    private static final Map<String, NamespaceFlowProperty<FlowRule>> PROPERTY_MAP = new ConcurrentHashMap<>();
    /**
     * Cluster flow rule property supplier for a specific namespace.
     */
    private static volatile Function<String, SentinelProperty<List<FlowRule>>> propertySupplier
        = DEFAULT_PROPERTY_SUPPLIER;

    private static final Object UPDATE_LOCK = new Object();

    static {
        initDefaultProperty();
    }

    private static void initDefaultProperty() {
        // The server should always support default namespace,
        // so register a default property for default namespace.
        SentinelProperty<List<FlowRule>> defaultProperty = new DynamicSentinelProperty<>();
        String defaultNamespace = ServerConstants.DEFAULT_NAMESPACE;
        registerPropertyInternal(defaultNamespace, defaultProperty);
    }

    public static void setPropertySupplier(Function<String, SentinelProperty<List<FlowRule>>> propertySupplier) {
        AssertUtil.notNull(propertySupplier, "flow rule property supplier cannot be null");
        ClusterFlowRuleManager.propertySupplier = propertySupplier;
    }

    /**
     * Listen to the {@link SentinelProperty} for cluster {@link FlowRule}s.
     * The property is the source of cluster {@link FlowRule}s for a specific namespace.
     *
     * @param namespace namespace to register
     */
    public static void register2Property(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (propertySupplier == null) {
            RecordLog.warn(
                "[ClusterFlowRuleManager] Cluster flow property supplier is absent, cannot register property");
            return;
        }
        SentinelProperty<List<FlowRule>> property = propertySupplier.apply(namespace);
        if (property == null) {
            RecordLog.warn(
                "[ClusterFlowRuleManager] Wrong created property from cluster flow property supplier, ignoring");
            return;
        }
        synchronized (UPDATE_LOCK) {
            RecordLog.info("[ClusterFlowRuleManager] Registering new property to cluster flow rule manager"
                + " for namespace <{0}>", namespace);
            registerPropertyInternal(namespace, property);
        }
    }

    /**
     * Listen to the {@link SentinelProperty} for cluster {@link FlowRule}s if current property for namespace is absent.
     * The property is the source of cluster {@link FlowRule}s for a specific namespace.
     *
     * @param namespace namespace to register
     */
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
                                                              SentinelProperty<List<FlowRule>> property) {
        NamespaceFlowProperty<FlowRule> oldProperty = PROPERTY_MAP.get(namespace);
        if (oldProperty != null) {
            oldProperty.getProperty().removeListener(oldProperty.getListener());
        }
        PropertyListener<List<FlowRule>> listener = new FlowRulePropertyListener(namespace);
        property.addListener(listener);
        PROPERTY_MAP.put(namespace, new NamespaceFlowProperty<>(namespace, property, listener));
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null) {
            resetNamespaceFlowIdMapFor(namespace);
        }
    }

    /**
     * Remove cluster flow rule property for a specific namespace.
     *
     * @param namespace valid namespace
     */
    public static void removeProperty(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        synchronized (UPDATE_LOCK) {
            NamespaceFlowProperty<FlowRule> property = PROPERTY_MAP.get(namespace);
            if (property != null) {
                property.getProperty().removeListener(property.getListener());
                PROPERTY_MAP.remove(namespace);
            }
            RecordLog.info("[ClusterFlowRuleManager] Removing property from cluster flow rule manager"
                + " for namespace <{0}>", namespace);
        }
    }

    private static void removePropertyListeners() {
        for (NamespaceFlowProperty<FlowRule> property : PROPERTY_MAP.values()) {
            property.getProperty().removeListener(property.getListener());
        }
    }

    private static void restorePropertyListeners() {
        for (NamespaceFlowProperty<FlowRule> p : PROPERTY_MAP.values()) {
            p.getProperty().removeListener(p.getListener());
            p.getProperty().addListener(p.getListener());
        }
    }

    /**
     * Get flow rule by rule ID.
     *
     * @param id rule ID
     * @return flow rule
     */
    public static FlowRule getFlowRuleById(Long id) {
        if (!ClusterRuleUtil.validId(id)) {
            return null;
        }
        return FLOW_RULES.get(id);
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

    public static List<FlowRule> getAllFlowRules() {
        return new ArrayList<>(FLOW_RULES.values());
    }

    /**
     * Get all cluster flow rules within a specific namespace.
     *
     * @param namespace valid namespace
     * @return cluster flow rules within the provided namespace
     */
    public static List<FlowRule> getFlowRules(String namespace) {
        if (StringUtil.isEmpty(namespace)) {
            return new ArrayList<>();
        }
        List<FlowRule> rules = new ArrayList<>();
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null || flowIdSet.isEmpty()) {
            return rules;
        }
        for (Long flowId : flowIdSet) {
            FlowRule rule = FLOW_RULES.get(flowId);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * Load flow rules for a specific namespace. The former rules of the namespace will be replaced.
     *
     * @param namespace a valid namespace
     * @param rules rule list
     */
    public static void loadRules(String namespace, List<FlowRule> rules) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        NamespaceFlowProperty<FlowRule> property = PROPERTY_MAP.get(namespace);
        if (property != null) {
            property.getProperty().updateValue(rules);
        }
    }

    private static void resetNamespaceFlowIdMapFor(/*@Valid*/ String namespace) {
        NAMESPACE_FLOW_ID_MAP.put(namespace, new HashSet<Long>());
    }

    /**
     * Clear all rules of the provided namespace and reset map.
     *
     * @param namespace valid namespace
     */
    private static void clearAndResetRulesFor(/*@Valid*/ String namespace) {
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet != null && !flowIdSet.isEmpty()) {
            for (Long flowId : flowIdSet) {
                FLOW_RULES.remove(flowId);
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
                    FLOW_RULES.remove(flowId);
                    FLOW_NAMESPACE_MAP.remove(flowId);
                    ClusterMetricStatistics.removeMetric(flowId);
                }
            }
            oldIdSet.clear();
        }
    }

    /**
     * Get connected count for associated namespace of given {@code flowId}.
     *
     * @param flowId unique flow ID
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

    public static String getNamespace(long flowId) {
        return FLOW_NAMESPACE_MAP.get(flowId);
    }

    private static void applyClusterFlowRule(List<FlowRule> list, /*@Valid*/ String namespace) {
        if (list == null || list.isEmpty()) {
            clearAndResetRulesFor(namespace);
            return;
        }
        final ConcurrentHashMap<Long, FlowRule> ruleMap = new ConcurrentHashMap<>();

        Set<Long> flowIdSet = new HashSet<>();

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
            ClusterFlowConfig clusterConfig = rule.getClusterConfig();
            Long flowId = clusterConfig.getFlowId();
            if (flowId == null) {
                continue;
            }
            ruleMap.put(flowId, rule);
            FLOW_NAMESPACE_MAP.put(flowId, namespace);
            flowIdSet.add(flowId);

            // Prepare cluster metric from valid flow ID.
            ClusterMetricStatistics.putMetricIfAbsent(flowId,
                new ClusterMetric(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs()));
        }

        // Cleanup unused cluster metrics.
        clearAndResetRulesConditional(namespace, new Predicate<Long>() {
            @Override
            public boolean test(Long flowId) {
                return !ruleMap.containsKey(flowId);
            }
        });

        FLOW_RULES.putAll(ruleMap);
        NAMESPACE_FLOW_ID_MAP.put(namespace, flowIdSet);
    }

    private static final class FlowRulePropertyListener implements PropertyListener<List<FlowRule>> {

        private final String namespace;

        public FlowRulePropertyListener(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public synchronized void configUpdate(List<FlowRule> conf) {
            applyClusterFlowRule(conf, namespace);
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules received for namespace <{0}>: {1}",
                namespace, FLOW_RULES);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> conf) {
            applyClusterFlowRule(conf, namespace);
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules loaded for namespace <{0}>: {1}",
                namespace, FLOW_RULES);
        }
    }

    private ClusterFlowRuleManager() {}
}
