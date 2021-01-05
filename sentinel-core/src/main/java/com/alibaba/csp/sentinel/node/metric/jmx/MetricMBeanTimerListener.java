package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenglu
 */
public class MetricMBeanTimerListener implements Runnable {
    
    private static final MetricMBeanWriter METRIC_MBEAN_WRITER = new MetricMBeanWriter();
    
    @Override
    public void run() {
        Map<String, MetricNode> metricNodeMap = new HashMap<>();
        for (Map.Entry<ResourceWrapper, ClusterNode> e : ClusterBuilderSlot.getClusterNodeMap().entrySet()) {
            ClusterNode node = e.getValue();
            Map<Long, MetricNode> metrics = node.metrics();
            aggregate(metricNodeMap, metrics, node);
        }
        aggregate(metricNodeMap, Constants.ENTRY_NODE.metrics(), Constants.ENTRY_NODE);
        try {
            METRIC_MBEAN_WRITER.write(metricNodeMap);
        } catch (Exception e) {
            RecordLog.warn("[MetricMBeanTimerListener] write MBean fail", e);
        }
        metricNodeMap.clear();
    }
    
    private void aggregate(Map<String, MetricNode> metricNodeMap, Map<Long, MetricNode> metrics, ClusterNode node) {
        for (Map.Entry<Long, MetricNode> entry : metrics.entrySet()) {
            String resource = node.getName();
            MetricNode metricNode = entry.getValue();
            metricNode.setResource(resource);
            metricNode.setClassification(node.getResourceType());
            MetricNode existMetricNode = metricNodeMap.get(resource);
            if (existMetricNode != null && existMetricNode.getTimestamp() > metricNode.getTimestamp()) {
                continue;
            }
            metricNodeMap.put(resource, metricNode);
        }
    }
}
