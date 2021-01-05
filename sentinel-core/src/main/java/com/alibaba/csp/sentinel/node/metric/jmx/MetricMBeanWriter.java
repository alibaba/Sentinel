package com.alibaba.csp.sentinel.node.metric.jmx;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenglu
 */
public class MetricMBeanWriter {
    
    private final MBeanRegistry mBeanRegistry = MBeanRegistry.getInstance();
    
    public synchronized void write(Map<String, MetricNode> map) throws Exception {
        if (map == null || map.isEmpty()) {
            List<MetricBean> metricNodes = mBeanRegistry.listAllMBeans();
            if (metricNodes == null || metricNodes.isEmpty()) {
                return;
            }
            Iterator<MetricBean> iterator = metricNodes.iterator();
            while (iterator.hasNext()) {
                MetricBean metricNode = iterator.next();
                metricNode.reset();
            }
            return;
        }
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = "sentinel-application";
        }
        Set<String> existResource = new HashSet<>();
        // set or update the new value
        for (MetricNode metricNode : map.values()) {
            final String mBeanName = "Sentinel:type=" + appName + ",name=\"" + metricNode.getResource() +"\"";
            MetricBean metricBean = mBeanRegistry.findMBean(mBeanName);
            if (metricBean != null) {
                metricBean.setValueFromNode(metricNode);
            } else {
                metricBean = new MetricBean();
                metricBean.setValueFromNode(metricNode);
                mBeanRegistry.register(metricBean, mBeanName);
            }
            existResource.add(mBeanName);
        }
        // reset the old value
        List<MetricBean> metricBeans = mBeanRegistry.listAllMBeans();
        if (metricBeans == null || metricBeans.isEmpty()) {
            existResource.clear();
            return;
        }
        for (MetricBean metricBean : metricBeans) {
            final String mBeanName = "Sentinel:type=" + appName + ",name=\"" + metricBean.getResource() +"\"";
            if (existResource.contains(mBeanName)) {
                continue;
            }
            metricBean.reset();
        }
        existResource.clear();
    }
}
