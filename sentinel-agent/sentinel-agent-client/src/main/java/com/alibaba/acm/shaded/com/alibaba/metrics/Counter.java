package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * <pre>
 * An incrementing and decrementing counter metric.
 *
 * 计数器型指标，适用于记录调用总量等类型的数据
 * </pre>
 */
public interface Counter extends Metric, Counting {

    /**
     * Increment the counter by one.
     * 计数器加1
     */
    void inc();

    /**
     * Increment the counter by {@code n}.
     * 计数器加n
     *
     * @param n the amount by which the counter will be increased
     */
    void inc(long n);

    /**
     * Decrement the counter by one.
     * 计数器减1
     */
    void dec();

    /**
     * Decrement the counter by {@code n}.
     * 计数器减n
     *
     * @param n the amount by which the counter will be decreased
     */
    void dec(long n);

}