package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ContainerDeserializerBase} instead.
 */
@Deprecated
public class CollectionDeserializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.CollectionDeserializer
{
    /**
     * @deprecated Since 1.9, use variant that takes ValueInstantiator
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public CollectionDeserializer(JavaType collectionType, JsonDeserializer<Object> valueDeser,
            TypeDeserializer valueTypeDeser,
            Constructor<Collection<Object>> defCtor)
    {
        super(collectionType, valueDeser, valueTypeDeser, defCtor);
    }

    /**
     * @since 1.9
     */
    public CollectionDeserializer(JavaType collectionType, JsonDeserializer<Object> valueDeser,
            TypeDeserializer valueTypeDeser, ValueInstantiator valueInstantiator)
    {
        super(collectionType, valueDeser, valueTypeDeser, valueInstantiator);
    }

    /**
     * Copy-constructor that can be used by sub-classes to allow
     * copy-on-write styling copying of settings of an existing instance.
     * 
     * @since 1.9
     */
    protected CollectionDeserializer(CollectionDeserializer src)
    {
        super(src);
    }
}
