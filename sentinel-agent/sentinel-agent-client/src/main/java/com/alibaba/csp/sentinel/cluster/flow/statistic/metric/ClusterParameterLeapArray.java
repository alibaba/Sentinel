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

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @param <C> counter type
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterParameterLeapArray<C> extends LeapArray<CacheMap<Object, C>> {

    private final int maxCapacity;

    public ClusterParameterLeapArray(int sampleCount, int intervalInMs, int maxCapacity) {
        super(sampleCount, intervalInMs);
        AssertUtil.isTrue(maxCapacity > 0, "maxCapacity of LRU map should be positive");
        this.maxCapacity = maxCapacity;
    }

    @Override
    public CacheMap<Object, C> newEmptyBucket(long timeMillis) {
        return new ConcurrentLinkedHashMapWrapper<>(maxCapacity);
    }

    @Override
    protected WindowWrap<CacheMap<Object, C>> resetWindowTo(WindowWrap<CacheMap<Object, C>> w, long startTime) {
        w.resetTo(startTime);
        w.value().clear();
        return w;
    }

}
