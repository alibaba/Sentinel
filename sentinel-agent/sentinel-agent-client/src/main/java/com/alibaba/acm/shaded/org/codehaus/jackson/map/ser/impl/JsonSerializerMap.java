package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl.SerializerCache.TypeKey;

/**
 * Specialized read-only map used for storing and accessing serializers by type.
 * 
 * @since 1.7
 */
public class JsonSerializerMap
{
    private final Bucket[] _buckets;

    private final int _size;
    
    public JsonSerializerMap(Map<TypeKey,JsonSerializer<Object>> serializers)
    {
        int size = findSize(serializers.size());
        _size = size;
        int hashMask = (size-1);
        Bucket[] buckets = new Bucket[size];
        for (Map.Entry<TypeKey,JsonSerializer<Object>> entry : serializers.entrySet()) {
            TypeKey key = entry.getKey();
            int index = key.hashCode() & hashMask;
            buckets[index] = new Bucket(buckets[index], key, entry.getValue());
        }
        _buckets = buckets;
    }
    
    private final static int findSize(int size)
    {
        // For small enough results (64 or less), we'll require <= 50% fill rate; otherwise 80%
        int needed = (size <= 64) ? (size + size) : (size + (size >> 2));
        int result = 8;
        while (result < needed) {
            result += result;
        }
        return result;
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    public int size() { return _size; }
    
    public JsonSerializer<Object> find(TypeKey key)
    {
        int index = key.hashCode() & (_buckets.length-1);
        Bucket bucket = _buckets[index];
        /* Ok let's actually try unrolling loop slightly as this shows up in profiler;
         * and also because in vast majority of cases first entry is either null
         * or matches.
         */
        if (bucket == null) {
            return null;
        }
        if (key.equals(bucket.key)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (key.equals(bucket.key)) {
                return bucket.value;
            }
        }
        return null;
    }

    /*
    /**********************************************************
    /* Helper beans
    /**********************************************************
     */
    
    private final static class Bucket
    {
        public final TypeKey key;
        public final JsonSerializer<Object> value;
        public final Bucket next;
        
        public Bucket(Bucket next, TypeKey key, JsonSerializer<Object> value)
        {
            this.next = next;
            this.key = key;
            this.value = value;
        }
    }
}
