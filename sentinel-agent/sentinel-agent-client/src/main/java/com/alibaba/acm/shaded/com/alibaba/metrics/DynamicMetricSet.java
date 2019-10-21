package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.Map;

/**
 * A dynamic metric set.
 * The metrics inside will change dynamically.
 * @author wangtao 2016-07-07 14:37.
 */
public interface DynamicMetricSet extends Metric {

    /**
     * A map of metric names to metrics.
     * The metrics inside will change dynamically.
     * So DO NOT register them at first time.
     *
     * @return the dynamically changing metrics
     */
    Map<MetricName, Metric> getDynamicMetrics();
}
