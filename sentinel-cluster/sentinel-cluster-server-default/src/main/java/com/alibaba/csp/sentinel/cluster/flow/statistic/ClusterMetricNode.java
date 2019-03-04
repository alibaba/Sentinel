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
package com.alibaba.csp.sentinel.cluster.flow.statistic;

import java.util.Map;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterMetricNode {

    private long timestamp;

    private String resourceName;
    private long flowId;

    private double passQps;
    private double blockQps;
    private long rt;

    private Map<Object, Double> topParams;

    public long getTimestamp() {
        return timestamp;
    }

    public ClusterMetricNode setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public ClusterMetricNode setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public long getFlowId() {
        return flowId;
    }

    public ClusterMetricNode setFlowId(long flowId) {
        this.flowId = flowId;
        return this;
    }

    public double getPassQps() {
        return passQps;
    }

    public ClusterMetricNode setPassQps(double passQps) {
        this.passQps = passQps;
        return this;
    }

    public double getBlockQps() {
        return blockQps;
    }

    public ClusterMetricNode setBlockQps(double blockQps) {
        this.blockQps = blockQps;
        return this;
    }

    public long getRt() {
        return rt;
    }

    public ClusterMetricNode setRt(long rt) {
        this.rt = rt;
        return this;
    }

    public Map<Object, Double> getTopParams() {
        return topParams;
    }

    public ClusterMetricNode setTopParams(Map<Object, Double> topParams) {
        this.topParams = topParams;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterMetricNode{" +
            "timestamp=" + timestamp +
            ", resourceName='" + resourceName + '\'' +
            ", flowId=" + flowId +
            ", passQps=" + passQps +
            ", blockQps=" + blockQps +
            ", rt=" + rt +
            ", topParams=" + topParams +
            '}';
    }
}
