package com.alibaba.csp.sentinel.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertArrayEquals;

/***
 * 
 * @author yikangfeng
 * @date 2019/07/18
 */
public class LRUCacheTest {

    @Test
    public void testLRUCacheSize() {
        final LRUCache<String, String> lruCache = buildLRUCache();
        lruCache.put("a", "a");
        lruCache.put("b", "b");
        lruCache.put("c", "c");
        lruCache.put("d", "d");

        assertEquals(lruCache.getSize(), lruCache.size());

        assertNull(lruCache.get("a"));

    }

    @Test
    public void testLRUCacheAddItem() {
        final LRUCache<String, String> lruCache = buildLRUCache();
        lruCache.put("a", "a");
        lruCache.put("b", "b");
        lruCache.put("c", "c");
        lruCache.put("d", "d");

        lruCache.get("b");

        final LRUCache<String, String> newCache = buildLRUCache();
        newCache.putAll(lruCache);
        newCache.put("e", "e");

        assertArrayEquals(new String[] {"d", "b", "e"}, newCache.keySet().toArray(new String[0]));

    }

    private LRUCache<String, String> buildLRUCache() {
        return new LRUCache<String, String>(3);
    }

}
