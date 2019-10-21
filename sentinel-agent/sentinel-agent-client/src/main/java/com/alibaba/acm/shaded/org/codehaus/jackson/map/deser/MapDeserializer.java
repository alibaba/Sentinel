package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.MapDeserializer} instead.
 */
@Deprecated
public class MapDeserializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.MapDeserializer
{
    /**
     * @deprecated Since 1.9, use variant that takes ValueInstantiator
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public MapDeserializer(JavaType mapType, Constructor<Map<Object,Object>> defCtor,
            KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser,
            TypeDeserializer valueTypeDeser)
    {
        super(mapType, defCtor, keyDeser, valueDeser, valueTypeDeser);
    }

    public MapDeserializer(JavaType mapType, ValueInstantiator valueInstantiator,
            KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser,
            TypeDeserializer valueTypeDeser)
    {
        super(mapType, valueInstantiator, keyDeser, valueDeser, valueTypeDeser);
    }

    /**
     * Copy-constructor that can be used by sub-classes to allow
     * copy-on-write styling copying of settings of an existing instance.
     * 
     * @since 1.9
     */
    protected MapDeserializer(MapDeserializer src) {
        super(src);
    }
}