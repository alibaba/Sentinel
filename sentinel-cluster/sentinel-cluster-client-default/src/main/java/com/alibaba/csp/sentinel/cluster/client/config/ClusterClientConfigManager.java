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
package com.alibaba.csp.sentinel.cluster.client.config;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterClientConfigManager {

    /**
     * Client config properties.
     */
    private static volatile String serverHost = null;
    private static volatile int serverPort = ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT;

    private static volatile int requestTimeout = ClusterConstants.DEFAULT_REQUEST_TIMEOUT;
    private static volatile int connectTimeout = ClusterConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS;

    private static final PropertyListener<ClusterClientConfig> CONFIG_PROPERTY_LISTENER
        = new ClientConfigPropertyListener();
    private static final PropertyListener<ClusterClientAssignConfig> ASSIGN_PROPERTY_LISTENER
        = new ClientAssignPropertyListener();

    private static SentinelProperty<ClusterClientConfig> clientConfigProperty = new DynamicSentinelProperty<>();
    private static SentinelProperty<ClusterClientAssignConfig> clientAssignProperty = new DynamicSentinelProperty<>();

    private static final List<ServerChangeObserver> SERVER_CHANGE_OBSERVERS = new ArrayList<>();

    static {
        bindPropertyListener();
    }

    private static void bindPropertyListener() {
        removePropertyListener();
        clientAssignProperty.addListener(ASSIGN_PROPERTY_LISTENER);
        clientConfigProperty.addListener(CONFIG_PROPERTY_LISTENER);
    }

    private static void removePropertyListener() {
        clientAssignProperty.removeListener(ASSIGN_PROPERTY_LISTENER);
        clientConfigProperty.removeListener(CONFIG_PROPERTY_LISTENER);
    }

    public static void registerServerAssignProperty(SentinelProperty<ClusterClientAssignConfig> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (ASSIGN_PROPERTY_LISTENER) {
            RecordLog.info("[ClusterClientConfigManager] Registering new server assignment property to cluster "
                + "client config manager");
            clientAssignProperty.removeListener(ASSIGN_PROPERTY_LISTENER);
            property.addListener(ASSIGN_PROPERTY_LISTENER);
            clientAssignProperty = property;
        }
    }

    public static void registerClientConfigProperty(SentinelProperty<ClusterClientConfig> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (CONFIG_PROPERTY_LISTENER) {
            RecordLog.info("[ClusterClientConfigManager] Registering new global client config property to "
                + "cluster client config manager");
            clientConfigProperty.removeListener(CONFIG_PROPERTY_LISTENER);
            property.addListener(CONFIG_PROPERTY_LISTENER);
            clientConfigProperty = property;
        }
    }

    public static void addServerChangeObserver(ServerChangeObserver observer) {
        AssertUtil.notNull(observer, "observer cannot be null");
        SERVER_CHANGE_OBSERVERS.add(observer);
    }

    /**
     * Apply new {@link ClusterClientConfig}, while the former config will be replaced.
     *
     * @param config new config to apply
     */
    public static void applyNewConfig(ClusterClientConfig config) {
        clientConfigProperty.updateValue(config);
    }

    public static void applyNewAssignConfig(ClusterClientAssignConfig clusterClientAssignConfig) {
        clientAssignProperty.updateValue(clusterClientAssignConfig);
    }

    private static class ClientAssignPropertyListener implements PropertyListener<ClusterClientAssignConfig> {
        @Override
        public void configLoad(ClusterClientAssignConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterClientConfigManager] Empty initial client assignment config");
                return;
            }
            applyConfig(config);
        }

        @Override
        public void configUpdate(ClusterClientAssignConfig config) {
            applyConfig(config);
        }

        private synchronized void applyConfig(ClusterClientAssignConfig config) {
            if (!isValidAssignConfig(config)) {
                RecordLog.warn(
                    "[ClusterClientConfigManager] Invalid cluster client assign config, ignoring: " + config);
                return;
            }
            if (serverPort == config.getServerPort() && config.getServerHost().equals(serverHost)) {
                return;
            }

            RecordLog.info("[ClusterClientConfigManager] Assign to new target token server: {}", config);

            updateServerAssignment(config);
        }
    }

    private static class ClientConfigPropertyListener implements PropertyListener<ClusterClientConfig> {

        @Override
        public void configLoad(ClusterClientConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterClientConfigManager] Empty initial client config");
                return;
            }
            applyConfig(config);
        }

        @Override
        public void configUpdate(ClusterClientConfig config) {
            applyConfig(config);
        }

        private synchronized void applyConfig(ClusterClientConfig config) {
            if (!isValidClientConfig(config)) {
                RecordLog.warn(
                    "[ClusterClientConfigManager] Invalid cluster client config, ignoring: {}", config);
                return;
            }

            RecordLog.info("[ClusterClientConfigManager] Updating to new client config: {}", config);

            updateClientConfigChange(config);
        }
    }

    private static void updateClientConfigChange(ClusterClientConfig config) {
        if (config.getRequestTimeout() != requestTimeout) {
            requestTimeout = config.getRequestTimeout();
        }
    }

    private static void updateServerAssignment(/*@Valid*/ ClusterClientAssignConfig config) {
        String host = config.getServerHost();
        int port = config.getServerPort();

        for (ServerChangeObserver observer : SERVER_CHANGE_OBSERVERS) {
            observer.onRemoteServerChange(config);
        }

        serverHost = host;
        serverPort = port;
    }

    public static boolean isValidAssignConfig(ClusterClientAssignConfig config) {
        return config != null && StringUtil.isNotBlank(config.getServerHost())
            && config.getServerPort() > 0
            && config.getServerPort() <= 65535;
    }

    public static boolean isValidClientConfig(ClusterClientConfig config) {
        return config != null && config.getRequestTimeout() > 0;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static int getRequestTimeout() {
        return requestTimeout;
    }

    public static int getConnectTimeout() {
        return connectTimeout;
    }

    private ClusterClientConfigManager() {}
}
