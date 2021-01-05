package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import java.util.HashMap;
import java.util.Map;

/**
 * the MetricBean Timer to expose the metrics of {@link com.alibaba.csp.sentinel.node.Node}
 * @author chenglu
 */
public class MetricBeanTimerListener implements Runnable {
    
    private static final MetricBeanWriter METRIC_BEAN_WRITER = new MetricBeanWriter();
    
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
            METRIC_BEAN_WRITER.write(metricNodeMap);
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
