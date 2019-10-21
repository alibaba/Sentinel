package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * A filter used to determine whether or not a metric should be reported, among other things.
 */
public interface MetricFilter {

    /**
     * Matches all metrics, regardless of type or name.
     */
    MetricFilter ALL = new MetricFilter() {
        @Override
        public boolean matches(MetricName name, Metric metric) {
            return true;
        }
    };

    /**
     * Returns {@code true} if the metric matches the filter; {@code false} otherwise.
     *
     * @param name      the metric's name
     * @param metric    the metric
     * @return {@code true} if the metric matches the filter
     */
    boolean matches(MetricName name, Metric metric);
}
