package com.alibaba.csp.sentinel.metric.extension;

/**
 * This interface provides extension to Sentinel internal statistics.
 *
 * @author Carpenter Lee
 */
public interface MetricExtension {

    /**
     * Add current pass count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     */
    void addPass(String resource, int n);

    /**
     * Add current block count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     */
    void addBlock(String resource, int n);

    /**
     * Add current completed count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     */
    void addSuccess(String resource, int n);

    /**
     * Add current exception count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     */
    void addException(String resource, int n);

    /**
     * Add response time of the resource name.
     *
     * @param rt       response time in millisecond
     * @param resource resource name
     */
    void addRt(String resource, long rt);

    /**
     * Increase current thread count of the resource name.
     *
     * @param resource resource name
     */
    void increaseThreadNum(String resource);

    /**
     * Decrease current thread count of the resource name.
     *
     * @param resource resource name
     */
    void decreaseThreadNum(String resource);
}
