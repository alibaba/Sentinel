package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.io.OutputStream;

/**
 * @author wangtao 2016-06-15 14:04.
 */
public interface Snapshot {

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile    a given quantile, in {@code [0..1]}
     * @return the value in the distribution at {@code quantile}
     */
    double getValue(double quantile);

    /**
     * Returns the entire set of values in the snapshot.
     *
     * @return the entire set of values
     */
    long[] getValues();

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values
     */
    int size();

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value
     */
    double getMedian();

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile
     */
    double get75thPercentile();

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile
     */
    double get95thPercentile();

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile
     */
    double get98thPercentile();

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile
     */
    double get99thPercentile();

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile
     */
    double get999thPercentile();

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    long getMax();

    /**
     * Returns the arithmetic mean of the values in the snapshot.
     *
     * @return the arithmetic mean
     */
    double getMean();

    /**
     * Returns the lowest value in the snapshot.
     *
     * @return the lowest value
     */
    long getMin();

    /**
     * Returns the standard deviation of the values in the snapshot.
     *
     * @return the standard value
     */
    double getStdDev();

    /**
     * Writes the values of the snapshot to the given stream.
     *
     * @param output an output stream
     */
    void dump(OutputStream output);
}
