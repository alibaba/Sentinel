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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;

/**
 * Flow rule config in cluster mode.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterFlowConfig {

    /**
     * Global unique ID.
     */
    private Long flowId;

    /**
     * Threshold type (average by local value or global value).
     */
    private int thresholdType = ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL;
    private boolean fallbackToLocalWhenFail;

    /**
     * 0: normal; 1: using reference (borrow from reference).
     */
    private int strategy = ClusterRuleConstant.FLOW_CLUSTER_STRATEGY_NORMAL;

    private Long refFlowId;
    private int refSampleCount = 10;
    private double refRatio = 1d;

    public Long getFlowId() {
        return flowId;
    }

    public ClusterFlowConfig setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getThresholdType() {
        return thresholdType;
    }

    public ClusterFlowConfig setThresholdType(int thresholdType) {
        this.thresholdType = thresholdType;
        return this;
    }

    public int getStrategy() {
        return strategy;
    }

    public ClusterFlowConfig setStrategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public Long getRefFlowId() {
        return refFlowId;
    }

    public ClusterFlowConfig setRefFlowId(Long refFlowId) {
        this.refFlowId = refFlowId;
        return this;
    }

    public int getRefSampleCount() {
        return refSampleCount;
    }

    public ClusterFlowConfig setRefSampleCount(int refSampleCount) {
        this.refSampleCount = refSampleCount;
        return this;
    }

    public double getRefRatio() {
        return refRatio;
    }

    public ClusterFlowConfig setRefRatio(double refRatio) {
        this.refRatio = refRatio;
        return this;
    }

    public boolean isFallbackToLocalWhenFail() {
        return fallbackToLocalWhenFail;
    }

    public ClusterFlowConfig setFallbackToLocalWhenFail(boolean fallbackToLocalWhenFail) {
        this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ClusterFlowConfig that = (ClusterFlowConfig)o;

        if (thresholdType != that.thresholdType) { return false; }
        if (fallbackToLocalWhenFail != that.fallbackToLocalWhenFail) { return false; }
        if (strategy != that.strategy) { return false; }
        if (refSampleCount != that.refSampleCount) { return false; }
        if (Double.compare(that.refRatio, refRatio) != 0) { return false; }
        if (flowId != null ? !flowId.equals(that.flowId) : that.flowId != null) { return false; }
        return refFlowId != null ? refFlowId.equals(that.refFlowId) : that.refFlowId == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = flowId != null ? flowId.hashCode() : 0;
        result = 31 * result + thresholdType;
        result = 31 * result + (fallbackToLocalWhenFail ? 1 : 0);
        result = 31 * result + strategy;
        result = 31 * result + (refFlowId != null ? refFlowId.hashCode() : 0);
        result = 31 * result + refSampleCount;
        temp = Double.doubleToLongBits(refRatio);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ClusterFlowConfig{" +
            "flowId=" + flowId +
            ", thresholdType=" + thresholdType +
            ", fallbackToLocalWhenFail=" + fallbackToLocalWhenFail +
            ", strategy=" + strategy +
            ", refFlowId=" + refFlowId +
            ", refSampleCount=" + refSampleCount +
            ", refRatio=" + refRatio +
            '}';
    }
}
