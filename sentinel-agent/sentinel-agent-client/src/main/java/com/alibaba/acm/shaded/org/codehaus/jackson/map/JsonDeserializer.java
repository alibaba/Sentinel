package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Abstract class that defines API used by {@link ObjectMapper} (and
 * other chained {@link JsonDeserializer}s too) to deserialize Objects of
 * arbitrary types from JSON, using provided {@link JsonParser}.
 */
public abstract class JsonDeserializer<T>
{
    /*
    /**********************************************************
    /* Main deserialization methods
    /**********************************************************
     */
    
    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     *<p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which 
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker.
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     *<p>
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param jp Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about
     *   this deserialization activity.
     *
     * @return Deserializer value
     */
    public abstract T deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    /**
     * Alternate deserialization method (compared to the most commonly
     * used, {@link #deserialize(JsonParser, DeserializationContext)}),
     * which takes in initialized value instance, to be
     * configured and/or populated by deserializer.
     * Method is not necessarily used for all supported types; most commonly
     * it is used
     * for Collections and Maps.
     *<p>
     * Default implementation just throws
     * {@link UnsupportedOperationException}, to indicate that types
     * that do not explicitly add support do not expect to get the call.
     */
    public T deserialize(JsonParser jp, DeserializationContext ctxt,
                         T intoValue)
        throws IOException, JsonProcessingException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deserialization called when type being deserialized is defined to
     * contain additional type identifier, to allow for correctly
     * instantiating correct subtype. This can be due to annotation on
     * type (or its supertype), or due to global settings without
     * annotations.
     *<p>
     * Default implementation may work for some types, but ideally subclasses
     * should not rely on current default implementation.
     * Implementation is mostly provided to avoid compilation errors with older
     * code.
     * 
     * @param typeDeserializer Deserializer to use for handling type information
     * 
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        // We could try calling 
        return (T) typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }

    /*
    /**********************************************************
    /* Fluent factory methods for constructing decorated versions
    /**********************************************************
     */

    /**
     * Method that will return deserializer instance that is able
     * to handle "unwrapped" value instances
     * If no unwrapped instance can be constructed, will simply
     * return this object as-is.
     *<p>
     * Default implementation just returns 'this'
     * indicating that no unwrapped variant exists
     * 
     * @since 1.9
     */
    public JsonDeserializer<T> unwrappingDeserializer() {
        return this;
    }
    
    /*
    /**********************************************************
    /* Other accessors
    /**********************************************************
     */
    
    /**
     * Method that can be called to determine value to be used for
     * representing null values (values deserialized when JSON token
     * is {@link JsonToken#VALUE_NULL}). Usually this is simply
     * Java null, but for some types (especially primitives) it may be
     * necessary to use non-null values.
     *<p>
     * Note that deserializers are allowed to call this just once and
     * then reuse returned value; that is, method is not guaranteed to
     * be called once for each conversion.
     *<p>
     * Default implementation simply returns null.
     */
    public T getNullValue() { return null; }

    /**
     * Method called to determine value to be used for "empty" values
     * (most commonly when deserializing from empty JSON Strings).
     * Usually this is same as {@link #getNullValue} (which in turn
     * is usually simply Java null), but it can be overridden
     * for types. Or, if type should never be converted from empty
     * String, method can also throw an exception.
     *<p>
     * Default implementation simple calls {@link #getNullValue} and
     * returns value.
     * 
     * @since 1.9
     */
    public T getEmptyValue() { return getNullValue(); }
    
    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * This marker class is only to be used with annotations, to
     * indicate that <b>no deserializer is configured</b>.
     *<p>
     * Specifically, this class is to be used as the marker for
     * annotation {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonDeserialize}
     */
    public abstract static class None
        extends JsonDeserializer<Object> { }
}
