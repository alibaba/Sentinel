package com.alibaba.csp.sentinel.prom.metrics;

import com.alibaba.csp.sentinel.prom.collector.SentinelCollector;
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
        samples.add(new Sample(name, labelNames, labelValues, value,timestampMs));
        return this;
    }
}