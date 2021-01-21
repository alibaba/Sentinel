package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import java.util.HashMap;
import java.util.List;
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
            List<MetricNode> metrics = getLastMetrics(node);
            aggregate(metricNodeMap, metrics, node);
        }
        aggregate(metricNodeMap, getLastMetrics(Constants.ENTRY_NODE), Constants.ENTRY_NODE);
        try {
            METRIC_BEAN_WRITER.write(metricNodeMap);
        } catch (Exception e) {
            RecordLog.warn("[MetricMBeanTimerListener] write MBean fail", e);
        }
        metricNodeMap.clear();
    }
    
    private List<MetricNode> getLastMetrics(ClusterNode node) {
        final long currentTime = TimeUtil.currentTimeMillis();
        final long maxTime = currentTime - currentTime % 1000;
        final long minTime = maxTime - 1000;
        return node.rawMetricsInMin(new Predicate<Long>() {
            @Override
            public boolean test(Long aLong) {
                return aLong >= minTime && aLong < maxTime;
            }
        });
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
            if (existMetricNode != null && existMetricNode.getTimestamp() > metricNode.getTimestamp()) {
                continue;
            }
            metricNodeMap.put(resource, metricNode);
        }
    }
}
