package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import java.lang.reflect.*;

/**
 * Simple replacement for {@link java.lang.Class} (and/or various Type subtypes)
 * that is used as part of single-path extends/implements chain to express
 * specific relationship between one subtype and one supertype. This is needed
 * for resolving type parameters. Instances are doubly-linked so that chain
 * can be traversed in both directions
 * 
 * @since 1.6
 */
public class HierarchicType
{
    /**
     * Type which will be either plain {@link java.lang.Class} or
     * {@link java.lang.reflect.ParameterizedType}.
     */
    protected final Type _actualType;
    
    protected final Class<?> _rawClass;

    protected final ParameterizedType _genericType;
    
    protected HierarchicType _superType;

    protected HierarchicType _subType;
    
    public HierarchicType(Type type)
    {
        this._actualType = type;
        if (type instanceof Class<?>) {
            _rawClass = (Class<?>) type;
            _genericType = null;
        } else if (type instanceof ParameterizedType) {
            _genericType = (ParameterizedType) type;
            _rawClass = (Class<?>) _genericType.getRawType();
        } else { // should never happen... can't extend GenericArrayType?
            throw new IllegalArgumentException("Type "+type.getClass().getName()+" can not be used to construct HierarchicType");
        }
    }

    private HierarchicType(Type actualType, Class<?> rawClass, ParameterizedType genericType,
        HierarchicType superType, HierarchicType subType)
    {
        _actualType = actualType;
        _rawClass = rawClass;
        _genericType = genericType;
        _superType = superType;
        _subType = subType;
    }
    
    /**
     * Method that can be used to create a deep clone of this hierarchic type, including
     * super types (but not subtypes)
     * 
     * @since 1.9
     */
    public HierarchicType deepCloneWithoutSubtype()
    {
        HierarchicType sup = (_superType == null) ? null : _superType.deepCloneWithoutSubtype();
        HierarchicType result = new HierarchicType(_actualType, _rawClass, _genericType, sup, null);
        if (sup != null) {
            sup.setSubType(result);
        }
        return result;
    }
    
    public void setSuperType(HierarchicType sup) { _superType = sup; }
    public final HierarchicType getSuperType() { return _superType; }
    public void setSubType(HierarchicType sub) { _subType = sub; }
    public final HierarchicType getSubType() { return _subType; }
    
    public final boolean isGeneric() { return _genericType != null; }
    public final ParameterizedType asGeneric() { return _genericType; }

    public final Class<?> getRawClass() { return _rawClass; }
    
    @Override
    public String toString() {
        if (_genericType != null) {
            return _genericType.toString();
        }
        return _rawClass.getName();
    }
    
}
