package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Type deserializer used with {@link As#EXTERNAL_PROPERTY} inclusion mechanism.
 * Actual implementation may look bit strange since it depends on comprehensive
 * pre-processing done by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializer}
 * to basically transform external type id into structure that looks more like
 * "wrapper-array" style inclusion. This intermediate form is chosen to allow
 * supporting all possible JSON structures.
 * 
 * @since 1.9
 */
public class AsExternalTypeDeserializer extends AsArrayTypeDeserializer
{
    protected final String _typePropertyName;
    
    public AsExternalTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl,
            String typePropName)
    {
        super(bt, idRes, property, defaultImpl);
        _typePropertyName = typePropName;
    }

    @Override
    public As getTypeInclusion() {
        return As.EXTERNAL_PROPERTY;
    }

    @Override
    public String getPropertyName() { return _typePropertyName; }
}
