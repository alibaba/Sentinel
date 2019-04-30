package com.alibaba.csp.sentinel.metric.extension;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * This interface provides extension to Sentinel internal statistics.
 * <p>
 * Please note that all method in this class will invoke in the same thread of biz logic.
 * It's necessary to not do time-consuming operation in any of the interface's method,
 * otherwise biz logic will be blocked.
 * </p>
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public interface MetricExtension {

    /**
     * Add current pass count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     * @param args     additional arguments of the resource, eg. if the resource is a method name,
     *                 the args will be the parameters of the method.
     */
    void addPass(String resource, int n, Object... args);

    /**
     * Add current block count of the resource name.
     *
     * @param n              count to add
     * @param resource       resource name
     * @param origin         the original invoker.
     * @param blockException block exception related.
     * @param args           additional arguments of the resource, eg. if the resource is a method name,
     *                       the args will be the parameters of the method.
     */
    void addBlock(String resource, int n, String origin, BlockException blockException, Object... args);

    /**
     * Add current completed count of the resource name.
     *
     * @param n        count to add
     * @param resource resource name
     * @param args     additional arguments of the resource, eg. if the resource is a method name,
     *                 the args will be the parameters of the method.
     */
    void addSuccess(String resource, int n, Object... args);

    /**
     * Add current exception count of the resource name.
     *
     * @param n         count to add
     * @param resource  resource name
     * @param throwable exception related.
     */
    void addException(String resource, int n, Throwable throwable);

    /**
     * Add response time of the resource name.
     *
     * @param rt       response time in millisecond
     * @param resource resource name
     * @param args     additional arguments of the resource, eg. if the resource is a method name,
     *                 the args will be the parameters of the method.
     */
    void addRt(String resource, long rt, Object... args);

    /**
     * Increase current thread count of the resource name.
     *
     * @param resource resource name
     * @param args     additional arguments of the resource, eg. if the resource is a method name,
     *                 the args will be the parameters of the method.
     */
    void increaseThreadNum(String resource, Object... args);

    /**
     * Decrease current thread count of the resource name.
     *
     * @param resource resource name
     * @param args     additional arguments of the resource, eg. if the resource is a method name,
     *                 the args will be the parameters of the method.
     */
    void decreaseThreadNum(String resource, Object... args);
}
