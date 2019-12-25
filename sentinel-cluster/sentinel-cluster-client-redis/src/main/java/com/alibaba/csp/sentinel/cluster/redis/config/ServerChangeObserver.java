package com.alibaba.csp.sentinel.cluster.redis.config;

public interface ServerChangeObserver {
    /**
     * Callback on remote server address change.
     *
     * @param assignConfig new cluster assignment config
     */
    void onRemoteServerChange(ClusterClientConfig assignConfig);
}
