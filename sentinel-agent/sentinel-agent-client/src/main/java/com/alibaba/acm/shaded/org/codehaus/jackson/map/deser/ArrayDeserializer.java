package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ArrayType;

/**
 * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ObjectArrayDeserializer} instead.
 */
@Deprecated
public class ArrayDeserializer
    extends com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std.ObjectArrayDeserializer
{
    /**
     * @deprecated
     */
    @Deprecated
    public ArrayDeserializer(ArrayType arrayType, JsonDeserializer<Object> elemDeser)
    {
        this(arrayType, elemDeser, null);
    }

    public ArrayDeserializer(ArrayType arrayType, JsonDeserializer<Object> elemDeser,
            TypeDeserializer elemTypeDeser)
    {
        super(arrayType, elemDeser, elemTypeDeser);
    }
}

