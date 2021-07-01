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
import com.alibaba.csp.sentinel.metric.collector.MetricCollector;
import com.alibaba.csp.sentinel.metric.exporter.MetricExporter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglu
 * @date 2021-07-01 20:02
 */
public class JMXMetricExporter implements MetricExporter {
    
    private static final ScheduledExecutorService JMX_EXPORTER_SCHEDULE = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("sentinel-metrics-record-task", true));
    
    private final MetricBeanWriter metricBeanWriter = new MetricBeanWriter();
    
    private MetricCollector metricCollector;
    
    public JMXMetricExporter(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }
    
    @Override
    public void start() throws Exception {
        JMX_EXPORTER_SCHEDULE.scheduleAtFixedRate(new JMXExportTask(), 1, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void export() throws Exception {
        metricBeanWriter.write(metricCollector.collectMetric());
    }
    
    @Override
    public void shutdown() throws Exception {
        JMX_EXPORTER_SCHEDULE.shutdown();
    }
    
    class JMXExportTask implements Runnable {
        
        @Override
        public void run() {
            try {
                export();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
