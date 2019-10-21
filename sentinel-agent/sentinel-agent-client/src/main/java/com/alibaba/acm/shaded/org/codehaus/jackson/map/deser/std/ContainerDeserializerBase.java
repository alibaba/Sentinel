package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Intermediate base deserializer class that adds more shared accessor
 * so that other classes can access information about contained (value)
 * types
 * 
 * @since 1.9 (renamed from 'com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ContainerDeserializer')
 */
public abstract class ContainerDeserializerBase<T>
    extends StdDeserializer<T>
{
    protected ContainerDeserializerBase(Class<?> selfType)
    {
        super(selfType);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Accessor for declared type of contained value elements; either exact
     * type, or one of its supertypes.
     */
    public abstract JavaType getContentType();

    /**
     * Accesor for deserializer use for deserializing content values.
     */
    public abstract JsonDeserializer<Object> getContentDeserializer();
}
