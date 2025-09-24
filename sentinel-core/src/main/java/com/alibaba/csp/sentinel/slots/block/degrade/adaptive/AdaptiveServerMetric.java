package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

/**
 * Server metrics.
 *
 * @author ylnxwlp
 */
public class AdaptiveServerMetric {

    private final String resourceName;
    private volatile double serverCpuUsage = -1.0;
    private volatile double serverTomcatUsageRate = -1.0;
    private volatile int serverTomcatQueueSize = -1;

    public AdaptiveServerMetric(String resourceName) {
        this.resourceName = resourceName;
    }

    public double getServerCpuUsage() {
        return serverCpuUsage;
    }

    public void setServerCpuUsage(double serverCpuUsage) {
        this.serverCpuUsage = serverCpuUsage;
    }

    public double getServerTomcatUsageRate() {
        return serverTomcatUsageRate;
    }

    public void setServerTomcatUsageRate(double serverTomcatUsageRate) {
        this.serverTomcatUsageRate = serverTomcatUsageRate;
    }

    public int getServerTomcatQueueSize() {
        return serverTomcatQueueSize;
    }

    public void setServerTomcatQueueSize(int serverTomcatQueueSize) {
        this.serverTomcatQueueSize = serverTomcatQueueSize;
    }
}