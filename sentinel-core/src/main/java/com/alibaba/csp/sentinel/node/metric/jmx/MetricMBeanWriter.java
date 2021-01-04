package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MetricMBeanWriter {
    
    private MBeanRegistry mBeanRegistry = MBeanRegistry.getInstance();
    
    public synchronized void write(Map<Long, List<MetricNode>> map) throws Exception {
        if (map == null || map.isEmpty()) {
            List<MetricNode> metricNodes = mBeanRegistry.listAllMBeans();
            if (metricNodes == null || metricNodes.isEmpty()) {
                return;
            }
            Iterator<MetricNode> iterator = metricNodes.iterator();
            while (iterator.hasNext()) {
                MetricNode metricNode = iterator.next();
                metricNode.setTimestamp(System.currentTimeMillis());
                metricNode.setPassQps(0);
                metricNode.setExceptionQps(0);
                metricNode.setSuccessQps(0);
                metricNode.setOccupiedPassQps(0);
                metricNode.setBlockQps(0);
            }
            return;
        }
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = "common";
        }
        Iterator<List<MetricNode>> list = map.values().iterator();
        while (list.hasNext()) {
            Iterator<MetricNode> iterator = list.next().iterator();
            while (iterator.hasNext()) {
                MetricNode metricNode = iterator.next();
                String mBeanName = "Sentinel:type=" + appName + ",name=\"" + metricNode.getResource() +"\"";
                MetricNode mBeanMetricNode = mBeanRegistry.findMBean(mBeanName);
                if (mBeanMetricNode != null) {
                    mBeanMetricNode.setBlockQps(metricNode.getBlockQps());
                    mBeanMetricNode.setRt(metricNode.getRt());
                    mBeanMetricNode.setSuccessQps(metricNode.getSuccessQps());
                    mBeanMetricNode.setExceptionQps(metricNode.getExceptionQps());
                    mBeanMetricNode.setTimestamp(metricNode.getTimestamp());
                    mBeanMetricNode.setClassification(metricNode.getClassification());
                    mBeanMetricNode.setConcurrency(metricNode.getConcurrency());
                    mBeanMetricNode.setOccupiedPassQps(mBeanMetricNode.getOccupiedPassQps());
                    mBeanMetricNode.setPassQps(mBeanMetricNode.getPassQps());
                } else {
                    mBeanRegistry.register(metricNode, mBeanName);
                }
            }
        }
    }
}
