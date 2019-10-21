package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Container class that can be used to wrap any Object instances (including
 * nulls), and will serialize embedded in
 * <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> wrapping.
 * 
 * @see com.alibaba.acm.shaded.org.codehaus.jackson.map.util.JSONWrappedObject
 * 
 * @author tatu
 * @since 1.5
 */
public class JSONPObject
    implements JsonSerializableWithType
{
    /**
     * JSONP function name to use for serialization
     */
    protected final String _function;
    
    /**
     * Value to be serialized as JSONP padded; can be null.
     */
    protected final Object _value;

    /**
     * Optional static type to use for serialization; if null, runtime
     * type is used. Can be used to specify declared type which defines
     * serializer to use, as well as aspects of extra type information
     * to include (if any).
     */
    protected final JavaType _serializationType;

    public JSONPObject(String function, Object value) {
        this(function, value, (JavaType) null);
    }

    public JSONPObject(String function, Object value, JavaType asType)
    {
        _function = function;
        _value = value;
        _serializationType = asType;
    }

    /**
     * @deprecated Since 1.8; instead use variant that takes JavaType: this ensures
     *    that type information is properly resolved
     */
    @Deprecated
    public JSONPObject(String function, Object value, Class<?> rawType) {
        _function = function;
        _value = value;
        _serializationType = (rawType == null) ? null : TypeFactory.defaultInstance().constructType(rawType);
    }
    
    /*
    /**********************************************************
    /* JsonSerializable(WithType) implementation
    /**********************************************************
     */
    
    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException
    {
        // No type for JSONP wrapping: value serializer will handle typing for value:
        serialize(jgen, provider);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void serialize(JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        // First, wrapping:
        jgen.writeRaw(_function);
        jgen.writeRaw('(');
        if (_value == null) {
            provider.defaultSerializeNull(jgen);
        } else if (_serializationType != null) {
            provider.findTypedValueSerializer(_serializationType, true, null).serialize(_value, jgen, provider);
        } else {
            Class<?> cls = _value.getClass();
            provider.findTypedValueSerializer(cls, true, null).serialize(_value, jgen, provider);
        }
        jgen.writeRaw(')');
    }

    /*
    /**************************************************************
    /* Accessors
    /**************************************************************
     */
    
    public String getFunction() { return _function; }
    public Object getValue() { return _value; }
    public JavaType getSerializationType() { return _serializationType; }
}
