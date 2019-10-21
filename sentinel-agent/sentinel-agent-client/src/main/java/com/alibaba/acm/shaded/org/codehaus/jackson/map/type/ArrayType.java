package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import java.lang.reflect.Array;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Array types represent Java arrays, both primitive and object valued.
 * Further, Object-valued arrays can have element type of any other
 * legal {@link JavaType}.
 */
public final class ArrayType
    extends TypeBase
{
    /**
     * Type of elements in the array.
     */
    protected final JavaType _componentType;

    /**
     * We will also keep track of shareable instance of empty array,
     * since it usually needs to be constructed any way; and because
     * it is essentially immutable and thus can be shared.
     */
    protected final Object _emptyArray;

    private ArrayType(JavaType componentType, Object emptyInstance,
            Object valueHandler, Object typeHandler)
    {
        super(emptyInstance.getClass(), componentType.hashCode(),
                valueHandler, typeHandler);
        _componentType = componentType;
        _emptyArray = emptyInstance;
    }

    /**
     * @deprecated Since 1.9, if you must directly instantiate,
     *  call method that takes handlers
     */
    @Deprecated
    public static ArrayType construct(JavaType componentType)
    {
        return construct(componentType, null, null);
    }                                   

    public static ArrayType construct(JavaType componentType,
            Object valueHandler, Object typeHandler)
    {
        /* This is bit messy: there is apparently no other way to
         * reconstruct actual concrete/raw array class from component
         * type, than to construct an instance, get class (same is
         * true for GenericArracyType as well; hence we won't bother
         * passing that in).
         */
        Object emptyInstance = Array.newInstance(componentType.getRawClass(), 0);
        return new ArrayType(componentType, emptyInstance, null, null);
    }                                   
    
    // Since 1.7:
    @Override
    public ArrayType withTypeHandler(Object h)
    {
        if (h == _typeHandler) {
            return this;
        }
        return new ArrayType(_componentType, _emptyArray, _valueHandler, h);
    }

    // Since 1.7:
    @Override
    public ArrayType withContentTypeHandler(Object h)
    {
        if (h == _componentType.getTypeHandler()) {
            return this;
        }
        return new ArrayType(_componentType.withTypeHandler(h), _emptyArray,
                _valueHandler, _typeHandler);
    }

    // Since 1.9:
    @Override
    public ArrayType withValueHandler(Object h) {
        if (h == _valueHandler) {
            return this;
        }
        return new ArrayType(_componentType, _emptyArray, h, _typeHandler);
    }

    // Since 1.9:
    @Override
    public ArrayType withContentValueHandler(Object h) {
        if (h == _componentType.getValueHandler()) {
            return this;
        }
        return new ArrayType(_componentType.withValueHandler(h), _emptyArray,
                _valueHandler, _typeHandler);
    }
    
    @Override
    protected String buildCanonicalName() {
        return _class.getName();
    }
    
    /*
    /**********************************************************
    /* Methods for narrowing conversions
    /**********************************************************
     */

    /**
     * Handling of narrowing conversions for arrays is trickier: for now,
     * it is not even allowed.
     */
    @Override
    protected JavaType _narrow(Class<?> subclass)
    {
        /* Ok: need a bit of indirection here. First, must replace component
         * type (and check that it is compatible), then re-construct.
         */
        if (!subclass.isArray()) { // sanity check, should never occur
            throw new IllegalArgumentException("Incompatible narrowing operation: trying to narrow "+toString()+" to class "+subclass.getName());
        }
        /* Hmmh. This is an awkward back reference... but seems like the
         * only simple way to do it.
         */
        Class<?> newCompClass = subclass.getComponentType();
        /* 14-Mar-2011, tatu: it gets even worse, as we do not have access to
         *   currently configured TypeFactory. This could theoretically cause
         *   problems (when narrowing from array of Objects, to array of non-standard
         *   Maps, for example); but for now need to defer solving this until
         *   it actually becomes a real problem, not just potential one.
         *   (famous last words?)
         */
        JavaType newCompType = TypeFactory.defaultInstance().constructType(newCompClass);
        return construct(newCompType, _valueHandler, _typeHandler);
    }

    /**
     * For array types, both main type and content type can be modified;
     * but ultimately they are interchangeable.
     */
    @Override
    public JavaType narrowContentsBy(Class<?> contentClass)
    {
        // Can do a quick check first:
        if (contentClass == _componentType.getRawClass()) {
            return this;
        }
        return construct(_componentType.narrowBy(contentClass),
                _valueHandler, _typeHandler);
    }

    @Override
    public JavaType widenContentsBy(Class<?> contentClass)
    {
        // Can do a quick check first:
        if (contentClass == _componentType.getRawClass()) {
            return this;
        }
        return construct(_componentType.widenBy(contentClass),
                _valueHandler, _typeHandler);
    }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public boolean isArrayType() { return true; }
    
    /**
     * For some odd reason, modifiers for array classes would
     * claim they are abstract types. Not so, at least for our
     * purposes.
     */
    @Override
    public boolean isAbstract() { return false; }

    /**
     * For some odd reason, modifiers for array classes would
     * claim they are abstract types. Not so, at least for our
     * purposes.
     */
    @Override
    public boolean isConcrete() { return true; }

    @Override
    public boolean hasGenericTypes() {
        // arrays are not parameterized, but element type may be:
        return _componentType.hasGenericTypes();
    }
    
    /**
     * Not sure what symbolic name is used internally, if any;
     * let's follow naming of Collection types here.
     * Should not really matter since array types have no
     * super types.
     */
    @Override
    public String containedTypeName(int index) {
        if (index == 0) return "E";
        return null;
    }
    
    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    @Override
    public boolean isContainerType() { return true; }

    @Override
    public JavaType getContentType() { return  _componentType; }

    @Override
    public int containedTypeCount() { return 1; }
    @Override
    public JavaType containedType(int index) {
            return (index == 0) ? _componentType : null;
    }
    
    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        sb.append('[');
        return _componentType.getGenericSignature(sb);
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        sb.append('[');
        return _componentType.getErasedSignature(sb);
    }
    
    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "[array type, component type: "+_componentType+"]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        ArrayType other = (ArrayType) o;
        return _componentType.equals(other._componentType);
    }
}
