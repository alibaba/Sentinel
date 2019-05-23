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
package com.alibaba.csp.sentinel.cluster;

import com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * <p>
 * Global state manager for Sentinel cluster.
 * This enables switching between cluster token client and server mode.
 * </p>
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterStateManager {

    public static final int CLUSTER_CLIENT = 0;
    public static final int CLUSTER_SERVER = 1;
    public static final int CLUSTER_NOT_STARTED = -1;

    private static volatile int mode = CLUSTER_NOT_STARTED;
    private static volatile long lastModified = -1;

    private static volatile SentinelProperty<Integer> stateProperty = new DynamicSentinelProperty<Integer>();
    private static final PropertyListener<Integer> PROPERTY_LISTENER = new ClusterStatePropertyListener();

    static {
        InitExecutor.doInit();
        stateProperty.addListener(PROPERTY_LISTENER);
    }

    public static void registerProperty(SentinelProperty<Integer> property) {
        synchronized (PROPERTY_LISTENER) {
            RecordLog.info("[ClusterStateManager] Registering new property to cluster state manager");
            stateProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            stateProperty = property;
        }
    }

    public static int getMode() {
        return mode;
    }

    public static boolean isClient() {
        return mode == CLUSTER_CLIENT;
    }

    public static boolean isServer() {
        return mode == CLUSTER_SERVER;
    }

    /**
     * <p>
     * Set current mode to client mode. If Sentinel currently works in server mode,
     * it will be turned off. Then the cluster client will be started.
     * </p>
     */
    public static boolean setToClient() {
        if (mode == CLUSTER_CLIENT) {
            return true;
        }
        mode = CLUSTER_CLIENT;
        sleepIfNeeded();
        lastModified = TimeUtil.currentTimeMillis();
        return startClient();
    }

    private static boolean startClient() {
        try {
            EmbeddedClusterTokenServer server = EmbeddedClusterTokenServerProvider.getServer();
            if (server != null) {
                server.stop();
            }
            ClusterTokenClient tokenClient = TokenClientProvider.getClient();
            if (tokenClient != null) {
                tokenClient.start();
                RecordLog.info("[ClusterStateManager] Changing cluster mode to client");
                return true;
            } else {
                RecordLog.warn("[ClusterStateManager] Cannot change to client (no client SPI found)");
                return false;
            }
        } catch (Exception ex) {
            RecordLog.warn("[ClusterStateManager] Error when changing cluster mode to client", ex);
            return false;
        }
    }

    private static boolean stopClient() {
        try {
            ClusterTokenClient tokenClient = TokenClientProvider.getClient();
            if (tokenClient != null) {
                tokenClient.stop();
                RecordLog.info("[ClusterStateManager] Stopping the cluster token client");
                return true;
            } else {
                RecordLog.warn("[ClusterStateManager] Cannot stop cluster token client (no server SPI found)");
                return false;
            }
        } catch (Exception ex) {
            RecordLog.warn("[ClusterStateManager] Error when stopping cluster token client", ex);
            return false;
        }
    }

    /**
     * <p>
     * Set current mode to server mode. If Sentinel currently works in client mode,
     * it will be turned off. Then the cluster server will be started.
     * </p>
     */
    public static boolean setToServer() {
        if (mode == CLUSTER_SERVER) {
            return true;
        }
        mode = CLUSTER_SERVER;
        sleepIfNeeded();
        lastModified = TimeUtil.currentTimeMillis();
        return startServer();
    }

    private static boolean startServer() {
        try {
            ClusterTokenClient tokenClient = TokenClientProvider.getClient();
            if (tokenClient != null) {
                tokenClient.stop();
            }
            EmbeddedClusterTokenServer server = EmbeddedClusterTokenServerProvider.getServer();
            if (server != null) {
                server.start();
                RecordLog.info("[ClusterStateManager] Changing cluster mode to server");
                return true;
            } else {
                RecordLog.warn("[ClusterStateManager] Cannot change to server (no server SPI found)");
                return false;
            }
        } catch (Exception ex) {
            RecordLog.warn("[ClusterStateManager] Error when changing cluster mode to server", ex);
            return false;
        }
    }

    private static boolean stopServer() {
        try {
            EmbeddedClusterTokenServer server = EmbeddedClusterTokenServerProvider.getServer();
            if (server != null) {
                server.stop();
                RecordLog.info("[ClusterStateManager] Stopping the cluster server");
                return true;
            } else {
                RecordLog.warn("[ClusterStateManager] Cannot stop server (no server SPI found)");
                return false;
            }
        } catch (Exception ex) {
            RecordLog.warn("[ClusterStateManager] Error when stopping server", ex);
            return false;
        }
    }

    /**
     * The interval between two change operations should be greater than {@code MIN_INTERVAL} (by default 10s).
     * Or we need to wait for a while.
     */
    private static void sleepIfNeeded() {
        if (lastModified <= 0) {
            return;
        }
        long now = TimeUtil.currentTimeMillis();
        long durationPast = now - lastModified;
        long estimated = durationPast - MIN_INTERVAL;
        if (estimated < 0) {
            try {
                Thread.sleep(-estimated);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getLastModified() {
        return lastModified;
    }

    private static class ClusterStatePropertyListener implements PropertyListener<Integer> {
        @Override
        public synchronized void configLoad(Integer value) {
            applyStateInternal(value);
        }

        @Override
        public synchronized void configUpdate(Integer value) {
            applyStateInternal(value);
        }
    }

    private static boolean applyStateInternal(Integer state) {
        if (state == null || state < CLUSTER_NOT_STARTED) {
            return false;
        }
        if (state == mode) {
            return true;
        }
        try {
            switch (state) {
                case CLUSTER_CLIENT:
                    return setToClient();
                case CLUSTER_SERVER:
                    return setToServer();
                case CLUSTER_NOT_STARTED:
                    setStop();
                    return true;
                default:
                    RecordLog.warn("[ClusterStateManager] Ignoring unknown cluster state: " + state);
                    return false;
            }
        } catch (Throwable t) {
            RecordLog.warn("[ClusterStateManager] Fatal error when applying state: " + state, t);
            return false;
        }
    }

    private static void setStop() {
        if (mode == CLUSTER_NOT_STARTED) {
            return;
        }
        RecordLog.info("[ClusterStateManager] Changing cluster mode to not-started");
        mode = CLUSTER_NOT_STARTED;

        sleepIfNeeded();
        lastModified = TimeUtil.currentTimeMillis();

        stopClient();
        stopServer();
    }

    /**
     * Apply given state to cluster mode.
     *
     * @param state valid state to apply
     */
    public static void applyState(Integer state) {
        stateProperty.updateValue(state);
    }

    public static void markToServer() {
        mode = CLUSTER_SERVER;
    }

    private static final int MIN_INTERVAL = 5 * 1000;
}
