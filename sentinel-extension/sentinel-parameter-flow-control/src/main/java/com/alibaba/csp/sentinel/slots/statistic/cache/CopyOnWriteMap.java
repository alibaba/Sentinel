/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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


import com.alibaba.csp.sentinel.util.function.Function;

import java.util.*;

/**
 * COW Map
 *
 * @author wavesZh
 */
public class CopyOnWriteMap<K, V> implements Map<K, V> {

    private volatile Map<K, V> delegate = Collections.emptyMap();

    public CopyOnWriteMap() {

    }

    public CopyOnWriteMap(Map<K, V> existing) {
        if (existing.getClass() == CopyOnWriteMap.class) {
            this.delegate = ((CopyOnWriteMap)existing).delegate;
        } else {
            this.delegate = new HashMap(existing);
        }
    }

    @Override
    public synchronized V put(K key, V value) {
        Map<K, V> delegate = new HashMap(this.delegate);
        V existing = delegate.put(key, value);
        this.delegate = delegate;
        return existing;
    }

    @Override
    public synchronized V remove(Object key) {
        Map<K, V> delegate = new HashMap(this.delegate);
        V existing = delegate.remove(key);
        this.delegate = delegate;
        return existing;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        Map<K, V> delegate = new HashMap(this.delegate);
        delegate.putAll(m);
        this.delegate = delegate;
    }

    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v, newValue;
        return ((v = get(key)) == null &&
                (newValue = mappingFunction.apply(key)) != null &&
                (v = putIfAbsent(key, newValue)) == null) ? newValue : v;
    }


    public synchronized V putIfAbsent(K key, V value) {
        final Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if(existing != null) {
            return existing;
        }
        put(key, value);
        return null;
    }



    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.delegate.get(key);
    }

    @Override
    public synchronized void clear() {
        this.delegate = Collections.emptyMap();
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(delegate.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(delegate.entrySet());
    }
}
