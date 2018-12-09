/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.flow.statistic.metric;

import java.util.List;

import com.alibaba.csp.sentinel.slots.statistic.base.LongAdder;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterParamMetric {

    public static final int DEFAULT_CLUSTER_MAX_CAPACITY = 4000;

    private final ClusterParameterLeapArray<LongAdder> metric;

    public ClusterParamMetric(int sampleCount, int intervalInMs) {
        this(sampleCount, intervalInMs, DEFAULT_CLUSTER_MAX_CAPACITY);
    }

    public ClusterParamMetric(int sampleCount, int intervalInMs, int maxCapacity) {
        AssertUtil.isTrue(sampleCount > 0, "sampleCount should be positive");
        AssertUtil.isTrue(intervalInMs > 0, "interval should be positive");
        AssertUtil.isTrue(intervalInMs % sampleCount == 0, "time span needs to be evenly divided");
        int windowLengthInMs = intervalInMs / sampleCount;
        this.metric = new ClusterParameterLeapArray<>(windowLengthInMs, intervalInMs, maxCapacity);
    }

    public long getSum(Object value) {
        if (value == null) {
            return 0;
        }

        metric.currentWindow();
        long sum = 0;

        List<CacheMap<Object, LongAdder>> buckets = metric.values();
        for (CacheMap<Object, LongAdder> bucket : buckets) {
            long count = getCount(bucket.get(value));
            sum += count;
        }
        return sum;
    }

    private long getCount(/*@Nullable*/ LongAdder adder) {
        return adder == null ? 0 : adder.sum();
    }

    public void addValue(Object value, int count) {
        if (value == null) {
            return;
        }
        CacheMap<Object, LongAdder> data = metric.currentWindow().value();
        LongAdder newCounter = new LongAdder();
        LongAdder currentCounter = data.putIfAbsent(value, newCounter);
        if (currentCounter != null) {
            currentCounter.add(count);
        } else {
            newCounter.add(count);
        }
    }

    public double getAvg(Object value) {
        return getSum(value) / metric.getIntervalInSecond();
    }
}
