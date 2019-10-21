package com.alibaba.acm.shaded.org.codehaus.jackson.map.module;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Simple implementation {@link Deserializers} which allows registration of
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
public class SimpleDeserializers implements Deserializers
{
    protected HashMap<ClassKey,JsonDeserializer<?>> _classMappings = null;

    /*
    /**********************************************************
    /* Life-cycle, construction and configuring
    /**********************************************************
     */
    
    public SimpleDeserializers() { }

    public <T> void addDeserializer(Class<T> forClass, JsonDeserializer<? extends T> deser)
    {
        ClassKey key = new ClassKey(forClass);
        if (_classMappings == null) {
            _classMappings = new HashMap<ClassKey,JsonDeserializer<?>>();
        }
        _classMappings.put(key, deser);
    }
    
    /*
    /**********************************************************
    /* Serializers implementation
    /**********************************************************
     */
    
    @Override
    public JsonDeserializer<?> findArrayDeserializer(ArrayType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    @Override
    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
            DeserializationConfig config, BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type));
    }

    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }

    @Override
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    @Override
    public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType,
            DeserializationConfig config, BeanProperty property)
        throws JsonMappingException
    {
        return (_classMappings == null) ? null : _classMappings.get(new ClassKey(nodeType));
    }
}
