package com.alibaba.acm.shaded.org.codehaus.jackson.map.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Simple implementation {@link KeyDeserializers} which allows registration of
 * deserializers based on raw (type erased class).
 * It can work well for basic bean and scalar type deserializers, but is not
 * a good fit for handling generic types (like {@link Map}s and {@link Collection}s
 * or array types).
 *<p>
 * Unlike {@link SimpleSerializers}, this class does not currently support generic mappings;
 * all mappings must be to exact declared deserialization type.
 * 
 * @since 1.7
 */
public class SimpleKeyDeserializers implements KeyDeserializers
{
    protected HashMap<ClassKey,KeyDeserializer> _classMappings = null;

    /*
    /**********************************************************
    /* Life-cycle, construction and configuring
    /**********************************************************
     */
    
    public SimpleKeyDeserializers() { }

    public SimpleKeyDeserializers addDeserializer(Class<?> forClass, KeyDeserializer deser)
    {
        if (_classMappings == null) {
            _classMappings = new HashMap<ClassKey,KeyDeserializer>();
        }
        _classMappings.put(new ClassKey(forClass), deser);
        return this;
    }

    /*
    /**********************************************************
    /* Serializers implementation
    /**********************************************************
     */

    @Override
    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, 
            BeanDescription beanDesc, BeanProperty property)
    {
        if (_classMappings == null) {
            return null;
        }
        return _classMappings.get(new ClassKey(type.getRawClass()));
    }
}
