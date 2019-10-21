package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;

/**
 * Intermediate base class for types that contain element(s) of
 * other types. Used for example for List, Map, Object array and
 * Iterator serializers.
 * 
 * @since 1.5
 */
public abstract class ContainerSerializerBase<T>
    extends SerializerBase<T>
{
    /*
    /**********************************************************
    /* Construction, initialization
    /**********************************************************
     */

    protected ContainerSerializerBase(Class<T> t) {
        super(t);
    }
    
    /**
     * Alternate constructor that is (alas!) needed to work
     * around kinks of generic type handling
     * 
     * @param t
     */
    protected ContainerSerializerBase(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    /**
     * Factory(-like) method that can be used to construct a new container
     * serializer that uses specified {@link TypeSerializer} for decorating
     * contained values with additional type information.
     * 
     * @param vts Type serializer to use for contained values; can be null,
     *    in which case 'this' serializer is returned as is
     * @return Serializer instance that uses given type serializer for values if
     *    that is possible (or if not, just 'this' serializer)
     */
    public ContainerSerializerBase<?> withValueTypeSerializer(TypeSerializer vts) {
        if (vts == null) return this;
        return _withValueTypeSerializer(vts);
    }

    public abstract ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts);
}
