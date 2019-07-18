package com.alibaba.csp.sentinel.util;


import java.util.LinkedHashMap;

/***
 * 
 * @author yikangfeng
 * @date 2019/07/18
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    static public final int DEFAULT_CACHE_SIZE = 1024;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final int size;

    public LRUCache() {
        this(DEFAULT_CACHE_SIZE);
    }

    public LRUCache(final int size) {
        super(size, 0.75f, true);
        this.size = size;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        // TODO Auto-generated method stub
        return super.size() > this.size;
    }

    public int getSize() {
        return size;
    }

}
