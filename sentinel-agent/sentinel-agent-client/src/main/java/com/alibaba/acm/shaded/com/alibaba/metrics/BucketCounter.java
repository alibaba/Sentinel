package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.Map;

/**
 * 提供分桶计数功能，每个桶统计一定时间间隔内的计数。
 * BucketCounter只保留最近N个时间间隔内的计数，再老的会被丢弃。
 * 同时保存从创建开始到现在的累计计数。
 * @author wangtao 2017-06-06 15:21.
 */
public interface BucketCounter extends Counter {

    /**
     * update the counter to the given bucket
     */
    void update();

    /**
     * update the counter to the given bucket
     */
    void update(long n);

    /**
     * Return the bucket count, keyed by timestamp
     * @return the bucket count, keyed by timestamp
     */
    Map<Long, Long> getBucketCounts();

    /**
     * Return the bucket count, keyed by timestamp, since (including) the startTime.
     * 返回从startTime开始的分桶统计功能
     * @param startTime 查询起始时间, 单位是毫秒
     * @return the bucket count, keyed by timestamp
     */
    Map<Long, Long> getBucketCounts(long startTime);

    /**
     * Get the interval of the bucket
     * @return the interval of the bucket
     */
    int getBucketInterval();
}
