package com.alibaba.csp.sentinel.slots.statistic.cache;


import java.util.*;

/**
 * @author wavesZh
 */
public class CopyOnWriteMap<K, V> implements CacheMap<K, V> {

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
    public boolean containsKey(K key) {
        return this.delegate.containsKey(key);
    }

    @Override
    public V get(K key) {
        return this.delegate.get(key);
    }

    @Override
    public synchronized V remove(K key) {
        Map<K, V> delegate = new HashMap(this.delegate);
        V existing = delegate.remove(key);
        this.delegate = delegate;
        return existing;
    }

    @Override
    public synchronized V put(K key, V value) {
        Map<K, V> delegate = new HashMap(this.delegate);
        V existing = delegate.put(key, value);
        this.delegate = delegate;
        return existing;
    }

    @Override
    public synchronized V putIfAbsent(K key, V value) {
        Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if (existing != null) {
            return existing;
        } else {
            this.put(key, value);
            return null;
        }
    }

    @Override
    public long size() {
        return this.delegate.size();
    }

    @Override
    public synchronized void clear() {
        this.delegate = Collections.emptyMap();
    }

    @Override
    public Set<K> keySet(boolean ascending) {
        return this.delegate.keySet();
    }
}
