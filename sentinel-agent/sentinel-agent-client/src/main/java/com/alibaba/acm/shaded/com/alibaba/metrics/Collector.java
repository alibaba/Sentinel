package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * The GOF Visitor pattern.
 * https://dzone.com/articles/design-patterns-visitor
 * @author wangtao 2016-11-24 22:05.
 */
public interface Collector {

    void collect(MetricName name, Counter counter, long timestamp);

    void collect(MetricName name, Gauge gauge, long timestamp);

    void collect(MetricName name, Meter meter, long timestamp);

    void collect(MetricName name, Histogram histogram, long timestamp);

    void collect(MetricName name, Timer timer, long timestamp);

    void collect(MetricName name, Compass compass, long timestamp);

    void collect(MetricName name, FastCompass fastCompass, long timestamp);

    void collect(MetricName name, ClusterHistogram clusterHistogram, long timestamp);
}
