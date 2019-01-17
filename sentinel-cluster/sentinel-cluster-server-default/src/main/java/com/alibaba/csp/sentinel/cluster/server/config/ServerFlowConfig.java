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
package com.alibaba.csp.sentinel.cluster.server.config;

import com.alibaba.csp.sentinel.cluster.server.ServerConstants;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ServerFlowConfig {

    public static final double DEFAULT_EXCEED_COUNT = 1.0d;
    public static final double DEFAULT_MAX_OCCUPY_RATIO = 1.0d;

    public static final int DEFAULT_INTERVAL_MS = 1000;
    public static final int DEFAULT_SAMPLE_COUNT= 10;
    public static final double DEFAULT_MAX_ALLOWED_QPS= 30000;

    private final String namespace;

    private double exceedCount = DEFAULT_EXCEED_COUNT;
    private double maxOccupyRatio = DEFAULT_MAX_OCCUPY_RATIO;
    private int intervalMs = DEFAULT_INTERVAL_MS;
    private int sampleCount = DEFAULT_SAMPLE_COUNT;

    private double maxAllowedQps = DEFAULT_MAX_ALLOWED_QPS;

    public ServerFlowConfig() {
        this(ServerConstants.DEFAULT_NAMESPACE);
    }

    public ServerFlowConfig(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public double getExceedCount() {
        return exceedCount;
    }

    public ServerFlowConfig setExceedCount(double exceedCount) {
        this.exceedCount = exceedCount;
        return this;
    }

    public double getMaxOccupyRatio() {
        return maxOccupyRatio;
    }

    public ServerFlowConfig setMaxOccupyRatio(double maxOccupyRatio) {
        this.maxOccupyRatio = maxOccupyRatio;
        return this;
    }

    public int getIntervalMs() {
        return intervalMs;
    }

    public ServerFlowConfig setIntervalMs(int intervalMs) {
        this.intervalMs = intervalMs;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public ServerFlowConfig setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public ServerFlowConfig setMaxAllowedQps(double maxAllowedQps) {
        this.maxAllowedQps = maxAllowedQps;
        return this;
    }

    @Override
    public String toString() {
        return "ServerFlowConfig{" +
            "namespace='" + namespace + '\'' +
            ", exceedCount=" + exceedCount +
            ", maxOccupyRatio=" + maxOccupyRatio +
            ", intervalMs=" + intervalMs +
            ", sampleCount=" + sampleCount +
            ", maxAllowedQps=" + maxAllowedQps +
            '}';
    }
}
