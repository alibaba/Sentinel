package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Type deserializer used with {@link As#WRAPPER_ARRAY}
 * inclusion mechanism. Simple since JSON structure used is always
 * the same, regardless of structure used for actual value: wrapping
 * is done using a 2-element JSON Array where type id is the first
 * element, and actual object data as second element.
 * 
 * @author tatus
 */
public class AsArrayTypeDeserializer extends TypeDeserializerBase
{
    @Deprecated // since 1.9
    public AsArrayTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property) {
        this(bt, idRes, property, null);
    }

    public AsArrayTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl)
    {
        super(bt, idRes, property, defaultImpl);
    }
    
    @Override
    public As getTypeInclusion() {
        return As.WRAPPER_ARRAY;
    }

    /**
     * Method called when actual object is serialized as JSON Array.
     */
    @Override
    public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _deserialize(jp, ctxt);
    }

    /**
     * Method called when actual object is serialized as JSON Object
     */
    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _deserialize(jp, ctxt);
    }
    
    @Override
    public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _deserialize(jp, ctxt);
    }    

    @Override
    public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _deserialize(jp, ctxt);
    }    
    
    /*
    /***************************************************************
    /* Internal methods
    /***************************************************************
     */

    /**
     * Method that handles type information wrapper, locates actual
     * subtype deserializer to use, and calls it to do actual
     * deserialization.
     */
    private final Object _deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        boolean hadStartArray = jp.isExpectedStartArrayToken();
        JsonDeserializer<Object> deser = _findDeserializer(ctxt, _locateTypeId(jp, ctxt));
        Object value = deser.deserialize(jp, ctxt);
        // And then need the closing END_ARRAY
        if (hadStartArray && jp.nextToken() != JsonToken.END_ARRAY) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY,
                    "expected closing END_ARRAY after type information and deserialized value");
        }
        return value;
    }    
    
    protected final String _locateTypeId(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (!jp.isExpectedStartArrayToken()) {
            // Add instanceof check because we cannot add method to TypeIdResolver interface due to backwards
            // compatibility issues.  We can however add it to the base method, which gets at least some of
            // the functionality we want.  Falls back to old exception for other types of TypeIdResolvers
            if (_idResolver instanceof TypeIdResolverBase) {
                if (_defaultImpl != null) { // but let's require existence of default impl, as a safeguard
                    return ((TypeIdResolverBase) _idResolver).idFromBaseType();
                }
            }
            throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY,
                    "need JSON Array to contain As.WRAPPER_ARRAY type information for class "+baseTypeName());
        }
        // And then type id as a String
        if (jp.nextToken() != JsonToken.VALUE_STRING) {
            if (_idResolver instanceof TypeIdResolverBase) {
                if (_defaultImpl != null) { // but let's require existence of default impl, as a safeguard
                    return ((TypeIdResolverBase) _idResolver).idFromBaseType();
                }
            }
            throw ctxt.wrongTokenException(jp, JsonToken.VALUE_STRING,
                    "need JSON String that contains type id (for subtype of "+baseTypeName()+")");
        }
        String result = jp.getText();
        jp.nextToken();
        return result;
    }
}
