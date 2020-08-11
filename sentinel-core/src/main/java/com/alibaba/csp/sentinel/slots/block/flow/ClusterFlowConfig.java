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
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.concurrent.atomic.AtomicInteger;

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
    private boolean fallbackToLocalWhenFail = true;

    /**
     * 0: normal.
     */
    private int strategy = ClusterRuleConstant.FLOW_CLUSTER_STRATEGY_NORMAL;

    private int sampleCount = ClusterRuleConstant.DEFAULT_CLUSTER_SAMPLE_COUNT;
    /**
     * The time interval length of the statistic sliding window (in milliseconds)
     */
    private int windowIntervalMs = RuleConstant.DEFAULT_WINDOW_INTERVAL_MS;

    private long resourceTimeout;

    private int resourceTimeoutStrategy = RuleConstant.DEFAULT_RESOURCE_TIMEOUT_STRATEGY;

    private int acquireRefuseStrategy = RuleConstant.DEFAULT_BLOCK_STRATEGY;

    private long clientOfflineTime;

    private int expireCount = 0;

    private AtomicInteger releasedCount = new AtomicInteger(0);

    public int getAcquireRefuseStrategy() {
        return acquireRefuseStrategy;
    }

    public void setAcquireRefuseStrategy(int acquireRefuseStrategy) {
        this.acquireRefuseStrategy = acquireRefuseStrategy;
    }

    public void addExpireCount(int acquireCount) {
        expireCount += expireCount;
    }

    public void addReleaseCount(int acquireCount) {
        releasedCount.getAndAdd(acquireCount);
    }

    public int getResourceTimeoutStrategy() {
        return resourceTimeoutStrategy;
    }

    public void setResourceTimeoutStrategy(int resourceTimeoutStrategy) {
        this.resourceTimeoutStrategy = resourceTimeoutStrategy;
    }

    public long getResourceTimeout() {
        return resourceTimeout;
    }

    public void setResourceTimeout(long resourceTimeout) {
        this.resourceTimeout = resourceTimeout;
    }

    public long getClientOfflineTime() {
        return clientOfflineTime;
    }

    public void setClientOfflineTime(long clientOfflineTime) {
        this.clientOfflineTime = clientOfflineTime;
    }

    public int getExpireCount() {
        return expireCount;
    }

    public void setExpireCount(int expireCount) {
        this.expireCount = expireCount;
    }

    public AtomicInteger getReleasedCount() {
        return releasedCount;
    }

    public void setReleasedCount(AtomicInteger releasedCount) {
        this.releasedCount = releasedCount;
    }

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

    public boolean isFallbackToLocalWhenFail() {
        return fallbackToLocalWhenFail;
    }

    public ClusterFlowConfig setFallbackToLocalWhenFail(boolean fallbackToLocalWhenFail) {
        this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public ClusterFlowConfig setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public int getWindowIntervalMs() {
        return windowIntervalMs;
    }

    public ClusterFlowConfig setWindowIntervalMs(int windowIntervalMs) {
        this.windowIntervalMs = windowIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClusterFlowConfig that = (ClusterFlowConfig) o;

        if (thresholdType != that.thresholdType) {
            return false;
        }
        if (fallbackToLocalWhenFail != that.fallbackToLocalWhenFail) {
            return false;
        }
        if (strategy != that.strategy) {
            return false;
        }
        if (sampleCount != that.sampleCount) {
            return false;
        }
        if (windowIntervalMs != that.windowIntervalMs) {
            return false;
        }
        return flowId != null ? flowId.equals(that.flowId) : that.flowId == null;
    }

    @Override
    public int hashCode() {
        int result = flowId != null ? flowId.hashCode() : 0;
        result = 31 * result + thresholdType;
        result = 31 * result + (fallbackToLocalWhenFail ? 1 : 0);
        result = 31 * result + strategy;
        result = 31 * result + sampleCount;
        result = 31 * result + windowIntervalMs;
        return result;
    }

    @Override
    public String toString() {
        return "ClusterFlowConfig{" +
                "flowId=" + flowId +
                ", thresholdType=" + thresholdType +
                ", fallbackToLocalWhenFail=" + fallbackToLocalWhenFail +
                ", strategy=" + strategy +
                ", sampleCount=" + sampleCount +
                ", windowIntervalMs=" + windowIntervalMs +
                ", resourceTimeout=" + resourceTimeout +
                ", resourceTimeoutStrategy=" + resourceTimeoutStrategy +
                ", clientOfflineTime=" + clientOfflineTime +
                ", expireCount=" + expireCount +
                ", releasedCount=" + releasedCount +
                '}';
    }
}
