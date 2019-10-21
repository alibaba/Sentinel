package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.lang.reflect.Modifier;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Serializer factory implementation that allows for configuring
 * mapping between types (classes) and serializers to use, by using
 * multiple types of overrides. Existing mappings established by
 * {@link BeanSerializerFactory} (and its super class,
 * {@link BasicSerializerFactory}) are used if no overrides are
 * defined.
 *<p>
 * Unlike base serializer factories ({@link BasicSerializerFactory},
 * {@link BeanSerializerFactory}), this factory is stateful because
 * of configuration settings. It is thread-safe, however, as long as
 * all configuration as done before using the factory -- a single
 * instance can be shared between providers and mappers.
 *<p>
 * Configurations currently available are:
 *<ul>
 * <li>Ability to define explicit mappings between classes and interfaces
 *  and serializers to use. These can be either specific ones (class must
 *  match exactly) or generic ones (any sub-class or class implementing
 *  the interface); specific ones have precedence over generic ones (and
 *  precedence between generic ones is not defined).
 *  </li>
 * <li>Ability to define a single generic base serializer for all Enum
 *   types (precedence below specific serializer mapping)
 *  </li>
 *</ul>
 *<p>
 * Note: as of version 1.7, this class is not as useful as it used to
 * be, due to addition of "modules", which allow simpler addition
 * of custom serializers and deserializers. In fact, use of module system
 * is recommended even when not exposing serializers or deserializers
 * as a pluggable library (just using them locally).
 */
public class CustomSerializerFactory
    extends BeanSerializerFactory
{
    /*
    /**********************************************************
    /* Configuration, direct/special mappings
    /**********************************************************
     */

    /**
     * Direct mappings that are only used for exact class type
     * matches, but not for sub-class checks.
     */
    protected HashMap<ClassKey,JsonSerializer<?>> _directClassMappings = null;

    /**
     * And for Enum handling we may specify a single default
     * serializer to use, regardless of actual enumeration.
     * Usually used to provide "toString - serializer".
     */
    protected JsonSerializer<?> _enumSerializerOverride;

    /*
    /**********************************************************
    /* Configuration, generic (interface, super-class) mappings
    /**********************************************************
     */

    /**
     * And then class-based mappings that are used both for exact and
     * sub-class matches.
     */
    protected HashMap<ClassKey,JsonSerializer<?>> _transitiveClassMappings = null;

    /**
     * And finally interface-based matches.
     */
    protected HashMap<ClassKey,JsonSerializer<?>> _interfaceMappings = null;

    /*
    /**********************************************************
    /* Life-cycle, constructors
    /**********************************************************
     */

    public CustomSerializerFactory() {
        this(null);
    }

    public CustomSerializerFactory(Config config) {
        super(config);
    }
    
    @Override
    public SerializerFactory withConfig(Config config)
    {
        /* 22-Nov-2010, tatu: As with BeanSerializerFactory, must ensure type won't change
         *   with this method, so:
         */
        if (getClass() != CustomSerializerFactory.class) {
            throw new IllegalStateException("Subtype of CustomSerializerFactory ("+getClass().getName()
                    +") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with "
                    +"additional serializer definitions");
        }
        return new CustomSerializerFactory(config);
    }
    
    /*
    /**********************************************************
    /* Configuration: type-to-serializer mappings
    /**********************************************************
     */

    /**
     * Method used to add a generic (transitive) mapping from specified
     * class or its sub-classes into a serializer.
     * When resolving a type into a serializer, explicit class is checked
     * first, then immediate super-class, and so forth along inheritance
     * chain. But if this fails, implemented interfaces are checked;
     * ordering is done such that first interfaces implemented by
     * the exact type are checked (in order returned by
     * {@link Class#getInterfaces}), then super-type's and so forth.
     *<p>
     * Note that adding generic mappings may lead to problems with
     * sub-classing: if sub-classes add new properties, these may not
     * get properly serialized.
     *
     * @param type Class for which specified serializer is to be
     *   used. May be more specific type than what serializer indicates,
     *   but must be compatible (same or sub-class)
     */
    public <T> void addGenericMapping(Class<? extends T> type, JsonSerializer<T> ser)
    {
        // Interface to match?
        ClassKey key = new ClassKey(type);
        if (type.isInterface()) {
            if (_interfaceMappings == null) {
                _interfaceMappings = new HashMap<ClassKey,JsonSerializer<?>>();
            }
            _interfaceMappings.put(key, ser);
        } else { // nope, class:
            if (_transitiveClassMappings == null) {
                _transitiveClassMappings = new HashMap<ClassKey,JsonSerializer<?>>();
            }
            _transitiveClassMappings.put(key, ser);
        }
    }

    /**
     * Method used to add a mapping from specific type -- and only that
     * type -- to specified serializer. This means that binding is not
     * used for sub-types. It also means that no such mappings are to
     * be defined for abstract classes or interfaces: and if an attempt
     * is made, {@link IllegalArgumentException} will be thrown to
     * indicate caller error.
     *
     * @param forClass Class for which specified serializer is to be
     *   used. May be more specific type than what serializer indicates,
     *   but must be compatible (same or sub-class)
     */
    public <T> void addSpecificMapping(Class<? extends T> forClass, JsonSerializer<T> ser)
    {
        ClassKey key = new ClassKey(forClass);

        /* First, let's ensure it's not an interface or abstract class:
         * as those can not be instantiated, such mappings would never
         * get used.
         */
        if (forClass.isInterface()) {
            throw new IllegalArgumentException("Can not add specific mapping for an interface ("+forClass.getName()+")");
        }
        if (Modifier.isAbstract(forClass.getModifiers())) {
            throw new IllegalArgumentException("Can not add specific mapping for an abstract class ("+forClass.getName()+")");
        }

        if (_directClassMappings == null) {
            _directClassMappings = new HashMap<ClassKey,JsonSerializer<?>>();
        }
        _directClassMappings.put(key, ser);
    }

    /**
     * Method that can be used to force specified serializer to be used for
     * serializing all Enum instances. This is most commonly used to specify
     * serializers that call either <code>enum.toString()</code>, or modify
     * value returned by <code>enum.name()</code> (such as upper- or
     * lower-casing it).
     *<p>
     * Note: this serializer has lower precedence than that of specific
     * types; so if a specific serializer is assigned to an Enum type,
     * this serializer will NOT be used. It has higher precedence than
     * generic mappings have however.
     */
    public void setEnumSerializer(JsonSerializer<?> enumSer)
    {
        _enumSerializerOverride = enumSer;
    }

    /*
    /**********************************************************
    /* JsonSerializerFactory impl
    /**********************************************************
     */

    @Override
    @SuppressWarnings("unchecked")    
    public JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType type,
            BeanProperty property)
        throws JsonMappingException
    {
        JsonSerializer<?> ser = findCustomSerializer(type.getRawClass(), config);
        if (ser != null) {
            return (JsonSerializer<Object>) ser;
        }
        return super.createSerializer(config, type, property);
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    protected JsonSerializer<?> findCustomSerializer(Class<?> type, SerializationConfig config)
    {
        JsonSerializer<?> ser = null;
        ClassKey key = new ClassKey(type);

        // First: exact matches
        if (_directClassMappings != null) {
            ser = _directClassMappings.get(key);
            if (ser != null) {
                return ser;
            }
        }

        // No match? Perhaps we can use the enum serializer?
        if (type.isEnum()) {
            if (_enumSerializerOverride != null) {
                return _enumSerializerOverride;
            }
        }

        // Still no match? How about more generic ones?
        // Mappings for super-classes?
        if (_transitiveClassMappings != null) {
            for (Class<?> curr = type; (curr != null); curr = curr.getSuperclass()) {
                key.reset(curr);
                ser = _transitiveClassMappings.get(key);
                if (ser != null) {
                    return ser;
                }
            }
        }

        // And if still no match, how about interfaces?
        if (_interfaceMappings != null) {
            // as per [JACKSON-327], better check actual interface first too...
            key.reset(type);
            ser = _interfaceMappings.get(key);
            if (ser != null) {
                return ser;
            }
            for (Class<?> curr = type; (curr != null); curr = curr.getSuperclass()) {
                ser = _findInterfaceMapping(curr, key);
                if (ser != null) {
                    return ser;
                }
            }
        }
        return null;
    }

    protected JsonSerializer<?> _findInterfaceMapping(Class<?> cls, ClassKey key)
    {
        for (Class<?> iface : cls.getInterfaces()) {
            key.reset(iface);
            JsonSerializer<?> ser = _interfaceMappings.get(key);
            if (ser != null) {
                return ser;
            }
            // [JACKSON-373] need to also check super-interfaces
            ser = _findInterfaceMapping(iface, key);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }
}

