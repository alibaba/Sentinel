package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.EnumResolver;

/**
 * 
 * <p>
 * Note: casting within this class is all messed up -- just could not figure out a way
 * to properly deal with recursive definition of "EnumSet<K extends Enum<K>, V>
 * 
 * @author tsaloranta
 * 
 * @since 1.9 (moved from higher-level package)
 */
@SuppressWarnings("rawtypes")
public class EnumSetDeserializer
    extends StdDeserializer<EnumSet<?>>
{
    protected final Class<Enum> _enumClass;

    protected final JsonDeserializer<Enum<?>> _enumDeserializer;

    @SuppressWarnings("unchecked" )
    public EnumSetDeserializer(EnumResolver enumRes)
    {
        // fugly, but what we can we do...
        this((Class<Enum>) ((Class<?>) enumRes.getEnumClass()),
                new EnumDeserializer(enumRes));
    }

    @SuppressWarnings("unchecked" )
    public EnumSetDeserializer(Class<?> enumClass, JsonDeserializer<?> deser)
    {
        super(EnumSet.class);
        _enumClass = (Class<Enum>) enumClass;
        _enumDeserializer = (JsonDeserializer<Enum<?>>) deser;
    }
    
    @SuppressWarnings("unchecked") 
    @Override
    public EnumSet<?> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // Ok: must point to START_ARRAY (or equivalent)
        if (!jp.isExpectedStartArrayToken()) {
            throw ctxt.mappingException(EnumSet.class);
        }
        EnumSet result = constructSet();
        JsonToken t;

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            /* What to do with nulls? Fail or ignore? Fail, for now
             * (note: would fail if we passed it to EnumDeserializer, too,
             * but in general nulls should never be passed to non-container
             * deserializers)
             */
            if (t == JsonToken.VALUE_NULL) {
                throw ctxt.mappingException(_enumClass);
            }
            Enum<?> value = _enumDeserializer.deserialize(jp, ctxt);
            result.add(value);
        }
        return result;
    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    @SuppressWarnings("unchecked") 
    private EnumSet constructSet()
    {
    	// superbly ugly... but apparently necessary
    	return EnumSet.noneOf(_enumClass);
    }
}
