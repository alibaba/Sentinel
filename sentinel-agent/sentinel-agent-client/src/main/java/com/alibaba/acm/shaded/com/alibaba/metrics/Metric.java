package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * A tag interface to indicate that a class is a metric.
 */
public interface Metric {

    /**
     * Return the last update time in milliseconds
     * @return the last updated time in milliseconds
     */
    long lastUpdateTime();
}
