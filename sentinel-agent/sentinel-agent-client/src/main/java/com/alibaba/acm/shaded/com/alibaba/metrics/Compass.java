package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A Compass Metric that wraps:
 *   a {@link Timer} to measure the qps and rt distribution
 *   a {@link Counter} to measure the success count
 *   a {@link RatioGauge} to measure the success rate
 *   a {@link Map} of <error_code, {@link Counter}> to record the count for each error code
 *   a {@link Map} of <addon, {@link Counter}> to record the count for each addon
 *
 * @author wangtao 2016-08-15 22:49.
 */
public interface Compass extends Metered, Sampling {

    /**
     * A timing context.
     *
     * @see Timer#time()
     */
    interface Context extends Closeable {

        /**
         * Updates the timer with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         * @return the elapsed time in nanoseconds
         */
        long stop();

        /**
         * Mark the invocation as a successful invocation.
         * 标记一次调用为成功调用
         */
        void success();

        /**
         * Mark one occurrence of the specified errorCode
         * 标记一次调用为错误，并且错误码为指定的错误码
         * @param errorCode the errorCode that will be marked
         */
        void error(String errorCode);

        /**
         * Mark one occurrence of the added-on metric with specified suffix
         * 标记一次扩展指标，用户可自定义后缀名
         * 例如，在统计缓存的指标时，除了统计缓存访问次数，还需要统计缓存命中次数，
         * 这个命中次数可能是当满足一定条件才会发生的
         * 这时候可以根据条件，当满足条件时候调用: markAddon(".hit")
         * 会产生一个后缀为.hit.count的Counter来进行计数
         * @param suffix the suffix for the metric
         */
        void markAddon(String suffix);
    }

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    void update(long duration, TimeUnit unit);


    /**
     * Adds a recorded duration
     * @param duration the length of the duration
     * @param unit the scale unit of {@code duration}
     * @param isSuccess whether it is success
     * @param errorCode the error code with this record, if not, null be passed
     * @param addon the addon with this record, if not, null be passed
     */
    void update(long duration, TimeUnit unit, boolean isSuccess, String errorCode, String addon);

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    <T> T time(Callable<T> event) throws Exception;

    /**
     * Returns a new {@link Compass.Context}.
     * 获取一个上下文实例, 这个上下文中记录了这次调用的起始时间
     *
     * @return a new {@link Compass.Context}
     * @see Compass.Context
     */
    Context time();

    /**
     * Get the distribution of error code
     * 获取错误码的分布，每个错误码及错误次数
     * @return the distribution of error code
     */
    Map<String, BucketCounter> getErrorCodeCounts();

    /**
     * Get the success rate of the invocation
     * 获取调用成功率, 这个成功率没有意义, 后续将不再提供
     * @return the success rate
     */
    @Deprecated
    double getSuccessRate();

    /**
     * Get the success count of the invocation
     * 获取调用成功数
     * @return the success count
     */
    long getSuccessCount();

    /**
     * 获取按时间间隔统计的调用成功数
     * @return the bucket success count
     */
    BucketCounter getBucketSuccessCount();

    /**
     * Get the number of occurrence of added on metric
     * 获取扩展指标，包含每个扩展指标的后缀名以及计数
     * @return the distribution of error code
     */
    Map<String, BucketCounter> getAddonCounts();

}
