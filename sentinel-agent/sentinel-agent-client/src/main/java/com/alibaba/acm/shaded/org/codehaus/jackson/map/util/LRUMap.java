package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper for simple bounded LRU maps used for reusing lookup values.
 * 
 * @since 1.7
 */
@SuppressWarnings("serial")
public class LRUMap<K,V> extends LinkedHashMap<K,V>
{
    protected final int _maxEntries;
    
    public LRUMap(int initialEntries, int maxEntries)
    {
        super(initialEntries, 0.8f, true);
        _maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
    {
        return size() > _maxEntries;
    }

}
