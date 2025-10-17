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

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * A {@link Caffeine} wrapper for the universal {@link CacheMap}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ConcurrentLinkedHashMapWrapper<T, R> implements CacheMap<T, R> {

    private final Cache<T, R> map;

    public ConcurrentLinkedHashMapWrapper(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cache max capacity should be positive: " + size);
        }
        this.map = Caffeine.newBuilder()
                .maximumSize(size)
                .build();
    }

    public ConcurrentLinkedHashMapWrapper(Cache<T, R> map) {
        if (map == null) {
            throw new IllegalArgumentException("Invalid map instance");
        }
        this.map = map;
    }

    @Override
    public boolean containsKey(T key) {
        return map.asMap().containsKey(key);
    }

    @Override
    public R get(T key) {
        return map.getIfPresent(key);
    }

    @Override
    public R remove(T key) {
        return map.asMap().remove(key);
    }

    @Override
    public R put(T key, R value) {
        return map.asMap().put(key, value);
    }

    @Override
    public R putIfAbsent(T key, R value) {
        return map.asMap().putIfAbsent(key, value);
    }

    @Override
    public long size() {
        return map.asMap().keySet().size();
    }

    @Override
    public void clear() {
        map.invalidateAll();
    }

    @Override
    public Set<T> keySet() {
        return map.asMap().keySet();
    }

    @Override
    public Set<T> keySet(boolean ascending) {
        if (ascending) {
            return map.asMap().keySet().stream().sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            return map.asMap().keySet().stream().sorted((Comparator
                    <? super T>) Comparator.reverseOrder())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        }
    }
}
