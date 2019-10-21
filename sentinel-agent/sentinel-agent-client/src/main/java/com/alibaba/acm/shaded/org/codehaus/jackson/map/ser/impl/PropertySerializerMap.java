package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Helper container used for resolving serializers for dynamic (possibly but not
 * necessarily polymorphic) properties: properties whose type is not forced
 * to use dynamic (declared) type and that are not final.
 * If so, serializer to use can only be established once actual value type is known.
 * Since this happens a lot unless static typing is forced (or types are final)
 * this implementation is optimized for efficiency.
 * Instances are immutable; new instances are created with factory methods: this
 * is important to ensure correct multi-threaded access.
 * 
 * @since 1.7
 */
public abstract class PropertySerializerMap
{
    /**
     * Main lookup method. Takes a "raw" type since usage is always from
     * place where parameterization is fixed such that there can not be
     * type-parametric variations.
     */
    public abstract JsonSerializer<Object> serializerFor(Class<?> type);

    /**
     * Method called if initial lookup fails; will both find serializer
     * and construct new map instance if warranted, and return both
     * @throws JsonMappingException 
     */
    public final SerializerAndMapResult findAndAddSerializer(Class<?> type,
            SerializerProvider provider, BeanProperty property)
        throws JsonMappingException
    {
        JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, newWith(type, serializer));
    }

    public final SerializerAndMapResult findAndAddSerializer(JavaType type,
            SerializerProvider provider, BeanProperty property)
        throws JsonMappingException
    {
        JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, newWith(type.getRawClass(), serializer));
    }

    public abstract PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer);
    
    public static PropertySerializerMap emptyMap() {
        return Empty.instance;
    }
    
    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Value class used for returning tuple that has both serializer
     * that was retrieved and new map instance
     */
    public final static class SerializerAndMapResult
    {
        public final JsonSerializer<Object> serializer;
        public final PropertySerializerMap map;
        
        public SerializerAndMapResult(JsonSerializer<Object> serializer,
                PropertySerializerMap map)
        {
            this.serializer = serializer;
            this.map = map;
        }
    }

    /**
     * Trivial container for bundling type + serializer entries.
     */
    private final static class TypeAndSerializer
    {
        public final Class<?> type;
        public final JsonSerializer<Object> serializer;

        public TypeAndSerializer(Class<?> type, JsonSerializer<Object> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    /*
    /**********************************************************
    /* Implementations
    /**********************************************************
     */

    /**
     * Bogus instance that contains no serializers; used as the default
     * map with new serializers.
     */
    private final static class Empty extends PropertySerializerMap
    {
        protected final static Empty instance = new Empty();

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type) {
            return null; // empty, nothing to find
        }        

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            return new Single(type, serializer);
        }
    }

    /**
     * Map that contains a single serializer; although seemingly silly
     * this is probably the most commonly used variant because many
     * theoretically dynamic or polymorphic types just have single
     * actual type.
     */
    private final static class Single extends PropertySerializerMap
    {
        private final Class<?> _type;
        private final JsonSerializer<Object> _serializer;

        public Single(Class<?> type, JsonSerializer<Object> serializer) {
            _type = type;
            _serializer = serializer;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type)
        {
            if (type == _type) {
                return _serializer;
            }
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            return new Double(_type, _serializer, type, serializer);
        }
    }

    private final static class Double extends PropertySerializerMap
    {
        private final Class<?> _type1, _type2;
        private final JsonSerializer<Object> _serializer1, _serializer2;

        public Double(Class<?> type1, JsonSerializer<Object> serializer1,
                Class<?> type2, JsonSerializer<Object> serializer2)
        {
            _type1 = type1;
            _serializer1 = serializer1;
            _type2 = type2;
            _serializer2 = serializer2;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type)
        {
            if (type == _type1) {
                return _serializer1;
            }
            if (type == _type2) {
                return _serializer2;
            }
            return null;
        }        

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            // Ok: let's just create generic one
            TypeAndSerializer[] ts = new TypeAndSerializer[2];
            ts[0] = new TypeAndSerializer(_type1, _serializer1);
            ts[1] = new TypeAndSerializer(_type2, _serializer2);
            return new Multi(ts);
        }
    }
    
    private final static class Multi extends PropertySerializerMap
    {
        /**
         * Let's limit number of serializers we actually cache; linear
         * lookup won't scale too well beyond smallish number, and if
         * we really want to support larger collections should use
         * a hash map. But it seems unlikely this is a common use
         * case so for now let's just stop building after hard-coded
         * limit. 8 sounds like a reasonable stab for now.
         */
        private final static int MAX_ENTRIES = 8;
        
        private final TypeAndSerializer[] _entries;

        public Multi(TypeAndSerializer[] entries) {
            _entries = entries;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type)
        {
            for (int i = 0, len = _entries.length; i < len; ++i) {
                TypeAndSerializer entry = _entries[i];
                if (entry.type == type) {
                    return entry.serializer;
                }
            }
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer)
        {
            int len = _entries.length;
            // Will only grow up to N entries
            if (len == MAX_ENTRIES) {
                return this;
            }
            // 1.6 has nice resize methods but we are still 1.5
            TypeAndSerializer[] entries = new TypeAndSerializer[len+1];
            System.arraycopy(_entries, 0, entries, 0, len);
            entries[len] = new TypeAndSerializer(type, serializer);
            return new Multi(entries);
        }
    }
}
