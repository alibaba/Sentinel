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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

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
        this.metric = new ClusterParameterLeapArray<>(sampleCount, intervalInMs, maxCapacity);
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

    public Map<Object, Double> getTopValues(int number) {
        AssertUtil.isTrue(number > 0, "number must be positive");
        metric.currentWindow();
        List<CacheMap<Object, LongAdder>> buckets = metric.values();

        Map<Object, Long> result = new HashMap<>(buckets.size());

        for (CacheMap<Object, LongAdder> b : buckets) {
            Set<Object> subSet = b.keySet(true);
            for (Object o : subSet) {
                Long count = result.get(o);
                if (count == null) {
                    count = getCount(b.get(o));
                } else {
                    count += getCount(b.get(o));
                }
                result.put(o, count);
            }
        }

        // After merge, get the top set one.
        Set<Entry<Object, Long>> set = result.entrySet();
        List<Entry<Object, Long>> list = new ArrayList<>(set);
        Collections.sort(list, new Comparator<Entry<Object, Long>>() {
            @Override
            public int compare(Entry<Object, Long> a,
                               Entry<Object, Long> b) {
                return (int) (b.getValue() == null ? 0 : b.getValue()) - (int) (a.getValue() == null ? 0 : a.getValue());
            }
        });

        Map<Object, Double> doubleResult = new HashMap<Object, Double>();

        int size = list.size() > number ? number : list.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<Object, Long> x = list.get(i);
            if (x.getValue() == 0) {
                break;
            }
            doubleResult.put(x.getKey(), ((double) x.getValue()) / metric.getIntervalInSecond());
        }

        return doubleResult;
    }
}
