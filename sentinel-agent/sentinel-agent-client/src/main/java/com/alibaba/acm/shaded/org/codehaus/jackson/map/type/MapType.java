package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Type that represents "true" Java Map types.
 */
public final class MapType extends MapLikeType
{
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    @Deprecated
    private MapType(Class<?> mapType, JavaType keyT, JavaType valueT) {
        this(mapType, keyT, valueT, null, null);
    }

    private MapType(Class<?> mapType, JavaType keyT, JavaType valueT,
            Object valueHandler, Object typeHandler) {
        super(mapType, keyT, valueT, valueHandler, typeHandler);
    }
    
    public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
        // nominally component types will be just Object.class
        return new MapType(rawType, keyT, valueT, null, null);
    }

    @Override
    protected JavaType _narrow(Class<?> subclass) {
        return new MapType(subclass, _keyType, _valueType,
                _valueHandler, _typeHandler);
    }

    @Override
    public JavaType narrowContentsBy(Class<?> contentClass)
    {
        // Can do a quick check first:
        if (contentClass == _valueType.getRawClass()) {
            return this;
        }
        return new MapType(_class, _keyType, _valueType.narrowBy(contentClass),
                _valueHandler, _typeHandler);
    }

    @Override
    public JavaType widenContentsBy(Class<?> contentClass)
    {
        if (contentClass == _valueType.getRawClass()) {
            return this;
        }
        return new MapType(_class, _keyType, _valueType.widenBy(contentClass),
                _valueHandler, _typeHandler);
    }
    
    @Override
    public JavaType narrowKey(Class<?> keySubclass)
    {
        // Can do a quick check first:
        if (keySubclass == _keyType.getRawClass()) {
            return this;
        }
        return new MapType(_class, _keyType.narrowBy(keySubclass), _valueType,
                _valueHandler, _typeHandler);
    }

    /**
     * @since 1.8
     */
    @Override
    public JavaType widenKey(Class<?> keySubclass)
    {
        // Can do a quick check first:
        if (keySubclass == _keyType.getRawClass()) {
            return this;
        }
        return new MapType(_class, _keyType.widenBy(keySubclass), _valueType,
                _valueHandler, _typeHandler);
    }
    
    // Since 1.7:
    @Override
    public MapType withTypeHandler(Object h) {
        return new MapType(_class, _keyType, _valueType, _valueHandler, h);
    }

    // Since 1.7:
    @Override
    public MapType withContentTypeHandler(Object h)
    {
        return new MapType(_class, _keyType, _valueType.withTypeHandler(h),
                _valueHandler, _typeHandler);
    }
    
    // Since 1.9:
    @Override
    public MapType withValueHandler(Object h) {
        return new MapType(_class, _keyType, _valueType, h, _typeHandler);
    }

    // Since 1.9:
    @Override
    public MapType withContentValueHandler(Object h) {
        return new MapType(_class, _keyType, _valueType.withValueHandler(h),
                _valueHandler, _typeHandler);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */
    
    /**
     * @since 1.9
     */
    @Override
    public MapType withKeyTypeHandler(Object h)
    {
        return new MapType(_class, _keyType.withTypeHandler(h), _valueType,
                _valueHandler, _typeHandler);
    }

    /**
     * @since 1.9
     */
    @Override
    public MapType withKeyValueHandler(Object h) {
        return new MapType(_class, _keyType.withValueHandler(h), _valueType,
                _valueHandler, _typeHandler);
    }
    
    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "[map type; class "+_class.getName()+", "+_keyType+" -> "+_valueType+"]";
    }
}
