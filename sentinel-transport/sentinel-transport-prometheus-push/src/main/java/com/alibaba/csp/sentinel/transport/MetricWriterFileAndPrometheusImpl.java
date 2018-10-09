package com.alibaba.csp.sentinel.transport;

import java.util.List;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.node.metric.MetricWriterDefaultImpl;

public class MetricWriterFileAndPrometheusImpl implements MetricWriter {

    private MetricWriter metricWriterDefaultImpl;

    private MetricWriter metricWriterPrometheusImpl = new MetricWriterPrometheusImpl();

    public MetricWriterFileAndPrometheusImpl() {
        this.metricWriterDefaultImpl = new MetricWriterDefaultImpl(SentinelConfig.singleMetricFileSize(),
            SentinelConfig.totalMetricFileCount());
    }

    public MetricWriterFileAndPrometheusImpl(long singleFileSize) {
        this.metricWriterDefaultImpl = new MetricWriterDefaultImpl(singleFileSize);
    }

    public MetricWriterFileAndPrometheusImpl(long singleFileSize, int totalFileCount) {
        this.metricWriterDefaultImpl = new MetricWriterDefaultImpl(singleFileSize, totalFileCount);
    }

    @Override
    public void write(long time, List<MetricNode> list) throws Exception {
        metricWriterDefaultImpl.write(time, list);
        metricWriterPrometheusImpl.write(time, list);
    }
}
