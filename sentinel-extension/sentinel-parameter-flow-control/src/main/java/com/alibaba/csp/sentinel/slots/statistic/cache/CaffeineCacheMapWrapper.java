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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * A {@link Cache} wrapper for the universal {@link CacheMap}.
 *
 * @author shaoqiangyan
 *
 */
public class CaffeineCacheMapWrapper<T, R> implements CacheMap<T, R> {


    private final Cache<T, R> map;

    public CaffeineCacheMapWrapper(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cache max capacity should be positive: " + size);
        }
        this.map = Caffeine.newBuilder()
                .maximumSize(size)
                .build();
    }

    public CaffeineCacheMapWrapper(Cache<T, R> map) {
        if (map == null) {
            throw new IllegalArgumentException("Invalid map instance");
        }
        this.map = map;
    }

    @Override
    public boolean containsKey(T key) {
        return asMap().containsKey(key);
    }

    @Override
    public R get(T key) {
        return map.getIfPresent(key);
    }

    @Override
    public R remove(T key) {
        return asMap().remove(key);
    }

    @Override
    public R put(T key, R value) {
        return asMap().put(key, value);
    }

    @Override
    public R putIfAbsent(T key, R value) {
        return asMap().putIfAbsent(key, value);
    }

    @Override
    public long size() {
        return asMap().size();
    }

    @Override
    public void clear() {
        map.invalidateAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> keySet(boolean ascending) {
        Comparator<? super T> comparator;
        if (ascending) {
            comparator = (Comparator<? super T>) Comparator.naturalOrder();
        } else {
            comparator = (Comparator<? super T>) Comparator.reverseOrder();
        }
        return asMap().keySet().stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private ConcurrentMap<T, R> asMap() {
        return map.asMap();
    }
}
