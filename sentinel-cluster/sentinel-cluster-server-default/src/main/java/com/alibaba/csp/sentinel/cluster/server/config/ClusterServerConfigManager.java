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
package com.alibaba.csp.sentinel.cluster.server.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.limit.GlobalRequestLimiter;
import com.alibaba.csp.sentinel.cluster.registry.ConfigSupplierRegistry;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterServerConfigManager {

    private static boolean embedded = false;

    /**
     * Server global transport and scope config.
     */
    private static volatile int port = ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT;
    private static volatile int idleSeconds = ServerTransportConfig.DEFAULT_IDLE_SECONDS;
    private static volatile Set<String> namespaceSet = Collections.singleton(ServerConstants.DEFAULT_NAMESPACE);

    /**
     * Server global flow config.
     */
    private static volatile double exceedCount = ServerFlowConfig.DEFAULT_EXCEED_COUNT;
    private static volatile double maxOccupyRatio = ServerFlowConfig.DEFAULT_MAX_OCCUPY_RATIO;
    private static volatile int intervalMs = ServerFlowConfig.DEFAULT_INTERVAL_MS;
    private static volatile int sampleCount = ServerFlowConfig.DEFAULT_SAMPLE_COUNT;
    private static volatile double maxAllowedQps = ServerFlowConfig.DEFAULT_MAX_ALLOWED_QPS;

    /**
     * Namespace-specific flow config for token server.
     * Format: (namespace, config).
     */
    private static final Map<String, ServerFlowConfig> NAMESPACE_CONF = new ConcurrentHashMap<>();

    private static final List<ServerTransportConfigObserver> TRANSPORT_CONFIG_OBSERVERS = new ArrayList<>();

    /**
     * Property for cluster server global transport configuration.
     */
    private static SentinelProperty<ServerTransportConfig> transportConfigProperty = new DynamicSentinelProperty<>();
    /**
     * Property for cluster server namespace set.
     */
    private static SentinelProperty<Set<String>> namespaceSetProperty = new DynamicSentinelProperty<>();
    /**
     * Property for cluster server global flow control configuration.
     */
    private static SentinelProperty<ServerFlowConfig> globalFlowProperty = new DynamicSentinelProperty<>();

    private static final PropertyListener<ServerTransportConfig> TRANSPORT_PROPERTY_LISTENER
        = new ServerGlobalTransportPropertyListener();
    private static final PropertyListener<ServerFlowConfig> GLOBAL_FLOW_PROPERTY_LISTENER
        = new ServerGlobalFlowPropertyListener();
    private static final PropertyListener<Set<String>> NAMESPACE_SET_PROPERTY_LISTENER
        = new ServerNamespaceSetPropertyListener();

    static {
        transportConfigProperty.addListener(TRANSPORT_PROPERTY_LISTENER);
        globalFlowProperty.addListener(GLOBAL_FLOW_PROPERTY_LISTENER);
        namespaceSetProperty.addListener(NAMESPACE_SET_PROPERTY_LISTENER);
    }

    /**
     * Register cluster server namespace set dynamic property.
     *
     * @param property server namespace set dynamic property
     */
    public static void registerNamespaceSetProperty(SentinelProperty<Set<String>> property) {
        AssertUtil.notNull(property, "namespace set dynamic property cannot be null");
        synchronized (NAMESPACE_SET_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new namespace set dynamic property to Sentinel server "
                    + "config manager");
            namespaceSetProperty.removeListener(NAMESPACE_SET_PROPERTY_LISTENER);
            property.addListener(NAMESPACE_SET_PROPERTY_LISTENER);
            namespaceSetProperty = property;
        }
    }

    /**
     * Register cluster server transport configuration dynamic property.
     *
     * @param property server transport configuration dynamic property
     */
    public static void registerServerTransportProperty(SentinelProperty<ServerTransportConfig> property) {
        AssertUtil.notNull(property, "cluster server transport config dynamic property cannot be null");
        synchronized (TRANSPORT_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new server transport dynamic property to Sentinel server "
                    + "config manager");
            transportConfigProperty.removeListener(TRANSPORT_PROPERTY_LISTENER);
            property.addListener(TRANSPORT_PROPERTY_LISTENER);
            transportConfigProperty = property;
        }
    }

    /**
     * Register cluster server global statistic (flow) configuration dynamic property.
     *
     * @param property server flow configuration dynamic property
     */
    public static void registerGlobalServerFlowProperty(SentinelProperty<ServerFlowConfig> property) {
        AssertUtil.notNull(property, "cluster server flow config dynamic property cannot be null");
        synchronized (GLOBAL_FLOW_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new server global flow dynamic property "
                    + "to Sentinel server config manager");
            globalFlowProperty.removeListener(GLOBAL_FLOW_PROPERTY_LISTENER);
            property.addListener(GLOBAL_FLOW_PROPERTY_LISTENER);
            globalFlowProperty = property;
        }
    }

    /**
     * Load provided server namespace set to property in memory.
     *
     * @param namespaceSet valid namespace set
     */
    public static void loadServerNamespaceSet(Set<String> namespaceSet) {
        namespaceSetProperty.updateValue(namespaceSet);
    }

    /**
     * Load provided server transport configuration to property in memory.
     *
     * @param config valid cluster server transport configuration
     */
    public static void loadGlobalTransportConfig(ServerTransportConfig config) {
        transportConfigProperty.updateValue(config);
    }

    /**
     * Load provided server global statistic (flow) configuration to property in memory.
     *
     * @param config valid cluster server flow configuration for global
     */
    public static void loadGlobalFlowConfig(ServerFlowConfig config) {
        globalFlowProperty.updateValue(config);
    }

    /**
     * Load server flow config for a specific namespace.
     *
     * @param namespace a valid namespace
     * @param config    valid flow config for the namespace
     */
    public static void loadFlowConfig(String namespace, ServerFlowConfig config) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        // TODO: Support namespace-scope server flow config.
        globalFlowProperty.updateValue(config);
    }

    /**
     * Add a transport config observer. The observers will be called as soon as
     * there are some changes in transport config (e.g. token server port).
     *
     * @param observer a valid transport config observer
     */
    public static void addTransportConfigChangeObserver(ServerTransportConfigObserver observer) {
        AssertUtil.notNull(observer, "observer cannot be null");
        TRANSPORT_CONFIG_OBSERVERS.add(observer);
    }

    private static class ServerNamespaceSetPropertyListener implements PropertyListener<Set<String>> {

        @Override
        public synchronized void configLoad(Set<String> set) {
            if (set == null || set.isEmpty()) {
                RecordLog.warn("[ClusterServerConfigManager] WARN: empty initial server namespace set");
                return;
            }
            applyNamespaceSetChange(set);
        }

        @Override
        public synchronized void configUpdate(Set<String> set) {
            // TODO: should debounce?
            applyNamespaceSetChange(set);
        }
    }

    private static void applyNamespaceSetChange(Set<String> newSet) {
        if (newSet == null) {
            return;
        }
        RecordLog.info("[ClusterServerConfigManager] Server namespace set will be update to: {}", newSet);
        if (newSet.isEmpty()) {
            ClusterServerConfigManager.namespaceSet = Collections.singleton(ServerConstants.DEFAULT_NAMESPACE);
            return;
        }

        newSet = new HashSet<>(newSet);
        // Always add the `default` namespace to the namespace set.
        newSet.add(ServerConstants.DEFAULT_NAMESPACE);

        if (embedded) {
            // In embedded server mode, the server itself is also a part of service,
            // so it should be added to namespace set.
            // By default, the added namespace is the appName.
            newSet.add(ConfigSupplierRegistry.getNamespaceSupplier().get());
        }

        Set<String> oldSet = ClusterServerConfigManager.namespaceSet;
        if (oldSet != null && !oldSet.isEmpty()) {
            for (String ns : oldSet) {
                // Remove the cluster rule property for deprecated namespace set.
                if (!newSet.contains(ns)) {
                    ClusterFlowRuleManager.removeProperty(ns);
                    ClusterParamFlowRuleManager.removeProperty(ns);
                }
            }
        }

        ClusterServerConfigManager.namespaceSet = newSet;
        for (String ns : newSet) {
            // Register the rule property if needed.
            ClusterFlowRuleManager.registerPropertyIfAbsent(ns);
            ClusterParamFlowRuleManager.registerPropertyIfAbsent(ns);
            // Initialize the global QPS limiter for the namespace.
            GlobalRequestLimiter.initIfAbsent(ns);
        }
    }

    private static class ServerGlobalTransportPropertyListener implements PropertyListener<ServerTransportConfig> {

        @Override
        public void configLoad(ServerTransportConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterServerConfigManager] Empty initial server transport config");
                return;
            }
            applyConfig(config);
        }

        @Override
        public void configUpdate(ServerTransportConfig config) {
            applyConfig(config);
        }

        private synchronized void applyConfig(ServerTransportConfig config) {
            if (!isValidTransportConfig(config)) {
                RecordLog.warn(
                    "[ClusterServerConfigManager] Invalid cluster server transport config, ignoring: {}", config);
                return;
            }
            RecordLog.info("[ClusterServerConfigManager] Updating new server transport config: {}", config);
            if (config.getIdleSeconds() != idleSeconds) {
                idleSeconds = config.getIdleSeconds();
            }
            updateTokenServer(config);
        }
    }

    private static void updateTokenServer(ServerTransportConfig config) {
        int newPort = config.getPort();
        AssertUtil.isTrue(newPort > 0, "token server port should be valid (positive)");
        if (newPort == port) {
            return;
        }
        ClusterServerConfigManager.port = newPort;

        for (ServerTransportConfigObserver observer : TRANSPORT_CONFIG_OBSERVERS) {
            observer.onTransportConfigChange(config);
        }
    }

    private static class ServerGlobalFlowPropertyListener implements PropertyListener<ServerFlowConfig> {

        @Override
        public void configUpdate(ServerFlowConfig config) {
            applyGlobalFlowConfig(config);
        }

        @Override
        public void configLoad(ServerFlowConfig config) {
            applyGlobalFlowConfig(config);
        }

        private synchronized void applyGlobalFlowConfig(ServerFlowConfig config) {
            if (!isValidFlowConfig(config)) {
                RecordLog.warn(
                    "[ClusterServerConfigManager] Invalid cluster server global flow config, ignoring: {}", config);
                return;
            }
            RecordLog.info("[ClusterServerConfigManager] Updating new server global flow config: {}", config);
            if (config.getExceedCount() != exceedCount) {
                exceedCount = config.getExceedCount();
            }
            if (config.getMaxOccupyRatio() != maxOccupyRatio) {
                maxOccupyRatio = config.getMaxOccupyRatio();
            }
            if (config.getMaxAllowedQps() != maxAllowedQps) {
                maxAllowedQps = config.getMaxAllowedQps();
                GlobalRequestLimiter.applyMaxQpsChange(maxAllowedQps);
            }
            int newIntervalMs = config.getIntervalMs();
            int newSampleCount = config.getSampleCount();
            if (newIntervalMs != intervalMs || newSampleCount != sampleCount) {
                if (newIntervalMs <= 0 || newSampleCount <= 0 || newIntervalMs % newSampleCount != 0) {
                    RecordLog.warn("[ClusterServerConfigManager] Ignoring invalid flow interval or sample count");
                } else {
                    intervalMs = newIntervalMs;
                    sampleCount = newSampleCount;
                    // Reset all the metrics.
                    ClusterMetricStatistics.resetFlowMetrics();
                    ClusterParamMetricStatistics.resetFlowMetrics();
                }
            }
        }
    }

    public static boolean isValidTransportConfig(ServerTransportConfig config) {
        return config != null && config.getPort() > 0 && config.getPort() <= 65535;
    }

    public static boolean isValidFlowConfig(ServerFlowConfig config) {
        return config != null && config.getMaxOccupyRatio() >= 0 && config.getExceedCount() >= 0
            && config.getMaxAllowedQps() >= 0
            && FlowRuleUtil.isWindowConfigValid(config.getSampleCount(), config.getIntervalMs());
    }

    public static double getExceedCount(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getExceedCount();
        }
        return exceedCount;
    }

    public static double getMaxOccupyRatio(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getMaxOccupyRatio();
        }
        return maxOccupyRatio;
    }

    public static int getIntervalMs(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getIntervalMs();
        }
        return intervalMs;
    }

    /**
     * Get sample count of provided namespace.
     *
     * @param namespace valid namespace
     * @return the sample count of namespace; if the namespace does not have customized value, use the global value
     */
    public static int getSampleCount(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getSampleCount();
        }
        return sampleCount;
    }

    public static double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public static double getMaxAllowedQps(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getMaxAllowedQps();
        }
        return maxAllowedQps;
    }

    public static double getExceedCount() {
        return exceedCount;
    }

    public static double getMaxOccupyRatio() {
        return maxOccupyRatio;
    }

    public static Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public static int getPort() {
        return port;
    }

    public static int getIdleSeconds() {
        return idleSeconds;
    }

    public static int getIntervalMs() {
        return intervalMs;
    }

    public static int getSampleCount() {
        return sampleCount;
    }

    public static void setNamespaceSet(Set<String> namespaceSet) {
        applyNamespaceSetChange(namespaceSet);
    }

    public static boolean isEmbedded() {
        return embedded;
    }

    /**
     * <p>Set the embedded mode flag for the token server. </p>
     * <p>
     * NOTE: developers SHOULD NOT manually invoke this method.
     * The embedded flag should be initialized by Sentinel when starting token server.
     * </p>
     *
     * @param embedded whether the token server is currently running in embedded mode
     */
    public static void setEmbedded(boolean embedded) {
        ClusterServerConfigManager.embedded = embedded;
    }

    public static void setMaxAllowedQps(double maxAllowedQps) {
        ClusterServerConfigManager.maxAllowedQps = maxAllowedQps;
    }

    private ClusterServerConfigManager() {}
}
