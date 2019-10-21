package com.alibaba.jvm.sandbox.api.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存加载
 *
 * @param <K> KEY
 * @param <V> VAL
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public abstract class CacheGet<K, V> {

    private final Map<K, V> cache = new HashMap<K, V>();

    /**
     * 从缓存中加载
     *
     * @param key KEY
     * @return VALUE
     */
    public V getFromCache(K key) {
        if (!cache.containsKey(key)) {
            try {
                final V value;
                cache.put(key, value = load(key));
                return value;
            } catch (Throwable cause) {
                throw new CacheLoadUnCaughtException(cause);
            }
        } else {
            return cache.get(key);
        }
    }

    /**
     * 加载缓存
     *
     * @param key KEY
     * @return VALUE
     * @throws Throwable 加载失败
     */
    protected abstract V load(K key) throws Throwable;

    private final static class CacheLoadUnCaughtException extends RuntimeException {
        CacheLoadUnCaughtException(Throwable cause) {
            super(cause);
        }
    }

}
