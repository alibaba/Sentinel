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

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.collector.MetricCollector;
import com.alibaba.csp.sentinel.metric.exporter.MetricExporter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The JMX metric exporter, mainly for write metric datas to JMX bean. It implement {@link MetricExporter}, provide method
 * start, export and shutdown. The mainly design for the jmx is refresh the JMX bean data scheduled.
 * {@link JMXExportTask} work on export data to {@link MetricBean}.
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class JMXMetricExporter implements MetricExporter {
    
    /**
     * schedule executor.
     */
    private final ScheduledExecutorService jmxExporterSchedule;
    
    /**
     * JMX metric writer, write metric datas to {@link MetricBean}.
     */
    private final MetricBeanWriter metricBeanWriter = new MetricBeanWriter();
    
    /**
     * global metrics collector.
     */
    private final MetricCollector metricCollector = new MetricCollector();
    
    public JMXMetricExporter() {
        jmxExporterSchedule = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("sentinel-metrics-jmx-exporter-task", true));
    }
    
    @Override
    public void start() throws Exception {
        jmxExporterSchedule.scheduleAtFixedRate(new JMXExportTask(), 1, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void export() throws Exception {
        metricBeanWriter.write(metricCollector.collectMetric());
    }
    
    @Override
    public void shutdown() throws Exception {
        jmxExporterSchedule.shutdown();
    }
    
    /**
     * JMXExportTask mainly work on execute the JMX metric export.
     */
    class JMXExportTask implements Runnable {
        
        @Override
        public void run() {
            try {
                export();
            } catch (Exception e) {
                RecordLog.warn("[JMX Metric Exporter] export to JMX MetricBean failed.", e);
            }
        }
    }
}
