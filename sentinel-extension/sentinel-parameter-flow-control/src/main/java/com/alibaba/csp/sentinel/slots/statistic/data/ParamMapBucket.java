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
package com.alibaba.csp.sentinel.slots.statistic.data;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.slots.block.flow.param.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Represents metric bucket of frequent parameters in a period of time window.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamMapBucket {

    private final CacheMap<Object, AtomicInteger>[] data;

    public ParamMapBucket() {
        this(DEFAULT_MAX_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public ParamMapBucket(int capacity) {
        AssertUtil.isTrue(capacity > 0, "capacity should be positive");
        RollingParamEvent[] events = RollingParamEvent.values();
        this.data = new CacheMap[events.length];
        for (RollingParamEvent event : events) {
            data[event.ordinal()] = new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(capacity);
        }
    }

    public void reset() {
        for (RollingParamEvent event : RollingParamEvent.values()) {
            data[event.ordinal()].clear();
        }
    }

    public int get(RollingParamEvent event, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        return counter == null ? 0 : counter.intValue();
    }

    public ParamMapBucket add(RollingParamEvent event, int count, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        // Note: not strictly concise.
        if (counter == null) {
            AtomicInteger old = data[event.ordinal()].putIfAbsent(value, new AtomicInteger(count));
            if (old != null) {
                old.addAndGet(count);
            }
        } else {
            counter.addAndGet(count);
        }
        return this;
    }

    public Set<Object> ascendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(true);
    }

    public Set<Object> descendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].keySet(false);
    }

    public static final int DEFAULT_MAX_CAPACITY = 200;
}