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

import com.alibaba.csp.sentinel.slots.hotspot.RollingParamEvent;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;

/**
 * @author Eric Zhao
 */
public class LruMapBucket {

    private final CacheMap<Object, AtomicInteger>[] data;

    @SuppressWarnings("unchecked")
    public LruMapBucket() {
        RollingParamEvent[] events = RollingParamEvent.values();
        this.data = new CacheMap[events.length];
        for (RollingParamEvent event : events) {
            data[event.ordinal()] = new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>();
        }
    }

    public void reset() {
        passedParams.clear();
        blockedParams.clear();
    }

    public int get(RollingParamEvent event, Object value) {
        AtomicInteger counter = data[event.ordinal()].get(value);
        return counter == null ? 0 : counter.intValue();
    }

    public void add(RollingParamEvent event, Object value) {
        data[event.ordinal()].putIfAbsent(value, new AtomicInteger());
        AtomicInteger counter = data[event.ordinal()].get(value);
        counter.incrementAndGet();
    }

    public int getPassCountFor(Object value) {
        return get(RollingParamEvent.REQUEST_PASSED, value);
    }

    public void addPassCountFor(Object value) {
        add(RollingParamEvent.REQUEST_PASSED, value);
    }

    public int getBlockedCountFor(Object value) {
        return get(RollingParamEvent.REQUEST_BLOCKED, value);
    }

    public void addBlockedCountFor(Object value) {
        add(RollingParamEvent.REQUEST_BLOCKED, value);
    }

    public Set<Object> ascendingKeySet(RollingParamEvent type) {
        return data[type.ordinal()].ascendingKeySet();
    }
}
