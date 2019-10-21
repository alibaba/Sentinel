package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ResolvableSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * Simple cache object that allows for doing 2-level lookups: first level is
 * by "local" read-only lookup Map (used without locking)
 * and second backup level is by a shared modifiable HashMap.
 * The idea is that after a while, most serializers are found from the
 * local Map (to optimize performance, reduce lock contention),
 * but that during buildup we can use a shared map to reduce both
 * number of distinct read-only maps constructed, and number of
 * serializers constructed.
 *<p>
 * Since version 1.5 cache will actually contain three kinds of entries,
 * based on combination of class pair key. First class in key is for the
 * type to serialize, and second one is type used for determining how
 * to resolve value type. One (but not both) of entries can be null.
 */
public final class SerializerCache
{
    /**
     * Shared, modifiable map; all access needs to be through synchronized blocks.
     *<p>
     * NOTE: keys are of various types (see below for key types), in addition to
     * basic {@link JavaType} used for "untyped" serializers.
     */
    private HashMap<TypeKey, JsonSerializer<Object>> _sharedMap = new HashMap<TypeKey, JsonSerializer<Object>>(64);

    /**
     * Most recent read-only instance, created from _sharedMap, if any.
     */
    private ReadOnlyClassToSerializerMap _readOnlyMap = null;

    public SerializerCache() {
    }

    /**
     * Method that can be called to get a read-only instance populated from the
     * most recent version of the shared lookup Map.
     */
    public ReadOnlyClassToSerializerMap getReadOnlyLookupMap()
    {
        ReadOnlyClassToSerializerMap m;
        synchronized (this) {
            m = _readOnlyMap;
            if (m == null) {
                _readOnlyMap = m = ReadOnlyClassToSerializerMap.from(_sharedMap);
            }
        }
        return m.instance();
    }

    /*
    /**********************************************************
    /* Lookup methods for accessing shared (slow) cache
    /**********************************************************
     */

    /**
     * @since 1.4
     */
    public synchronized int size() {
        return _sharedMap.size();
    }
    
    /**
     * Method that checks if the shared (and hence, synchronized) lookup Map might have
     * untyped serializer for given type.
     */
    public JsonSerializer<Object> untypedValueSerializer(Class<?> type)
    {
        synchronized (this) {
            return _sharedMap.get(new TypeKey(type, false));
        }
    }

    /**
     * @since 1.5
     */
    public JsonSerializer<Object> untypedValueSerializer(JavaType type)
    {
        synchronized (this) {
            return _sharedMap.get(new TypeKey(type, false));
        }
    }

    public JsonSerializer<Object> typedValueSerializer(JavaType type)
    {
        synchronized (this) {
            return _sharedMap.get(new TypeKey(type, true));
        }
    }

    public JsonSerializer<Object> typedValueSerializer(Class<?> cls)
    {
        synchronized (this) {
            return _sharedMap.get(new TypeKey(cls, true));
        }
    }

    /*
    /**********************************************************
    /* Methods for adding shared serializer instances
    /**********************************************************
     */
    
    /**
     * Method called if none of lookups succeeded, and caller had to construct
     * a serializer. If so, we will update the shared lookup map so that it
     * can be resolved via it next time.
     */
    public void addTypedSerializer(JavaType type, JsonSerializer<Object> ser)
    {
        synchronized (this) {
            if (_sharedMap.put(new TypeKey(type, true), ser) == null) {
                // let's invalidate the read-only copy, too, to get it updated
                _readOnlyMap = null;
            }
        }
    }

    public void addTypedSerializer(Class<?> cls, JsonSerializer<Object> ser)
    {
        synchronized (this) {
            if (_sharedMap.put(new TypeKey(cls, true), ser) == null) {
                // let's invalidate the read-only copy, too, to get it updated
                _readOnlyMap = null;
            }
        }
    }
    
    /**
     * @since 1.8
     */
    public void addAndResolveNonTypedSerializer(Class<?> type, JsonSerializer<Object> ser,
            SerializerProvider provider)
        throws JsonMappingException
    {
        synchronized (this) {
            if (_sharedMap.put(new TypeKey(type, false), ser) == null) {
                // let's invalidate the read-only copy, too, to get it updated
                _readOnlyMap = null;
            }
            /* Finally: some serializers want to do post-processing, after
             * getting registered (to handle cyclic deps).
             */
            /* 14-May-2011, tatu: As per [JACKSON-570], resolving needs to be done
             *   in synchronized manner; this because while we do need to register
             *   instance first, we also must keep lock until resolution is complete
             */
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer) ser).resolve(provider);
            }
        }
    }

    /**
     * @since 1.8
     */
    public void addAndResolveNonTypedSerializer(JavaType type, JsonSerializer<Object> ser,
            SerializerProvider provider)
        throws JsonMappingException
    {
        synchronized (this) {
            if (_sharedMap.put(new TypeKey(type, false), ser) == null) {
                // let's invalidate the read-only copy, too, to get it updated
                _readOnlyMap = null;
            }
            /* Finally: some serializers want to do post-processing, after
             * getting registered (to handle cyclic deps).
             */
            /* 14-May-2011, tatu: As per [JACKSON-570], resolving needs to be done
             *   in synchronized manner; this because while we do need to register
             *   instance first, we also must keep lock until resolution is complete
             */
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer) ser).resolve(provider);
            }
        }
    }

    /**
     * Method called by StdSerializerProvider#flushCachedSerializers() to
     * clear all cached serializers
     */
    public synchronized void flush() {
        _sharedMap.clear();
    }

    /*
    /**************************************************************
    /* Helper class(es)
    /**************************************************************
     */

    /**
     * Key that offers two "modes"; one with raw class, as used for
     * cases were raw class type is available (for example, when using
     * runtime type); and one with full generics-including.
     */
    public final static class TypeKey
    {
        protected int _hashCode;

        protected Class<?> _class;

        protected JavaType _type;

        /**
         * Indicator of whether serializer stored has a type serializer
         * wrapper around it or not; if not, it is "untyped" serializer;
         * if it has, it is "typed"
         */
        protected boolean _isTyped;
        
        public TypeKey(Class<?> key, boolean typed) {
            _class = key;
            _type = null;
            _isTyped = typed;
            _hashCode = hash(key, typed);
        }

        public TypeKey(JavaType key, boolean typed) {
            _type = key;
            _class = null;
            _isTyped = typed;
            _hashCode = hash(key, typed);
        }

        private final static int hash(Class<?> cls, boolean typed) {
            int hash = cls.getName().hashCode();
            if (typed) {
                ++hash;
            }
            return hash;
        }

        private final static int hash(JavaType type, boolean typed) {
            int hash = type.hashCode() - 1;
            if (typed) {
                --hash;
            }
            return hash;
        }
        
        public void resetTyped(Class<?> cls) {
            _type = null;
            _class = cls;
            _isTyped = true;
            _hashCode = hash(cls, true);
        }

        public void resetUntyped(Class<?> cls) {
            _type = null;
            _class = cls;
            _isTyped = false;
            _hashCode = hash(cls, false);
        }
        
        public void resetTyped(JavaType type) {
            _type = type;
            _class = null;
            _isTyped = true;
            _hashCode = hash(type, true);
        }

        public void resetUntyped(JavaType type) {
            _type = type;
            _class = null;
            _isTyped = false;
            _hashCode = hash(type, false);
        }
        
        @Override public final int hashCode() { return _hashCode; }

        @Override public final String toString() {
            if (_class != null) {
                return "{class: "+_class.getName()+", typed? "+_isTyped+"}";
            }
            return "{type: "+_type+", typed? "+_isTyped+"}";
        }
        
        // note: we assume key is never used for anything other than as map key, so:
        @Override public final boolean equals(Object o)
        {
            if (o == this) return true;
            TypeKey other = (TypeKey) o;
            if (other._isTyped == _isTyped) {
                if (_class != null) {
                    return other._class == _class;
                }
                return _type.equals(other._type);
            }
            return false;
        } 
    }
}
