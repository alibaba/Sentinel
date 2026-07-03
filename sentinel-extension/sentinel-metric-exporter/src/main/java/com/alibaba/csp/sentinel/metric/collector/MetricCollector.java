/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.csp.sentinel.metric.collector;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link MetricCollector} work on collecting metrics in {@link MetricNode}.
 *
 * @author chenglu
 * @date 2021-07-01 20:01
 * @since 1.8.3
 */
public class MetricCollector {
    
    /**
     * collect the metrics in {@link MetricNode}.
     *
     * @return the metric grouped by resource name.
     */
    public Map<String, MetricNode> collectMetric() {
        final long currentTime = TimeUtil.currentTimeMillis();
        final long maxTime = currentTime - currentTime % 1000;
        final long minTime = maxTime - 1000;
        Map<String, MetricNode> metricNodeMap = new HashMap<>();
        for (Map.Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            ClusterNode node = e.getValue();
            List<MetricNode> metrics = getLastMetrics(node, minTime, maxTime);
            aggregate(metricNodeMap, metrics, node);
        }
        aggregate(metricNodeMap, getLastMetrics(Constants.ENTRY_NODE, minTime, maxTime), Constants.ENTRY_NODE);
        return metricNodeMap;
    }
    
    
    /**
     * Get the last second {@link MetricNode} of {@link ClusterNode}
     * @param node {@link ClusterNode}
     * @param minTime the min time.
     * @param maxTime the max time.
     * @return the list of {@link MetricNode}
     */
    private List<MetricNode> getLastMetrics(ClusterNode node, long minTime, long maxTime) {
        return node.rawMetricsInMin(time -> time >= minTime && time < maxTime);
    }
    
    
    /**
     * aggregate the metrics, the metrics under the same resource will left the lasted value
     * @param metricNodeMap metrics map
     * @param metrics metrics info group by timestamp
     * @param node the node
     */
    private void aggregate(Map<String, MetricNode> metricNodeMap, List<MetricNode> metrics, ClusterNode node) {
        if (metrics == null || metrics.size() == 0) {
            return;
        }
        for (MetricNode metricNode : metrics) {
            String resource = node.getName();
            metricNode.setResource(resource);
            metricNode.setClassification(node.getResourceType());
            MetricNode existMetricNode = metricNodeMap.get(resource);
            // always keep the MetricNode is the last
            if (existMetricNode != null && existMetricNode.getTimestamp() > metricNode.getTimestamp()) {
                continue;
            }
            metricNodeMap.put(resource, metricNode);
        }
    }
}
