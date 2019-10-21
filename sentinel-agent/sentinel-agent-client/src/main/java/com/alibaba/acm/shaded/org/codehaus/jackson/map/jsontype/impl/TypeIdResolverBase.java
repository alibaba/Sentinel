package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public abstract class TypeIdResolverBase
    implements TypeIdResolver
{
    protected final TypeFactory _typeFactory;

    /**
     * Common base type for all polymorphic instances handled.
     */
    protected final JavaType _baseType;

    protected TypeIdResolverBase(JavaType baseType, TypeFactory typeFactory)
    {
        _baseType = baseType;
        _typeFactory = typeFactory;
    }

    @Override
    public void init(JavaType bt) {
        /* Standard type id resolvers do not need this;
         * only useful for custom ones.
         */
    }

    /**
     * @since 1.9.4
     */
    public String idFromBaseType()
    {
        return idFromValueAndType(null, _baseType.getRawClass());
    }
}
