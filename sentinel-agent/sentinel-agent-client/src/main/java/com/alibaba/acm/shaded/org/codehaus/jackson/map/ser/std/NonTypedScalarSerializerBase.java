package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;

/**
 * Intermediate base class for limited number of scalar types
 * that should never include type information. These are "native"
 * types that are default mappings for corresponding JSON scalar
 * types: {@link java.lang.String}, {@link java.lang.Integer},
 * {@link java.lang.Double} and {@link java.lang.Boolean}.
 * 
 * @since 1.9 (refactored from 'com.alibaba.acm.shaded.org.codehaus.jackson.map.StdSerializers#NontTypedScalarSerializer')
 */
public abstract class NonTypedScalarSerializerBase<T>
    extends ScalarSerializerBase<T>
{
    protected NonTypedScalarSerializerBase(Class<T> t) {
        super(t);
    }

    @Override
    public final void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        // no type info, just regular serialization
        serialize(value, jgen, provider);            
    }
}
