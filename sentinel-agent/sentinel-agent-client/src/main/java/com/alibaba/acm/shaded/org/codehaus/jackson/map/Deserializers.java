package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Interface that defines API for simple extensions that can provide additional deserializers
 * for various types. Access is by a single callback method; instance is to either return
 * a configured {@link JsonDeserializer} for specified type, or null to indicate that it
 * does not support handling of the type. In latter case, further calls can be made
 * for other providers; in former case returned deserializer is used for handling of
 * instances of specified type.
 *<p>
 * Unlike with {@link Serializers}, multiple different methods are used since different
 * kinds of types typically require different kinds of inputs.
 * 
 * @since 1.7
 */
public interface Deserializers
{
    /**
     * Method called to locate serializer for specified array type.
     *<p>
     * Deserializer for element type may be passed, if configured explicitly at higher level (by
     * annotations, typically), but usually are not.
     * Type deserializer for element is passed if one is needed based on contextual information
     * (annotations on declared element class; or on field or method type is associated with).
     * 
     * @param type Type of array instances to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate deserializer for component type (if
     *    one not provided, or needs to be overridden)
     * @param property Property that contains array value  (null for root values)
     * @param elementTypeDeserializer If element type needs polymorphic type handling, this is
     *    the type information deserializer to use; should usually be used as is when constructing
     *    array deserializer.
     * @param elementDeserializer Deserializer to use for elements, if explicitly defined (by using
     *    annotations, for exmple). May be null, in which case it should be resolved here (or using
     *    {@link ResolvableDeserializer} callback)
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config,
            DeserializerProvider provider,
            BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;

    /**
     * Method called to locate serializer for specified {@link java.util.Collection} (List, Set etc) type.
     *<p>
     * Deserializer for element type may be passed, if configured explicitly at higher level (by
     * annotations, typically), but usually are not.
     * Type deserializer for element is passed if one is needed based on contextual information
     * (annotations on declared element class; or on field or method type is associated with).
     * 
     * @param type Type of collection instances to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate dependant deserializers if and as necessary
     *   (but note that in many cases resolution must be deferred by using {@link ResolvableDeserializer} callback)
     * @param property Property that contains array value  (null for root values)
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * @param elementTypeDeserializer If element type needs polymorphic type handling, this is
     *    the type information deserializer to use; should usually be used as is when constructing
     *    array deserializer.
     * @param elementDeserializer Deserializer to use for elements, if explicitly defined (by using
     *    annotations, for exmple). May be null, in which case it should be resolved here (or using
     *    {@link ResolvableDeserializer} callback)
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;

    /**
     * Method called to locate serializer for specified
     * "Collection-like" type (one that acts
     * like {@link java.util.Collection} but does not implement it).
     *<p>
     * Deserializer for element type may be passed, if configured explicitly at higher level (by
     * annotations, typically), but usually are not.
     * Type deserializer for element is passed if one is needed based on contextual information
     * (annotations on declared element class; or on field or method type is associated with).
     * 
     * @param type Type of instances to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate dependant deserializers if and as necessary
     *   (but note that in many cases resolution must be deferred by using {@link ResolvableDeserializer} callback)
     * @param property Property that contains array value  (null for root values)
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * @param elementTypeDeserializer If element type needs polymorphic type handling, this is
     *    the type information deserializer to use; should usually be used as is when constructing
     *    array deserializer.
     * @param elementDeserializer Deserializer to use for elements, if explicitly defined (by using
     *    annotations, for exmple). May be null, in which case it should be resolved here (or using
     *    {@link ResolvableDeserializer} callback)
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     * 
     * @since 1.8
     */
    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;
    
    /**
     * Method called to locate deserializer for specified {@link java.lang.Enum} type.
     * 
     * @param type Type of {@link java.lang.Enum} instances to deserialize
     * @param config Configuration in effect
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config,
            BeanDescription beanDesc, BeanProperty property)
        throws JsonMappingException;

    /**
     * Method called to locate deserializer for specified {@link java.util.Map} type.
     *<p>
     * Deserializer for element type may be passed, if configured explicitly at higher level (by
     * annotations, typically), but usually are not.
     * Type deserializer for element is passed if one is needed based on contextual information
     * (annotations on declared element class; or on field or method type is associated with).
     *<p>
     * Similarly, a {@link KeyDeserializer} may be passed, but this is only done if there is
     * a specific configuration override (annotations) to indicate instance to use. Otherwise
     * null is passed, and key deserializer needs to be obtained using {@link DeserializerProvider}
     * 
     * @param type Type of {@link java.util.Map} instances to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate dependant deserializers if and as necessary
     *   (but note that in many cases resolution must be deferred by using {@link ResolvableDeserializer} callback)
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * @param keyDeserializer Key deserializer use, if it is defined via annotations or other configuration;
     *    null if default key deserializer for key type can be used.
     * @param elementTypeDeserializer If element type needs polymorphic type handling, this is
     *    the type information deserializer to use; should usually be used as is when constructing
     *    array deserializer.
     * @param elementDeserializer Deserializer to use for elements, if explicitly defined (by using
     *    annotations, for exmple). May be null, in which case it should be resolved here (or using
     *    {@link ResolvableDeserializer} callback)
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;

    /**
     * Method called to locate serializer for specified
     * "Map-like" type (one that acts
     * like {@link java.util.Map} but does not implement it).
     *<p>
     * Deserializer for element type may be passed, if configured explicitly at higher level (by
     * annotations, typically), but usually are not.
     * Type deserializer for element is passed if one is needed based on contextual information
     * (annotations on declared element class; or on field or method type is associated with).
     *<p>
     * Similarly, a {@link KeyDeserializer} may be passed, but this is only done if there is
     * a specific configuration override (annotations) to indicate instance to use. Otherwise
     * null is passed, and key deserializer needs to be obtained using {@link DeserializerProvider}
     * 
     * @param type Type of {@link java.util.Map} instances to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate dependant deserializers if and as necessary
     *   (but note that in many cases resolution must be deferred by using {@link ResolvableDeserializer} callback)
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * @param keyDeserializer Key deserializer use, if it is defined via annotations or other configuration;
     *    null if default key deserializer for key type can be used.
     * @param elementTypeDeserializer If element type needs polymorphic type handling, this is
     *    the type information deserializer to use; should usually be used as is when constructing
     *    array deserializer.
     * @param elementDeserializer Deserializer to use for elements, if explicitly defined (by using
     *    annotations, for exmple). May be null, in which case it should be resolved here (or using
     *    {@link ResolvableDeserializer} callback)
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     * 
     * @since 1.8
     */
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;
    
    /**
     * Method called to locate deserializer for specified JSON tree node type.
     * 
     * @param nodeType Specific type of JSON tree nodes to deserialize (subtype of {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode})
     * @param config Configuration in effect
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType, DeserializationConfig config,
            BeanProperty property)
        throws JsonMappingException;
    
    /**
     * Method called to locate deserializer for specified value type which does not belong to any other
     * category (not an Enum, Collection, Map, Array or tree node)
     * 
     * @param type Bean type to deserialize
     * @param config Configuration in effect
     * @param provider Provider that can be used to locate dependant deserializers if and as necessary
     *   (but note that in many cases resolution must be deferred by using {@link ResolvableDeserializer} callback)
     * @param beanDesc Definition of the enumeration type that contains class annotations and
     *    other information typically needed for building deserializers (note: always instance
     *    of {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription})
     * 
     * @return Deserializer to use for the type; or null if this provider does not know how to construct it
     */
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc,
            BeanProperty property)
        throws JsonMappingException;

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Basic {@link Deserializers} implementation that implements all methods but provides
     * no deserializers. Its main purpose is to serve as a base class so that
     * sub-classes only need to override methods they need, as most of the time some
     * of methods are not needed (especially enumeration and array deserializers are
     * very rarely overridden).
     * 
     * @since 1.9
     */
    public static class Base implements Deserializers
    {
        @Override
        public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config,
                DeserializerProvider provider, BeanProperty property,
                TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config,
                DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
                TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config,
                DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
                TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config,
                DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
                KeyDeserializer keyDeserializer,
                TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config,
                DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
                KeyDeserializer keyDeserializer,
                TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config,
                BeanDescription beanDesc,
                BeanProperty property)
            throws JsonMappingException
        {
            return null;
        }
        
        @Override
        public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType,
                DeserializationConfig config,
                BeanProperty property)
            throws JsonMappingException
        {
            return null;
        }

        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
                DeserializerProvider provider,
                BeanDescription beanDesc,
                BeanProperty property)
            throws JsonMappingException
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
