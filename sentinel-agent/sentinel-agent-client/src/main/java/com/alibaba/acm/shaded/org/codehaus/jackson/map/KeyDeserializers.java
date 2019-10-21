package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Interface that defines API for simple extensions that can provide additional deserializers
 * for deserializer Map keys of various types, from JSON property names.
 * Access is by a single callback method; instance is to either return
 * a configured {@link KeyDeserializer} for specified type, or null to indicate that it
 * does not support handling of the type. In latter case, further calls can be made
 * for other providers; in former case returned key deserializer is used for handling of
 * key instances of specified type.
 * 
 * @since 1.8
 */
public interface KeyDeserializers
{
    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config,
            BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException;
}
