package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype;

import java.util.Collection;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializationConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Interface that defines builders that are configured based on
 * annotations (like {@link JsonTypeInfo} or JAXB annotations),
 * and produce type serializers and deserializers used for
 * handling type information embedded in JSON to allow for safe
 * polymorphic type handling.
 *<p>
 * Builder is first initialized by calling {@link #init} method, and then
 * configured using <code>setXxx</code> (and <code>registerXxx</code>)
 * methods. Finally, after calling all configuration methods,
 * {@link #buildTypeSerializer} or {@link #buildTypeDeserializer}
 * will be called to get actual type resolver constructed
 * and used for resolving types for configured base type and its
 * subtypes.
 *<p>
 * Note that instances are used for two related but distinct use cases:
 *<ul>
 * <li>To create builders to use with explicit type information
 *    inclusion (usually via <code>@JsonTypeInfo</code> annotation)
 *   </li>
 * <li>To create builders when "default typing" is used; if so, type information
 *   is automatically included for certain kind of types, regardless of annotations
 *   </li>
 *</ul>
 * Important distinction between the cases is that in first case, calls to
 * create builders are only made when builders are certainly needed; whereas
 * in second case builder has to first verify whether type information is
 * applicable for given type, and if not, just return null to indicate this.
 * 
 * @since 1.5
 * @author tatu
 */
public interface TypeResolverBuilder<T extends TypeResolverBuilder<T>>
{
    /*
    /**********************************************************
    /* Accessors
    /**********************************************************
     */

    /**
     * Accessor for currently configured default type; implementation
     * class that may be used in case no valid type information is
     * available during type resolution
     * 
     * @since 1.9
     */
    public Class<?> getDefaultImpl();
    
    /*
    /**********************************************************
    /* Actual builder methods
    /**********************************************************
     */

    /**
     * Method for building type serializer based on current configuration
     * of this builder.
     * 
     * @param baseType Base type that constructed resolver will
     *    handle; super type of all types it will be used for.
     */
    public TypeSerializer buildTypeSerializer(SerializationConfig config,
            JavaType baseType, Collection<NamedType> subtypes, BeanProperty property);

    /**
     * Method for building type deserializer based on current configuration
     * of this builder.
     * 
     * @param baseType Base type that constructed resolver will
     *    handle; super type of all types it will be used for.
     * @param subtypes Known subtypes of the base type.
     */
    public TypeDeserializer buildTypeDeserializer( DeserializationConfig config,
            JavaType baseType, Collection<NamedType> subtypes, BeanProperty property);
    
    /*
    /**********************************************************
    /* Initialization method(s) that must be called before other
    /* configuration
    /**********************************************************
     */

    /**
     * Initialization method that is called right after constructing
     * the builder instance.
     *
     * @param idType Which type metadata is used
     * @param res (optional) Custom type id resolver used, if any
     * 
     * @return Resulting builder instance (usually this builder,
     *   but not necessarily)
     */
    public T init(JsonTypeInfo.Id idType, TypeIdResolver res);
    
    /*
    /**********************************************************
    /* Methods for configuring resolver to build 
    /**********************************************************
     */
    
    /**
     * Method for specifying mechanism to use for including type metadata
     * in JSON.
     * If not explicitly called, setting defaults to
     * {@link As#PROPERTY}.
     * 
     * @param includeAs Mechanism used for including type metadata in JSON
     * 
     * @return Resulting builder instance (usually this builder,
     *   but not necessarily)
     */
    public T inclusion(As includeAs);

    /**
     * Method for specifying name of property used for including type
     * information. Not used for all inclusion mechanisms;
     * usually only used with {@link As#PROPERTY}.
     *<p>
     * If not explicitly called, name of property to use is based on
     * defaults for {@link com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.Id} configured.
     * 
     * @param propName Name of JSON property to use for including
     *    type information
     * 
     * @return Resulting builder instance (usually this builder,
     *   but not necessarily)
     */
    public T typeProperty(String propName);

    /**
     * Method for specifying default implementation to use if type id 
     * is either not available, or can not be resolved.
     * 
     * @since 1.9
     */
    public T defaultImpl(Class<?> defaultImpl);
}
