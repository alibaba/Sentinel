package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;

/**
 * Helper class used for storing mapping from property name to
 * {@link SettableBeanProperty} instances.
 *<p>
 * Note that this class is used instead of generic {@link java.util.HashMap}
 * is performance: although default implementation is very good for generic
 * use cases, it can still be streamlined a bit for specific use case
 * we have.
 * 
 * @since 1.7
 */
public final class BeanPropertyMap
{
    private final Bucket[] _buckets;
    
    private final int _hashMask;

    private final int _size;
    
    public BeanPropertyMap(Collection<SettableBeanProperty> properties)
    {
        _size = properties.size();
        int bucketCount = findSize(_size);
        _hashMask = bucketCount-1;
        Bucket[] buckets = new Bucket[bucketCount];
        for (SettableBeanProperty property : properties) {
            String key = property.getName();
            int index = key.hashCode() & _hashMask;
            buckets[index] = new Bucket(buckets[index], key, property);
        }
        _buckets = buckets;
    }

    public void assignIndexes()
    {
        // order is arbitrary, but stable:
        int index = 0;
        for (Bucket bucket : _buckets) {
            while (bucket != null) {
                bucket.value.assignIndex(index++);
                bucket = bucket.next;
            }
        }
    }
    
    private final static int findSize(int size)
    {
        // For small enough results (32 or less), we'll require <= 50% fill rate; otherwise 80%
        int needed = (size <= 32) ? (size + size) : (size + (size >> 2));
        int result = 2;
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

    /**
     * Accessor for traversing over all contained properties.
     */
    public Iterator<SettableBeanProperty> allProperties() {
        return new IteratorImpl(_buckets);
    }
    
    public SettableBeanProperty find(String key)
    {
        int index = key.hashCode() & _hashMask;
        Bucket bucket = _buckets[index];
        // Let's unroll first lookup since that is null or match in 90+% cases
        if (bucket == null) {
            return null;
        }
        // Primarily we do just identity comparison as keys should be interned
        if (bucket.key == key) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (bucket.key == key) {
                return bucket.value;
            }
        }
        // Do we need fallback for non-interned Strings?
        return _findWithEquals(key, index);
    }

    /**
     * Specialized method that can be used to replace an existing entry
     * (note: entry MUST exist; otherwise exception is thrown) with
     * specified replacement.
     */
    public void replace(SettableBeanProperty property)
    {
        String name = property.getName();
        int index = name.hashCode() & (_buckets.length-1);

        /* This is bit tricky just because buckets themselves
         * are immutable, so we need to recreate the chain. Fine.
         */
        Bucket tail = null;
        boolean found = false;

        
        for (Bucket bucket = _buckets[index]; bucket != null; bucket = bucket.next) {
            // match to remove?
            if (!found && bucket.key.equals(name)) {
                found = true;
            } else {
                tail = new Bucket(tail, bucket.key, bucket.value);
            }
        }
        // Not finding specified entry is error, so:
        if (!found) {
            throw new NoSuchElementException("No entry '"+property+"' found, can't replace");
        }
        /* So let's attach replacement in front: useful also because
         * it allows replacement even when iterating over entries
         */
        _buckets[index] = new Bucket(tail, name, property);
    }

    /**
     * Specialized method for removing specified existing entry.
     * NOTE: entry MUST exist, otherwise an exception is thrown.
     * 
     * @since 1.9
     */
    public void remove(SettableBeanProperty property)
    {
        // Mostly this is the same as code with 'replace', just bit simpler...
        String name = property.getName();
        int index = name.hashCode() & (_buckets.length-1);
        Bucket tail = null;
        boolean found = false;
        // slightly complex just because chain is immutable, must recreate
        for (Bucket bucket = _buckets[index]; bucket != null; bucket = bucket.next) {
            // match to remove?
            if (!found && bucket.key.equals(name)) {
                found = true;
            } else {
                tail = new Bucket(tail, bucket.key, bucket.value);
            }
        }
        if (!found) { // must be found
            throw new NoSuchElementException("No entry '"+property+"' found, can't remove");
        }
        _buckets[index] = tail;
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    private SettableBeanProperty _findWithEquals(String key, int index)
    {
        Bucket bucket = _buckets[index];
        while (bucket != null) {
            if (key.equals(bucket.key)) {
                return bucket.value;
            }
            bucket = bucket.next;
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
        public final Bucket next;
        public final String key;
        public final SettableBeanProperty value;
        
        public Bucket(Bucket next, String key, SettableBeanProperty value)
        {
            this.next = next;
            this.key = key;
            this.value = value;
        }
    }

    private final static class IteratorImpl implements Iterator<SettableBeanProperty>
    {
        /**
         * Buckets of the map
         */
        private final Bucket[] _buckets;

        /**
         * Bucket that contains next value to return (if any); null if nothing more to iterate
         */
        private Bucket _currentBucket;

        /**
         * Index of the next bucket in bucket array to check.
         */
        private int _nextBucketIndex;
        
        public IteratorImpl(Bucket[] buckets) {
            _buckets = buckets;
            // need to initialize to point to first entry...
            int i = 0;
            for (int len = _buckets.length; i < len; ) {
                Bucket b = _buckets[i++];
                if (b != null) {
                    _currentBucket = b;
                    break;
                }
            }
            _nextBucketIndex = i;
        }

        @Override
        public boolean hasNext() {
            return _currentBucket != null;
        }

        @Override
        public SettableBeanProperty next()
        {
            Bucket curr = _currentBucket;
            if (curr == null) { // sanity check
                throw new NoSuchElementException();
            }
            // need to advance, too
            Bucket b = curr.next;
            while (b == null && _nextBucketIndex < _buckets.length) {
                b = _buckets[_nextBucketIndex++];
            }
            _currentBucket = b;
            return curr.value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
