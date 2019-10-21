package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;

/**
 * Interface that defines API for simple extensions that can provide additional serializers
 * for various types. Access is by a single callback method; instance is to either return
 * a configured {@link JsonSerializer} for specified type, or null to indicate that it
 * does not support handling of the type. In latter case, further calls can be made
 * for other providers; in former case returned serializer is used for handling of
 * instances of specified type.
 * 
 * @since 1.7
 */
public interface Serializers
{
    /**
     * Method called by serialization framework first time a serializer is needed for
     * specified type, which is not of a container type (for which other methods are
     * called).
     *<p>
     * Note: in version 1.7, this method was called to find serializers for all
     * type, including container types.
     * 
     * @param type Fully resolved type of instances to serialize
     * @param config Serialization configuration in use
     * @param beanDesc Additional information about type; will always be of type
     *    {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription} (that is,
     *    safe to cast to this more specific type)
     * @param property Property that contains values to serialize
     *    
     * @return Configured serializer to use for the type; or null if implementation
     *    does not recognize or support type
     */
    public JsonSerializer<?> findSerializer(SerializationConfig config,
            JavaType type, BeanDescription beanDesc, BeanProperty property);

    /**
     * Method called by serialization framework first time a serializer is needed for
     * specified array type.
     * Implementation should return a serializer instance if it supports
     * specified type; or null if it does not.
     * 
     * @since 1.8
     */
    public JsonSerializer<?> findArraySerializer(SerializationConfig config,
            ArrayType type, BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer);

    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config,
            CollectionType type, BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer);

    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config,
            CollectionLikeType type, BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer);
    
    public JsonSerializer<?> findMapSerializer(SerializationConfig config,
            MapType type, BeanDescription beanDesc, BeanProperty property,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer);

    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config,
            MapLikeType type, BeanDescription beanDesc, BeanProperty property,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer);

    /**
     * Basic {@link Serializers} implementation that implements all methods but provides
     * no serializers. Its main purpose is to serve as a base class so that
     * sub-classes only need to override methods they need.
     * 
     * @since 1.9
     */
    public static class Base implements Serializers
    {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config,
                JavaType type, BeanDescription beanDesc, BeanProperty property)
        {
            return null;
        }
        
        @Override
        public JsonSerializer<?> findArraySerializer(SerializationConfig config,
                ArrayType type, BeanDescription beanDesc, BeanProperty property,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        {
            return null;
        }

        @Override
        public JsonSerializer<?> findCollectionSerializer(SerializationConfig config,
                CollectionType type, BeanDescription beanDesc, BeanProperty property,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        {
            return null;
        }

        @Override
        public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config,
                CollectionLikeType type, BeanDescription beanDesc, BeanProperty property,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        {
            return null;
        }
            
        @Override
        public JsonSerializer<?> findMapSerializer(SerializationConfig config,
                MapType type, BeanDescription beanDesc, BeanProperty property,
                JsonSerializer<Object> keySerializer,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        {
            return null;
        }

        @Override
        public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config,
                MapLikeType type, BeanDescription beanDesc, BeanProperty property,
                JsonSerializer<Object> keySerializer,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
        {
            return null;
        }
    }

    /**
     * @deprecated As of 1.9, use {@link Base} instead
     */
    @Deprecated
    public static class None extends Base { }
}
