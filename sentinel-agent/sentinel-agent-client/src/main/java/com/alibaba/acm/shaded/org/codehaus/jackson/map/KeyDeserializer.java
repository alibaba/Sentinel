package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Abstract class that defines API used for deserializing JSON content
 * field names into Java Map keys. These deserializers are only used
 * if the Map key class is not <code>String</code> or <code>Object</code>.
 */
public abstract class KeyDeserializer
{
    /**
     * Method called to deserialize a {@link java.util.Map} key from JSON property name.
     */
    public abstract Object deserializeKey(String key, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;

    /**
     * This marker class is only to be used with annotations, to
     * indicate that <b>no deserializer is configured</b>.
     *<p>
     * Specifically, this class is to be used as the marker for
     * annotation {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JsonDeserialize}.
     *
     * @since 1.3
     */
    public abstract static class None
        extends KeyDeserializer { }
}
