package com.taobao.diamond.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 一个带TTL的简单Cache，对于过期的entry没有清理
 * 
 * @author fengHan, jiuRen
 * 
 * @param <E>
 */
public class SimpleCache<E> {

    final ConcurrentMap<String, CacheEntry<E>> cache = new ConcurrentHashMap<String, CacheEntry<E>>();

    private static class CacheEntry<E> {
        final long expireTime;
        final E value;

        public CacheEntry(E value, long expire) {
            this.expireTime = expire;
            this.value = value;
        }
    }

    public void put(String key, E e, long ttlMs) {
        if (key == null || e == null) {
            return;
        }
        CacheEntry<E> entry = new CacheEntry<E>(e, System.currentTimeMillis() + ttlMs);
        cache.put(key, entry);
    }

    public E get(String key) {
        CacheEntry<E> entry = cache.get(key);
        if (entry != null && entry.expireTime > System.currentTimeMillis()) {
            return entry.value;
        }
        return null;
    }
}
