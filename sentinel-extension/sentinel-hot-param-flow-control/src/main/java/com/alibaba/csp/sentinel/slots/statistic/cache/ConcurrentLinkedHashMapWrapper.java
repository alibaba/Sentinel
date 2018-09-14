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
package com.alibaba.csp.sentinel.slots.statistic.cache;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * @author Eric Zhao
 */
public class ConcurrentLinkedHashMapWrapper<T, R> implements CacheMap<T, R> {

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final int DEFAULT_MAX_CAPACITY = 200;

    private final ConcurrentLinkedHashMap<T, R> map;

    public ConcurrentLinkedHashMapWrapper(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cache max capacity should be positive: " + size);
        }
        this.map = new ConcurrentLinkedHashMap.Builder<T, R>()
            .concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL)
            .maximumWeightedCapacity(size)
            .weigher(Weighers.singleton())
            .build();
    }

    public ConcurrentLinkedHashMapWrapper() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public ConcurrentLinkedHashMapWrapper(ConcurrentLinkedHashMap<T, R> map) {
        if (map == null) {
            throw new IllegalArgumentException("Invalid map instance");
        }
        this.map = map;
    }

    @Override
    public R get(T key) {
        return map.get(key);
    }

    @Override
    public R remove(T key) {
        return map.remove(key);
    }

    @Override
    public R put(T key, R value) {
        return map.put(key, value);
    }

    @Override
    public R putIfAbsent(T key, R value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public long size() {
        return map.weightedSize();
    }

    @Override
    public void clear() {
        map.clear();
    }
}
