package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiators;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Abstract class that defines API used by {@link DeserializerProvider}
 * to obtain actual
 * {@link JsonDeserializer} instances from multiple distinct factories.
 *<p>
 * Since there are multiple broad categories of deserializers, there are 
 * multiple factory methods:
 *<ul>
 * <li>For JSON "Array" type, we need 2 methods: one to deal with expected
 *   Java arrays ({@link #createArrayDeserializer})
 *   and the other for other Java containers like {@link java.util.List}s
 *   and {@link java.util.Set}s ({@link #createCollectionDeserializer(DeserializationConfig, DeserializerProvider, CollectionType, BeanProperty)})
 *  </li>
 * <li>For JSON "Object" type, we need 2 methods: one to deal with
 *   expected Java {@link java.util.Map}s
 *   ({@link #createMapDeserializer}), and another for POJOs
 *   ({@link #createBeanDeserializer(DeserializationConfig, DeserializerProvider, JavaType, BeanProperty)}.
 *  </li>
 * <li>For Tree Model ({@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode}) properties there is
 *    {@link #createTreeDeserializer(DeserializationConfig, DeserializerProvider, JavaType, BeanProperty)}
 * <li>For enumerated types ({@link java.lang.Enum}) there is
 *    {@link #createEnumDeserializer(DeserializationConfig, DeserializerProvider, JavaType, BeanProperty)}
 *  </li>
 * <li>For all other types, {@link #createBeanDeserializer(DeserializationConfig, DeserializerProvider, JavaType, BeanProperty)}
 *   is used.
 * </ul>
 *<p>
 * All above methods take 2 type arguments, except for the first one
 * which takes just a single argument.
 */
public abstract class DeserializerFactory
{
    protected final static Deserializers[] NO_DESERIALIZERS = new Deserializers[0];

    /*
    /**********************************************************
    /* Helper class to contain configuration settings
    /**********************************************************
     */

    /**
     * Configuration settings container class for bean deserializer factory
     * 
     * @since 1.7
     */
    public abstract static class Config
    {
        /**
         * Fluent/factory method used to construct a configuration object that
         * has same deserializer providers as this instance, plus one specified
         * as argument. Additional provider will be added before existing ones,
         * meaning it has priority over existing definitions.
         */
        public abstract Config withAdditionalDeserializers(Deserializers additional);

        /**
         * Fluent/factory method used to construct a configuration object that
         * has same key deserializer providers as this instance, plus one specified
         * as argument. Additional provider will be added before existing ones,
         * meaning it has priority over existing definitions.
         */
        public abstract Config withAdditionalKeyDeserializers(KeyDeserializers additional);
        
        /**
         * Fluent/factory method used to construct a configuration object that
         * has same configuration as this instance plus one additional
         * deserialiazer modifier. Added modifier has the highest priority (that is, it
         * gets called before any already registered modifier).
         */
        public abstract Config withDeserializerModifier(BeanDeserializerModifier modifier);

        /**
         * Fluent/factory method used to construct a configuration object that
         * has same configuration as this instance plus one additional
         * abstract type resolver.
         * Added resolver has the highest priority (that is, it
         * gets called before any already registered resolver).
         * 
         * @since 1.8
         */
        public abstract Config withAbstractTypeResolver(AbstractTypeResolver resolver);

        /**
         * Fluent/factory method used to construct a configuration object that
         * has same configuration as this instance plus specified additional
         * value instantiator provider object.
         * Added instantiator provider has the highest priority (that is, it
         * gets called before any already registered resolver).
         * 
         * @param instantiators Object that can provide {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator}s for
         *    constructing POJO values during deserialization
         * 
         * @since 1.9
         */
        public abstract Config withValueInstantiators(ValueInstantiators instantiators);
        
        public abstract Iterable<Deserializers> deserializers();

        /**
         * @since 1.8
         */
        public abstract Iterable<KeyDeserializers> keyDeserializers();
        
        public abstract Iterable<BeanDeserializerModifier> deserializerModifiers();

        /**
         * @since 1.8
         */
        public abstract Iterable<AbstractTypeResolver> abstractTypeResolvers();

        /**
         * @since 1.9
         */
        public abstract Iterable<ValueInstantiators> valueInstantiators();
        
        public abstract boolean hasDeserializers();

        /**
         * @since 1.8
         */
        public abstract boolean hasKeyDeserializers();
        
        public abstract boolean hasDeserializerModifiers();

        /**
         * @since 1.8
         */
        public abstract boolean hasAbstractTypeResolvers();

        /**
         * @since 1.9
         */
        public abstract boolean hasValueInstantiators();
    }

    /*
    /********************************************************
    /* Configuration handling
    /********************************************************
     */

    /**
     * @since 1.7
     */
    public abstract Config getConfig();
    
    /**
     * Method used for creating a new instance of this factory, but with different
     * configuration. Reason for specifying factory method (instead of plain constructor)
     * is to allow proper sub-classing of factories.
     *<p>
     * Note that custom sub-classes <b>must override</b> implementation
     * of this method, as it usually requires instantiating a new instance of
     * factory type. Check out javadocs for
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializerFactory} for more details.
     * 
     * @since 1.7
     */
    public abstract DeserializerFactory withConfig(Config config);

    /**
     * Convenience method for creating a new factory instance with additional deserializer
     * provider.
     * 
     * @since 1.7
     */
    public final DeserializerFactory withAdditionalDeserializers(Deserializers additional) {
        return withConfig(getConfig().withAdditionalDeserializers(additional));
    }

    /**
     * Convenience method for creating a new factory instance with additional
     * {@link KeyDeserializers}.
     * 
     * @since 1.8
     */
    public final DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers additional) {
        return withConfig(getConfig().withAdditionalKeyDeserializers(additional));
    }
    
    /**
     * Convenience method for creating a new factory instance with additional
     * {@link BeanDeserializerModifier}.
     * 
     * @since 1.7
     */
    public final DeserializerFactory withDeserializerModifier(BeanDeserializerModifier modifier) {
        return withConfig(getConfig().withDeserializerModifier(modifier));
    }

    /**
     * Convenience method for creating a new factory instance with additional
     * {@link AbstractTypeResolver}.
     * 
     * @since 1.7
     */
    public final DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver resolver) {
        return withConfig(getConfig().withAbstractTypeResolver(resolver));
    }

    /**
     * Convenience method for creating a new factory instance with additional
     * {@link ValueInstantiators}.
     * 
     * @since 1.9
     */
    public final DeserializerFactory withValueInstantiators(ValueInstantiators instantiators) {
        return withConfig(getConfig().withValueInstantiators(instantiators));
    }
    
    /*
    /**********************************************************
    /* Basic DeserializerFactory API:
    /**********************************************************
     */

    /**
     * Method that can be called to try to resolve an abstract type
     * (interface, abstract class) into a concrete type, or at least
     * something "more concrete" (abstract class instead of interface).
     * Will either return passed type, or a more specific type.
     * 
     * @since 1.9
     */
    public abstract JavaType mapAbstractType(DeserializationConfig config, JavaType type)
        throws JsonMappingException;

    /**
     * Method that is to find all creators (constructors, factory methods)
     * for the bean type to deserialize.
     * 
     * @since 1.9
     */
    public abstract ValueInstantiator findValueInstantiator(DeserializationConfig config,
            BasicBeanDescription beanDesc)
        throws JsonMappingException;
    
    /**
     * Method called to create (or, for completely immutable deserializers,
     * reuse) a deserializer that can convert JSON content into values of
     * specified Java "bean" (POJO) type.
     * At this point it is known that the type is not otherwise recognized
     * as one of structured types (array, Collection, Map) or a well-known
     * JDK type (enum, primitives/wrappers, String); this method only
     * gets called if other options are exhausted. This also means that
     * this method can be overridden to add support for custom types.
     *
     * @param type Type to be deserialized
     * @param p Provider that can be called to create deserializers for
     *   contained member types
     */
    public abstract JsonDeserializer<Object> createBeanDeserializer(DeserializationConfig config, DeserializerProvider p,
            JavaType type, BeanProperty property)
        throws JsonMappingException;

    /**
     * Method called to create (or, for completely immutable deserializers,
     * reuse) a deserializer that can convert JSON content into values of
     * specified Java type.
     *
     * @param type Type to be deserialized
     * @param p Provider that can be called to create deserializers for
     *   contained member types
     */
    public abstract JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config, DeserializerProvider p,
            ArrayType type, BeanProperty property)
        throws JsonMappingException;

    public abstract JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config,
            DeserializerProvider p, CollectionType type, BeanProperty property)
        throws JsonMappingException;

    /**
     * @since 1.8
     */
    public abstract JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, CollectionLikeType type, BeanProperty property)
        throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createEnumDeserializer(DeserializationConfig config,
            DeserializerProvider p, JavaType type, BeanProperty property)
        throws JsonMappingException;

    public abstract JsonDeserializer<?> createMapDeserializer(DeserializationConfig config,
            DeserializerProvider p, MapType type, BeanProperty property)
        throws JsonMappingException;

    /**
     * @since 1.8
     */
    public abstract JsonDeserializer<?> createMapLikeDeserializer(DeserializationConfig config,
            DeserializerProvider p, MapLikeType type, BeanProperty property)
        throws JsonMappingException;

    /**
     * Method called to create and return a deserializer that can construct
     * JsonNode(s) from JSON content.
     */
    public abstract JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, DeserializerProvider p,
            JavaType type, BeanProperty property)
        throws JsonMappingException;

    /**
     * Method called to find if factory knows how to create a key deserializer
     * for specified type; currently this means checking if a module has registered
     * possible deserializers.
     * 
     * @return Key deserializer to use for specified type, if one found; null if not
     *   (and default key deserializer should be used)
     * 
     * @since 1.8
     */
    public KeyDeserializer createKeyDeserializer(DeserializationConfig config, JavaType type,
            BeanProperty property)
        throws JsonMappingException
    {
        // Default implementation returns null for backwards compatibility reasons
        return null;
    }
    
    /**
     * Method called to find and create a type information deserializer for given base type,
     * if one is needed. If not needed (no polymorphic handling configured for type),
     * should return null.
     *<p>
     * Note that this method is usually only directly called for values of container (Collection,
     * array, Map) types and root values, but not for bean property values.
     *
     * @param baseType Declared base type of the value to deserializer (actual
     *    deserializer type will be this type or its subtype)
     * 
     * @return Type deserializer to use for given base type, if one is needed; null if not.
     * 
     * @since 1.5
     */
    public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType,
            BeanProperty property)
        throws JsonMappingException
    {
        // Default implementation returns null for backwards compatibility reasons
        return null;
    }
}
