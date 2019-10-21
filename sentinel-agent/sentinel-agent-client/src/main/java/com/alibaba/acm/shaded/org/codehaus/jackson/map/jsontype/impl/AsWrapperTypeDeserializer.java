package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Type deserializer used with {@link As#WRAPPER_OBJECT}
 * inclusion mechanism. Simple since JSON structure used is always
 * the same, regardless of structure used for actual value: wrapping
 * is done using a single-element JSON Object where type id is the key,
 * and actual object data as the value.
 * 
 * @author tatus
 */
public class AsWrapperTypeDeserializer extends TypeDeserializerBase
{
    @Deprecated // since 1.9
    public AsWrapperTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property) {
        this(bt, idRes, property, null);
    }

    public AsWrapperTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl)
    {
        super(bt, idRes, property, null);
    }

    @Override
    public As getTypeInclusion() {
        return As.WRAPPER_OBJECT;
    }

    /**
     * Deserializing type id enclosed using WRAPPER_OBJECT style is straightforward
     */
    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _deserialize(jp, ctxt);
    }    

    @Override
    public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
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
        // first, sanity checks
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT,
                    "need JSON Object to contain As.WRAPPER_OBJECT type information for class "+baseTypeName());
        }
        // should always get field name, but just in case...
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME,
                    "need JSON String that contains type id (for subtype of "+baseTypeName()+")");
        }
        JsonDeserializer<Object> deser = _findDeserializer(ctxt, jp.getText());
        jp.nextToken();
        Object value = deser.deserialize(jp, ctxt);
        // And then need the closing END_OBJECT
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_OBJECT,
                    "expected closing END_OBJECT after type information and deserialized value");
        }
        return value;
    }
}
