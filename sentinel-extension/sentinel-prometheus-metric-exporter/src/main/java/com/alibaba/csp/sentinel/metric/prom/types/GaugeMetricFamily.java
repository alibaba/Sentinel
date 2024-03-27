/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.metric.prom.types;


import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The{@link SentinelCollector} the MetricFamilySamples for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class GaugeMetricFamily extends Collector.MetricFamilySamples {

    private final List<String> labelNames;

    public GaugeMetricFamily(String name, String help, double value) {
        super(name, Collector.Type.GAUGE, help, new ArrayList<Sample>());
        labelNames = Collections.emptyList();
        samples.add(new Sample(
                name,
                labelNames,
                Collections.<String>emptyList(),
                value));
    }

    public GaugeMetricFamily(String name, String help, List<String> labelNames) {
        super(name, Collector.Type.GAUGE, help, new ArrayList<Sample>());
        this.labelNames = labelNames;
    }

    public GaugeMetricFamily addMetric(List<String> labelValues, double value, long timestampMs) {
        if (labelValues.size() != labelNames.size()) {
            throw new IllegalArgumentException("Incorrect number of labels.");
        }
        samples.add(new Sample(name, labelNames, labelValues, value, timestampMs));
        return this;
    }
}