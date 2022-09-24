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

import java.util.Set;

/**
 * A common cache map interface.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 * @author Eric Zhao
 * @since 0.2.0
 */
public interface CacheMap<K, V> {

    boolean containsKey(K key);

    V get(K key);

    V remove(K key);

    V put(K key, V value);

    V putIfAbsent(K key, V value);

    long size();

    void clear();

    Set<K> keySet(boolean ascending);
}
