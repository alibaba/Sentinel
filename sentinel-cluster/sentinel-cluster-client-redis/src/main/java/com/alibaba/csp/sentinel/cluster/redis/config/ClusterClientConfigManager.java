package com.alibaba.csp.sentinel.cluster.redis.config;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.AssertUtil;

public final class ClusterClientConfigManager {

    /**
     * Client Config properties.
     */
    private static volatile ClusterClientConfig clientConfig;

    private static final PropertyListener<ClusterClientConfig> CONFIG_PROPERTY_LISTENER
            = new ClientConfigPropertyListener();

    private static SentinelProperty<ClusterClientConfig> clientConfigProperty = new DynamicSentinelProperty<>();

    private static final List<ServerChangeObserver> SERVER_CHANGE_OBSERVERS = new ArrayList<>();

    static {
        setToClient();
        bindPropertyListener();
        initServerChangeObserver();
    }

    private static void setToClient() {
        ClusterStateManager.setToClient();
    }

    private static void bindPropertyListener() {
        removePropertyListener();
        clientConfigProperty.addListener(CONFIG_PROPERTY_LISTENER);
    }

    private static void initServerChangeObserver() {
        SERVER_CHANGE_OBSERVERS.add(new RedisClientFactoryManager());
    }

    private static void removePropertyListener() {
        clientConfigProperty.removeListener(CONFIG_PROPERTY_LISTENER);
    }

    public static void registerClientConfigProperty(SentinelProperty<ClusterClientConfig> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (CONFIG_PROPERTY_LISTENER) {
            RecordLog.info("[ClusterClientConfigManager] Registering new global client assignConfig property to "
                    + "cluster client assignConfig manager");
            clientConfigProperty.removeListener(CONFIG_PROPERTY_LISTENER);
            property.addListener(CONFIG_PROPERTY_LISTENER);
            clientConfigProperty = property;
        }
    }

    public static void addServerChangeObserver(ServerChangeObserver observer) {
        AssertUtil.notNull(observer, "observer cannot be null");
        synchronized (SERVER_CHANGE_OBSERVERS) {
            SERVER_CHANGE_OBSERVERS.add(observer);
        }
    }

    public static void applyNewConfig(ClusterClientConfig config) {
        clientConfigProperty.updateValue(config);
    }

    private static class ClientConfigPropertyListener implements PropertyListener<ClusterClientConfig> {
        @Override
        public void configLoad(ClusterClientConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterClientConfigManager] Empty initial client assignConfig");
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
                        "[ClusterClientConfigManager] Invalid cluster client assignConfig, ignoring: " + config);
                return;
            }

            RecordLog.info("[ClusterClientConfigManager] Updating to new client assignConfig: " + config);

            updateClientConfigChange(config);
        }
    }

    private static void updateClientConfigChange(ClusterClientConfig config) {
        clientConfig = config;
        for (ServerChangeObserver observer : SERVER_CHANGE_OBSERVERS) {
            observer.onRemoteServerChange(config);
        }
    }

    public static boolean isValidClientConfig(ClusterClientConfig config) {
        if(config == null) {
            return false;
        }

        if(!ClusterClientConfig.validClusterType.contains(config.getClusterType())
            || (config.getClusterType() == ClusterClientConfig.getRedisSentinel() && config.getMasterName() == null)
            || config.getHostAndPorts() == null || config.getHostAndPorts().isEmpty()) {
            return false;
        }

        return true;
    }

    public static ClusterClientConfig getClientConfig() {
        return clientConfig;
    }

    private ClusterClientConfigManager() {}
}