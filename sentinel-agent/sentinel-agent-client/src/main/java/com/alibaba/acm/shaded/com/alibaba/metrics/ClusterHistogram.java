package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.Arrays;
import java.util.Map;

/**
 * A metric to calculate cluster histogram,
 * since the the percentile can not be aggregated.
 * @author wangtao 2019-01-15 15:00
 */
public abstract class ClusterHistogram implements Metric {

    private static final int MAX_BUCKET_COUNT =
            Integer.getInteger("com.alibaba.acm.shaded.com.alibaba.metrics.maxClusterHistogramBucketCount", 100);

    protected long[] buckets;

    public ClusterHistogram(final long[] buckets) {
        if (buckets == null) {
            this.buckets = new long[]{1, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000};
        } else if (buckets.length == 0) {
            throw new IllegalStateException("The number of buckets should not be null or 0.");
        } else if (buckets.length > MAX_BUCKET_COUNT) {
            throw new IllegalStateException("The number of buckets should be no larger than " + MAX_BUCKET_COUNT);
        } else {
            this.buckets = buckets;
            this.buckets = new long[buckets.length+1];
            for (int i = 0; i < buckets.length; i++) {
                this.buckets[i] = buckets[i];
            }
            this.buckets[buckets.length] = Long.MAX_VALUE;
            // ensure the input array is in ascending order
            Arrays.sort(this.buckets);
        }
    }

    public ClusterHistogram() {
        this(null);
    }

    public abstract void update(long value);

    public long[] getBuckets() {
        return this.buckets;
    }

    /**
     * get the bucket values since (including) the startTime, keyed by timestamp.
     * @param startTime 查询起始时间, 单位是毫秒
     * @return a map, keyed by timestamp, the value is a Map of the bucket and its value
     * For example: 1234556789 -> { [0, 1) -> 1, [1, 10) -> 5, [10, 100) -> 3  }
     * which means in timestamp 1234556789, there are 1 in [0, 1) bucket, 5 in [1, 10) bucket, 3 in [10, 100) bucket.
     */
    public abstract Map<Long, Map<Long, Long>> getBucketValues(long startTime);
}
