package com.alibaba.acm.shaded.org.codehaus.jackson.map.module;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.org.codehaus.jackson.Version;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;

/**
 * Simple {@link Module} implementation that allows registration
 * of serializers and deserializers, and bean serializer
 * and deserializer modifiers.
 * 
 * @since 1.7
 */
public class SimpleModule extends Module
{
    protected final String _name;
    protected final Version _version;
    
    protected SimpleSerializers _serializers = null;
    protected SimpleDeserializers _deserializers = null;

    protected SimpleSerializers _keySerializers = null;
    protected SimpleKeyDeserializers _keyDeserializers = null;

    /**
     * Lazily-constructed resolver used for storing mappings from
     * abstract classes to more specific implementing classes
     * (which may be abstract or concrete)
     */
    protected SimpleAbstractTypeResolver _abstractTypes = null;

    /**
     * Lazily-constructed resolver used for storing mappings from
     * abstract classes to more specific implementing classes
     * (which may be abstract or concrete)
     */
    protected SimpleValueInstantiators _valueInstantiators = null;

    /**
     * Lazily-constructed map that contains mix-in definitions, indexed
     * by target class, value being mix-in to apply.
     * 
     * @since 1.9
     */
    protected HashMap<Class<?>, Class<?>> _mixins = null;
    
    /*
    /**********************************************************
    /* Life-cycle: creation
    /**********************************************************
     */
    
    public SimpleModule(String name, Version version)
    {
        _name = name;
        _version = version;
    }

    /*
    /**********************************************************
    /* Simple setters to allow overriding
    /**********************************************************
     */

    /**
     * Resets all currently configured serializers.
     * 
     * @since 1.9
     */
    public void setSerializers(SimpleSerializers s) {
        _serializers = s;
    }

    /**
     * Resets all currently configured deserializers.
     * 
     * @since 1.9
     */
    public void setDeserializers(SimpleDeserializers d) {
        _deserializers = d;
    }

    /**
     * Resets all currently configured key serializers.
     * 
     * @since 1.9
     */
    public void setKeySerializers(SimpleSerializers ks) {
        _keySerializers = ks;
    }

    /**
     * Resets all currently configured key deserializers.
     * 
     * @since 1.9
     */
    public void setKeyDeserializers(SimpleKeyDeserializers kd) {
        _keyDeserializers = kd;
    }

    /**
     * Resets currently configured abstract type mappings
     *
     * @since 1.9
     */
    public void setAbstractTypes(SimpleAbstractTypeResolver atr) {
        _abstractTypes = atr;        
    }

    /**
     * Resets all currently configured value instantiators
     * 
     * @since 1.9
     */
    public void setValueInstantiators(SimpleValueInstantiators svi) {
        _valueInstantiators = svi;
    }
    
    /*
    /**********************************************************
    /* Configuration methods
    /**********************************************************
     */
    
    public SimpleModule addSerializer(JsonSerializer<?> ser)
    {
        if (_serializers == null) {
            _serializers = new SimpleSerializers();
        }
        _serializers.addSerializer(ser);
        return this;
    }
    
    public <T> SimpleModule addSerializer(Class<? extends T> type, JsonSerializer<T> ser)
    {
        if (_serializers == null) {
            _serializers = new SimpleSerializers();
        }
        _serializers.addSerializer(type, ser);
        return this;
    }

    public <T> SimpleModule addKeySerializer(Class<? extends T> type, JsonSerializer<T> ser)
    {
        if (_keySerializers == null) {
            _keySerializers = new SimpleSerializers();
        }
        _keySerializers.addSerializer(type, ser);
        return this;
    }
    
    public <T> SimpleModule addDeserializer(Class<T> type, JsonDeserializer<? extends T> deser)
    {
        if (_deserializers == null) {
            _deserializers = new SimpleDeserializers();
        }
        _deserializers.addDeserializer(type, deser);
        return this;
    }

    public SimpleModule addKeyDeserializer(Class<?> type, KeyDeserializer deser)
    {
        if (_keyDeserializers == null) {
            _keyDeserializers = new SimpleKeyDeserializers();
        }
        _keyDeserializers.addDeserializer(type, deser);
        return this;
    }

    /**
     * Lazily-constructed resolver used for storing mappings from
     * abstract classes to more specific implementing classes
     * (which may be abstract or concrete)
     */
    public <T> SimpleModule addAbstractTypeMapping(Class<T> superType,
            Class<? extends T> subType)
    {
        if (_abstractTypes == null) {
            _abstractTypes = new SimpleAbstractTypeResolver();
        }
        // note: addMapping() will verify arguments
        _abstractTypes = _abstractTypes.addMapping(superType, subType);
        return this;
    }

    /**
     * Method for registering {@link ValueInstantiator} to use when deserializing
     * instances of type <code>beanType</code>.
     *<p>
     * Instantiator is
     * registered when module is registered for <code>ObjectMapper</code>.
     */
    public SimpleModule addValueInstantiator(Class<?> beanType, ValueInstantiator inst)
    {
        if (_valueInstantiators == null) {
            _valueInstantiators = new SimpleValueInstantiators();
        }
        _valueInstantiators = _valueInstantiators.addValueInstantiator(beanType, inst);
        return this;
    }

    /**
     * Method for specifying that annotations define by <code>mixinClass</code>
     * should be "mixed in" with annotations that <code>targetType</code>
     * has (as if they were directly included on it!).
     *<p>
     * Mix-in annotations are
     * registered when module is registered for <code>ObjectMapper</code>.
     */
    public SimpleModule setMixInAnnotation(Class<?> targetType, Class<?> mixinClass)
    {
        if (_mixins == null) {
            _mixins = new HashMap<Class<?>, Class<?>>();
        }
        _mixins.put(targetType, mixinClass);
        return this;
    }
    
    /*
    /**********************************************************
    /* Module impl
    /**********************************************************
     */
    
    @Override
    public String getModuleName() {
        return _name;
    }

    @Override
    public void setupModule(SetupContext context)
    {
        if (_serializers != null) {
            context.addSerializers(_serializers);
        }
        if (_deserializers != null) {
            context.addDeserializers(_deserializers);
        }
        if (_keySerializers != null) {
            context.addKeySerializers(_keySerializers);
        }
        if (_keyDeserializers != null) {
            context.addKeyDeserializers(_keyDeserializers);
        }
        if (_abstractTypes != null) {
            context.addAbstractTypeResolver(_abstractTypes);
        }
        if (_valueInstantiators != null) {
            context.addValueInstantiators(_valueInstantiators);
        }
        if (_mixins != null) {
            for (Map.Entry<Class<?>,Class<?>> entry : _mixins.entrySet()) {
                context.setMixInAnnotations(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Version version() {
        return _version;
    }
}
