package com.alibaba.csp.sentinel.node.metric;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.PidUtil;

public enum MetricManager {

    INSTANCE;

    private static final MetricSearcherDefaultImpl METRIC_SEARCH_DEFAULT_IMPL = new MetricSearcherDefaultImpl(
            MetricWriterDefaultImpl.METRIC_BASE_DIR,
            MetricWriterDefaultImpl.formMetricFileName(SentinelConfig.getAppName(), PidUtil.getPid()));
    private static final MetricWriterDefaultImpl METRIC_WRITER_DEFAULT_IMPL = new MetricWriterDefaultImpl(
            SentinelConfig.singleMetricFileSize(),
            SentinelConfig.totalMetricFileCount());

    public MetricSearcherDefaultImpl getMetricSearcher() {
        final MetricSearcherDefaultImpl metricSearcher;
        ServiceLoader<MetricSearcherDefaultImpl> loader = ServiceLoader.load(MetricSearcherDefaultImpl.class);
        Iterator<MetricSearcherDefaultImpl> iterator = loader.iterator();
        if (iterator.hasNext()) {
            metricSearcher = iterator.next();
            RecordLog.info("MetricSearcher has been config, use custom impl: " + metricSearcher.getClass().getCanonicalName());
        } else {
            metricSearcher = METRIC_SEARCH_DEFAULT_IMPL;
            RecordLog.info("MetricSearcher not config, use default impl");
        }
        return metricSearcher;
    }

    public MetricWriter getMetricWriter() {
        final MetricWriter metricWriter;
        ServiceLoader<MetricWriter> loader = ServiceLoader.load(MetricWriter.class);
        Iterator<MetricWriter> iterator = loader.iterator();
        if (iterator.hasNext()) {
            metricWriter = iterator.next();
            RecordLog.info("MetricWriter has been config, use custom impl: " + metricWriter.getClass().getCanonicalName());
        } else {
            metricWriter = METRIC_WRITER_DEFAULT_IMPL;
            RecordLog.info("MetricWriter not config, use default impl");
        }
        return metricWriter;
    }

}
