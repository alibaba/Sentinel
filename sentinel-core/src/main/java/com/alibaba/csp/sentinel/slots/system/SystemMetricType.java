package com.alibaba.csp.sentinel.slots.system;

/**
 * Metric type of system rule.
 *
 * @author guozhong.huang
 */
public enum SystemMetricType {
    /**
     * Load represents system load1 in Linux/Unix.
     */
    LOAD(0),
    /**
     * AvgRT represents the average response time of all inbound requests.
     */
    AVG_RT(1),
    /**
     * Concurrency represents the concurrency of all inbound requests.
     */
    CONCURRENCY(2),

    /**
     * InboundQPS represents the QPS of all inbound requests.
     */
    INBOUND_QPS(3),

    /**
     * CpuUsage represents the CPU usage percentage of the system.
     */
    CPU_USAGE(4);

    private int type;

    SystemMetricType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
    }
