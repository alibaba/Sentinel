package com.alibaba.csp.sentinel.dashboard.datasource.ds.zookeeper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
@Component
public class ZookeeperProperties {

    @Value("${datasource.provider.zookeeper.server-addr:localhost:2181}")
    private String serverAddr;

    @Value("${datasource.provider.zookeeper.session-timeout:60000}")
    private int sessionTimeout;

    @Value("${datasource.provider.zookeeper.connection-timeout:15000}")
    private int connectionTimeout;

    @Value("${datasource.provider.zookeeper.retry.max-retries:3}")
    private int maxRetries;

    @Value("${datasource.provider.zookeeper.retry.base-sleep-time:1000}")
    private int baseSleepTime;

    @Value("${datasource.provider.zookeeper.retry.max-sleep-time:2147483647}")
    private int maxSleepTime;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getBaseSleepTime() {
        return baseSleepTime;
    }

    public void setBaseSleepTime(int baseSleepTime) {
        this.baseSleepTime = baseSleepTime;
    }

    public int getMaxSleepTime() {
        return maxSleepTime;
    }

    public void setMaxSleepTime(int maxSleepTime) {
        this.maxSleepTime = maxSleepTime;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Datasource.ZookeeperProperties ");
        builder.append("[ ");
        builder.append(  "serverAddr").append("=").append(serverAddr).append(",");
        builder.append(  "sessionTimeout").append("=").append(sessionTimeout).append(",");
        builder.append(  "connectionTimeout").append("=").append(connectionTimeout).append(",");
        builder.append(  "maxRetries").append("=").append(maxRetries).append(",");
        builder.append(  "baseSleepTime").append("=").append(baseSleepTime).append(",");
        builder.append(  "maxSleepTime").append("=").append(maxSleepTime).append(",");
        builder.append("] ");

        return  builder.toString();
    }

}
