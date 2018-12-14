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

    private static final PropertyListener<ClusterClientConfig> PROPERTY_LISTENER = new ClientConfigPropertyListener();
    private static SentinelProperty<ClusterClientConfig> currentProperty = new DynamicSentinelProperty<>();

    private static final List<ServerChangeObserver> SERVER_CHANGE_OBSERVERS = new ArrayList<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    public static void register2Property(SentinelProperty<ClusterClientConfig> property) {
        synchronized (PROPERTY_LISTENER) {
            RecordLog.info("[ClusterClientConfigManager] Registering new property to cluster client config manager");
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
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
        currentProperty.updateValue(config);
    }

    private static class ClientConfigPropertyListener implements PropertyListener<ClusterClientConfig> {

        @Override
        public void configUpdate(ClusterClientConfig config) {
            applyConfig(config);
        }

        @Override
        public void configLoad(ClusterClientConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterClientConfigManager] Empty initial config");
                return;
            }
            applyConfig(config);
        }

        private synchronized void applyConfig(ClusterClientConfig config) {
            if (!isValidConfig(config)) {
                RecordLog.warn(
                    "[ClusterClientConfigManager] Invalid cluster client config, ignoring: " + config);
                return;
            }
            RecordLog.info("[ClusterClientConfigManager] Updating new config: " + config);
            if (config.getRequestTimeout() != requestTimeout) {
                requestTimeout = config.getRequestTimeout();
            }
            updateServer(config);
        }
    }

    public static boolean isValidConfig(ClusterClientConfig config) {
        return config != null && StringUtil.isNotBlank(config.getServerHost())
            && config.getServerPort() > 0
            && config.getServerPort() <= 65535
            && config.getRequestTimeout() > 0;
    }

    public static void updateServer(ClusterClientConfig config) {
        String host = config.getServerHost();
        int port = config.getServerPort();
        AssertUtil.assertNotBlank(host, "token server host cannot be empty");
        AssertUtil.isTrue(port > 0, "token server port should be valid (positive)");
        if (serverPort == port && host.equals(serverHost)) {
            return;
        }
        for (ServerChangeObserver observer : SERVER_CHANGE_OBSERVERS) {
            observer.onRemoteServerChange(config);
        }

        serverHost = host;
        serverPort = port;
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

    private ClusterClientConfigManager() {}
}
