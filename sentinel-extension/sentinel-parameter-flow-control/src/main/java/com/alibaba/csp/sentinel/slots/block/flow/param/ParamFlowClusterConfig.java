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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * Parameter flow rule config in cluster mode.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ParamFlowClusterConfig {

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

    public ParamFlowClusterConfig setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getThresholdType() {
        return thresholdType;
    }

    public ParamFlowClusterConfig setThresholdType(int thresholdType) {
        this.thresholdType = thresholdType;
        return this;
    }

    public boolean isFallbackToLocalWhenFail() {
        return fallbackToLocalWhenFail;
    }

    public ParamFlowClusterConfig setFallbackToLocalWhenFail(boolean fallbackToLocalWhenFail) {
        this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public ParamFlowClusterConfig setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public int getWindowIntervalMs() {
        return windowIntervalMs;
    }

    public ParamFlowClusterConfig setWindowIntervalMs(int windowIntervalMs) {
        this.windowIntervalMs = windowIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ParamFlowClusterConfig config = (ParamFlowClusterConfig)o;

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
        return "ParamFlowClusterConfig{" +
            "flowId=" + flowId +
            ", thresholdType=" + thresholdType +
            ", fallbackToLocalWhenFail=" + fallbackToLocalWhenFail +
            ", sampleCount=" + sampleCount +
            ", windowIntervalMs=" + windowIntervalMs +
            '}';
    }
}
