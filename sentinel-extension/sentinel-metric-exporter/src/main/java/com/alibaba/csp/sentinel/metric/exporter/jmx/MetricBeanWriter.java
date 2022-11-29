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

package com.alibaba.csp.sentinel.metric.exporter.jmx;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * the metric bean writer, it provides {@link MetricBeanWriter#write} method for register the
 * MetricBean in {@link MBeanRegistry} or update the value of MetricBean
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class MetricBeanWriter {
    
    private final MBeanRegistry mBeanRegistry = MBeanRegistry.getInstance();
    
    private static final String DEFAULT_APP_NAME = "sentinel-application";
    
    /**
     * write the MetricNode value to MetricBean
     * if the MetricBean is not registered into {@link MBeanRegistry},
     * it will be created and registered into {@link MBeanRegistry}.
     * else it will update the value of MetricBean.
     * Notes. if the MetricNode is null, then {@link MetricBean} will be reset.
     * @param map metricNode value group by resource
     * @throws Exception write failed exception
     */
    public synchronized void write(Map<String, MetricNode> map) throws Exception {
        if (map == null || map.isEmpty()) {
            List<MetricBean> metricNodes = mBeanRegistry.listAllMBeans();
            if (metricNodes == null || metricNodes.isEmpty()) {
                return;
            }
            for (MetricBean metricNode : metricNodes) {
                metricNode.reset();
            }
            return;
        }
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = DEFAULT_APP_NAME;
        }
        long version = System.currentTimeMillis();
        // set or update the new value
        for (MetricNode metricNode : map.values()) {
            final String mBeanName = "Sentinel:type=" + appName + ",name=\"" + metricNode.getResource()
                    +"\",classification=\"" + metricNode.getClassification() +"\"";
            MetricBean metricBean = mBeanRegistry.findMBean(mBeanName);
            if (metricBean != null) {
                metricBean.setValueFromNode(metricNode);
                metricBean.setVersion(version);
            } else {
                metricBean = new MetricBean();
                metricBean.setValueFromNode(metricNode);
                metricBean.setVersion(version);
                mBeanRegistry.register(metricBean, mBeanName);
                RecordLog.info("[MetricBeanWriter] Registering with JMX as Metric MBean [{}]", mBeanName);
            }
        }
        // reset the old value
        List<MetricBean> metricBeans = mBeanRegistry.listAllMBeans();
        if (metricBeans == null || metricBeans.isEmpty()) {
            return;
        }
        for (MetricBean metricBean : metricBeans) {
            if (!Objects.equals(metricBean.getVersion(), version)) {
                metricBean.reset();
                mBeanRegistry.unRegister(metricBean);
            }
        }
    }
}
