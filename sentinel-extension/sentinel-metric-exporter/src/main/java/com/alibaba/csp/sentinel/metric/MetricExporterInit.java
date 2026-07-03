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

package com.alibaba.csp.sentinel.metric;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.collector.MetricCollector;
import com.alibaba.csp.sentinel.metric.exporter.MetricExporter;
import com.alibaba.csp.sentinel.metric.exporter.jmx.JMXMetricExporter;

import java.util.ArrayList;
import java.util.List;

/**
 * The{@link MetricExporterInit} work on load Metric exporters.
 *
 * @author chenglu
 * @date 2021-07-01 19:58
 * @since 1.8.3
 */
public class MetricExporterInit implements InitFunc {
    
    /**
     * the list of metric exporters.
     */
    private static List<MetricExporter> metricExporters = new ArrayList<>();
    
    /*
      load metric exporters.
     */
    static {
        // now we use this simple way to load MetricExporter.
        metricExporters.add(new JMXMetricExporter());
    }
    
    @Override
    public void init() throws Exception {
        RecordLog.info("[MetricExporterInit] MetricExporter start init.");
        // start the metric exporters.
        for (MetricExporter metricExporter : metricExporters) {
           try {
               metricExporter.start();
           } catch (Exception e) {
               RecordLog.warn("[MetricExporterInit] MetricExporterInit start the metricExport[{}] failed, will ignore it.",
                       metricExporter.getClass().getName(), e);
           }
        }
        
        // add shutdown hook.
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> metricExporters.forEach(metricExporter -> {
                    try {
                        metricExporter.shutdown();
                    } catch (Exception e) {
                        RecordLog.warn("[MetricExporterInit] MetricExporterInit shutdown the metricExport[{}] failed, will ignore it.",
                                metricExporter.getClass().getName(), e);
                    }
                })
        ));
    }
}
