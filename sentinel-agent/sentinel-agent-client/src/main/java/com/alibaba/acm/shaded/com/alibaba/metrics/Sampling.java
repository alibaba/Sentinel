package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * An object which samples values.
 */
public interface Sampling {
    /**
     * Returns a snapshot of the values.
     *
     * @return a snapshot of the values
     */
    Snapshot getSnapshot();
}
