package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Type that represents Java Collection types (Lists, Sets).
 */
public final class CollectionType
    extends CollectionLikeType
{
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    private CollectionType(Class<?> collT, JavaType elemT,
            Object valueHandler, Object typeHandler)
    {
        super(collT,  elemT, valueHandler, typeHandler);
    }

    @Override
    protected JavaType _narrow(Class<?> subclass) {
        return new CollectionType(subclass, _elementType, null, null);
    }

    @Override
    public JavaType narrowContentsBy(Class<?> contentClass)
    {
        // Can do a quick check first:
        if (contentClass == _elementType.getRawClass()) {
            return this;
        }
        return new CollectionType(_class, _elementType.narrowBy(contentClass),
                _valueHandler, _typeHandler);
    }

    @Override
    public JavaType widenContentsBy(Class<?> contentClass)
    {
        // Can do a quick check first:
        if (contentClass == _elementType.getRawClass()) {
            return this;
        }
        return new CollectionType(_class, _elementType.widenBy(contentClass),
                _valueHandler, _typeHandler);
    }
    
    public static CollectionType construct(Class<?> rawType, JavaType elemT)
    {
        // nominally component types will be just Object.class
        return new CollectionType(rawType, elemT, null, null);
    }

    // Since 1.7:
    @Override
    public CollectionType withTypeHandler(Object h) {
        return new CollectionType(_class, _elementType, _valueHandler, h);
    }

    // Since 1.7:
    @Override
    public CollectionType withContentTypeHandler(Object h)
    {
        return new CollectionType(_class, _elementType.withTypeHandler(h),
                _valueHandler, _typeHandler);
    }

    // Since 1.9:
    @Override
    public CollectionType withValueHandler(Object h) {
        return new CollectionType(_class, _elementType, h, _typeHandler);
    }

    // Since 1.9:
    @Override
    public  CollectionType withContentValueHandler(Object h) {
        return new CollectionType(_class, _elementType.withValueHandler(h),
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
        return "[collection type; class "+_class.getName()+", contains "+_elementType+"]";
    }
}
