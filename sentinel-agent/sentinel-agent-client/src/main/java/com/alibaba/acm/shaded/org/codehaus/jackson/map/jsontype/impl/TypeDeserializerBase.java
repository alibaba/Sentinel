package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;
import java.util.HashMap;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * @since 1.5
 * @author tatus
 */
public abstract class TypeDeserializerBase extends TypeDeserializer
{
    protected final TypeIdResolver _idResolver;
    
    protected final JavaType _baseType;

    protected final BeanProperty _property;

    /**
     * Type to use as the default implementation, if type id is
     * missing or can not be resolved.
     * 
     * @since 1.9
     */
    protected final JavaType _defaultImpl;
    
    /**
     * For efficient operation we will lazily build mappings from type ids
     * to actual deserializers, once needed.
     */
    protected final HashMap<String,JsonDeserializer<Object>> _deserializers;

    /**
     * @since 1.9
     */
    protected JsonDeserializer<Object> _defaultImplDeserializer;
    
    /**
     * @deprecated Since 1.9, use the constructor that takes 'defaultImpl'
     */
    @Deprecated
    protected TypeDeserializerBase(JavaType baseType, TypeIdResolver idRes, BeanProperty property) {
        this(baseType, idRes, property, null);
    }

    protected TypeDeserializerBase(JavaType baseType, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl)
    {
        _baseType = baseType;
        _idResolver = idRes;
        _property = property;
        _deserializers = new HashMap<String,JsonDeserializer<Object>>();
        if (defaultImpl == null) {
            _defaultImpl = null;
        } else {
            /* 16-Oct-2011, tatu: should call this via TypeFactory; this is
             *    not entirely safe... however, since Collections/Maps are
             *    seldom (if ever) base types, may be ok.
             */
            _defaultImpl = baseType.forcedNarrowBy(defaultImpl);
        }
    }

    @Override
    public abstract JsonTypeInfo.As getTypeInclusion();

    public String baseTypeName() { return _baseType.getRawClass().getName(); }

    @Override
    public String getPropertyName() { return null; }
    
    @Override    
    public TypeIdResolver getTypeIdResolver() { return _idResolver; }

    @Override    
    public Class<?> getDefaultImpl() {
        return (_defaultImpl == null) ? null : _defaultImpl.getRawClass();
    }
    
    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append('[').append(getClass().getName());
    	sb.append("; base-type:").append(_baseType);
    	sb.append("; id-resolver: ").append(_idResolver);
    	sb.append(']');
    	return sb.toString();
    }
    
    /*
    /**********************************************************
    /* Helper methods for sub-classes
    /**********************************************************
     */

    protected final JsonDeserializer<Object> _findDeserializer(DeserializationContext ctxt, String typeId)
        throws IOException, JsonProcessingException
    {
        JsonDeserializer<Object> deser;

        synchronized (_deserializers) {
            deser = _deserializers.get(typeId);
            if (deser == null) {
                JavaType type = _idResolver.typeFromId(typeId);
                if (type == null) {
                    // As per [JACKSON-614], use the default impl if no type id available:
                    if (_defaultImpl == null) {
                        throw ctxt.unknownTypeException(_baseType, typeId);
                    }
                    deser = _findDefaultImplDeserializer(ctxt);
                } else {
                    /* 16-Dec-2010, tatu: Since nominal type we get here has no (generic) type parameters,
                     *   we actually now need to explicitly narrow from base type (which may have parameterization)
                     *   using raw type.
                     *   
                     *   One complication, though; can not change 'type class' (simple type to container); otherwise
                     *   we may try to narrow a SimpleType (Object.class) into MapType (Map.class), losing actual
                     *   type in process (getting SimpleType of Map.class which will not work as expected)
                     */
                    if (_baseType != null && _baseType.getClass() == type.getClass()) {
                        type = _baseType.narrowBy(type.getRawClass());
                    }
                    deser = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), type, _property);
                }
                _deserializers.put(typeId, deser);
            }
        }
        return deser;
    }

    protected final JsonDeserializer<Object> _findDefaultImplDeserializer(DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        if (_defaultImpl == null) {
            return null;
        }
        synchronized (_defaultImpl) {
            if (_defaultImplDeserializer == null) {
                _defaultImplDeserializer = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(),
                        _defaultImpl, _property);
            }
            return _defaultImplDeserializer;
        }
    }
}
