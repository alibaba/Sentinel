package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Abstract class that defines API used by {@link ObjectMapper} (and
 * other chained {@link JsonSerializer}s too) to serialize Objects of
 * arbitrary types into JSON, using provided {@link JsonGenerator}.
 *<p>
 * NOTE: it is recommended that custom serializers extend
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializerBase} instead
 * of this class, since it will implement many of optional
 * methods of this class.
 */
public abstract class JsonSerializer<T>
{
    /*
    /**********************************************************
    /* Fluent factory methods for constructing decorated versions
    /**********************************************************
     */

    /**
     * Method that will return serializer instance that produces
     * "unwrapped" serialization, if applicable for type being
     * serialized (which is the case for some serializers
     * that produce JSON Objects as output).
     * If no unwrapped serializer can be constructed, will simply
     * return serializer as-is.
     *<p>
     * Default implementation just returns serializer as-is,
     * indicating that no unwrapped variant exists
     * 
     * @since 1.9
     */
    public JsonSerializer<T> unwrappingSerializer() {
        return this;
    }

    /**
     * Accessor for checking whether this serializer is an
     * "unwrapping" serializer; this is necessary to know since
     * it may also require caller to suppress writing of the
     * leading property name.
     * 
     * @since 1.9
     */
    public boolean isUnwrappingSerializer() {
        return false;
    }
    
    /*
    /**********************************************************
    /* Serialization methods
    /**********************************************************
     */

    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value Value to serialize; can <b>not</b> be null.
     * @param jgen Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     */
    public abstract void serialize(T value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException;

    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles, using specified type serializer
     * for embedding necessary type information.
     *<p>
     * Default implementation will ignore serialization of type information,
     * and just calls {@link #serialize}: serializers that can embed
     * type information should override this to implement actual handling.
     * Most common such handling is done by something like:
     *<pre>
     *  // note: method to call depends on whether this type is serialized as JSON scalar, object or Array!
     *  typeSer.writeTypePrefixForScalar(value, jgen);
     *  serialize(value, jgen, provider);
     *  typeSer.writeTypeSuffixForScalar(value, jgen);
     *</pre>
     *
     * @param value Value to serialize; can <b>not</b> be null.
     * @param jgen Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     * @param typeSer Type serializer to use for including type information
     *
     * @since 1.5
     */
    public void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        serialize(value, jgen, provider);
    }
    
    /*
    /**********************************************************
    /* Introspection methods needed for type handling 
    /**********************************************************
     */
    
    /**
     * Method for accessing type of Objects this serializer can handle.
     * Note that this information is not guaranteed to be exact -- it
     * may be a more generic (super-type) -- but it should not be
     * incorrect (return a non-related type).
     *<p>
     * Default implementation will return null, which essentially means
     * same as returning <code>Object.class</code> would; that is, that
     * nothing is known about handled type.
     *<p>
     */
    public Class<T> handledType() { return null; }
    
    /*
    /**********************************************************
    /* Helper class(es)
    /**********************************************************
     */

    /**
     * This marker class is only to be used with annotations, to
     * indicate that <b>no serializer is configured</b>.
     *<p>
     * Specifically, this class is to be used as the marker for
     * annotation {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonSerialize}.
     */
    public abstract static class None
        extends JsonSerializer<Object> { }
}
