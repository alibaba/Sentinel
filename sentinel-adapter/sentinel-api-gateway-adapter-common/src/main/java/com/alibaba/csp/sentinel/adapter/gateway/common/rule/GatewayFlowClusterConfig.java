package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;

/**
 * @author limingqiang01
 */
public class GatewayFlowClusterConfig {
    /**
     * Global unique ID.
     */
    private Long flowId;

    /**
     * Threshold type (average by local value or global value).
     */
    private int thresholdType = ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL;
    private boolean fallbackToLocalWhenFail = false;

    private int sampleCount = ClusterRuleConstant.DEFAULT_CLUSTER_SAMPLE_COUNT;
    /**
     * The time interval length of the statistic sliding window (in milliseconds)
     */
    private int windowIntervalMs = RuleConstant.DEFAULT_WINDOW_INTERVAL_MS;

    public Long getFlowId() {
        return flowId;
    }

    public GatewayFlowClusterConfig setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getThresholdType() {
        return thresholdType;
    }

    public GatewayFlowClusterConfig setThresholdType(int thresholdType) {
        this.thresholdType = thresholdType;
        return this;
    }

    public boolean isFallbackToLocalWhenFail() {
        return fallbackToLocalWhenFail;
    }

    public GatewayFlowClusterConfig setFallbackToLocalWhenFail(boolean fallbackToLocalWhenFail) {
        this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public GatewayFlowClusterConfig setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public int getWindowIntervalMs() {
        return windowIntervalMs;
    }

    public GatewayFlowClusterConfig setWindowIntervalMs(int windowIntervalMs) {
        this.windowIntervalMs = windowIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        GatewayFlowClusterConfig config = (GatewayFlowClusterConfig)o;

        if (thresholdType != config.thresholdType) { return false; }
        if (fallbackToLocalWhenFail != config.fallbackToLocalWhenFail) { return false; }
        if (sampleCount != config.sampleCount) { return false; }
        if (windowIntervalMs != config.windowIntervalMs) { return false; }
        return flowId != null ? flowId.equals(config.flowId) : config.flowId == null;
    }

    @Override
    public int hashCode() {
        int result = flowId != null ? flowId.hashCode() : 0;
        result = 31 * result + thresholdType;
        result = 31 * result + (fallbackToLocalWhenFail ? 1 : 0);
        result = 31 * result + sampleCount;
        result = 31 * result + windowIntervalMs;
        return result;
    }

    @Override
    public String toString() {
        return "GatewayFlowClusterConfig{" +
                "flowId=" + flowId +
                ", thresholdType=" + thresholdType +
                ", fallbackToLocalWhenFail=" + fallbackToLocalWhenFail +
                ", sampleCount=" + sampleCount +
                ", windowIntervalMs=" + windowIntervalMs +
                '}';
    }
}
