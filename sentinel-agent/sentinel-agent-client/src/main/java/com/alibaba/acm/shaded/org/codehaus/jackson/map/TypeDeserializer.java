package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;

/**
 * Interface for deserializing type information from JSON content, to
 * type-safely deserialize data into correct polymorphic instance
 * (when type inclusion has been enabled for type handled).
 *<p>
 * Separate deserialization methods are needed because serialized
 * form for inclusion mechanism {@link As#PROPERTY}
 * is slighty different if value is not expressed as JSON Object:
 * and as such both type deserializer and serializer need to
 * JSON Object form (array, object or other (== scalar)) being
 * used.
 * 
 * @since 1.5
 * @author tatus
 */
public abstract class TypeDeserializer
{
    /*
    /**********************************************************
    /* Introspection
    /**********************************************************
     */

    /**
     * Accessor for type information inclusion method
     * that deserializer uses; indicates how type information
     * is (expected to be) embedded in JSON input.
     */
    public abstract As getTypeInclusion();

    /**
     * Name of property that contains type information, if
     * property-based inclusion is used.
     */
    public abstract String getPropertyName();

    /**
     * Accessor for object that handles conversions between
     * types and matching type ids.
     */
    public abstract TypeIdResolver getTypeIdResolver();

    /**
     * Accessor for "default implementation" type; optionally defined
     * class to use in cases where type id is not
     * accessible for some reason (either missing, or can not be
     * resolved)
     * 
     * @since 1.9
     */
    public abstract Class<?> getDefaultImpl();
    
    /*
    /*********************************************************
    /* Type deserialization methods
    /**********************************************************
     */

    /**
     * Method called to let this type deserializer handle 
     * deserialization of "typed" object, when value itself
     * is serialized as JSON Object (regardless of Java type).
     * Method needs to figure out intended
     * polymorphic type, locate {@link JsonDeserializer} to use, and
     * call it with JSON data to deserializer (which does not contain
     * type information).
     */
    public abstract Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    /**
     * Method called to let this type deserializer handle 
     * deserialization of "typed" object, when value itself
     * is serialized as JSON Array (regardless of Java type).
     * Method needs to figure out intended
     * polymorphic type, locate {@link JsonDeserializer} to use, and
     * call it with JSON data to deserializer (which does not contain
     * type information).
     */
    public abstract Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    /**
     * Method called to let this type deserializer handle 
     * deserialization of "typed" object, when value itself
     * is serialized as a scalar JSON value (something other
     * than Array or Object), regardless of Java type.
     * Method needs to figure out intended
     * polymorphic type, locate {@link JsonDeserializer} to use, and
     * call it with JSON data to deserializer (which does not contain
     * type information).
     */
    public abstract Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    /**
     * Method called to let this type deserializer handle 
     * deserialization of "typed" object, when value itself
     * may have been serialized using any kind of JSON value
     * (Array, Object, scalar). Should only be called if JSON
     * serialization is polymorphic (not Java type); for example when
     * using JSON node representation, or "untyped" Java object
     * (which may be Map, Collection, wrapper/primitive etc).
     */
    public abstract Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

}
    