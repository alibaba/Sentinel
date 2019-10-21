package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;

/**
 * Type serializer that preferably embeds type information as an additional
 * JSON Object property, if possible (when resulting serialization would
 * use JSON Object). If this is not possible (for JSON Arrays, scalars),
 * uses a JSON Array wrapper (similar to how
 * {@link As#WRAPPER_ARRAY} always works) as a fallback.
 * 
 * @since 1.5
 * @author tatus
 */
public class AsPropertyTypeSerializer
    extends AsArrayTypeSerializer
{
    protected final String _typePropertyName;

    public AsPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property,
            String propName)
    {
        super(idRes, property);
        _typePropertyName = propName;
    }

    @Override
    public String getPropertyName() { return _typePropertyName; }

    @Override
    public As getTypeInclusion() { return As.PROPERTY; }
    
    @Override
    public void writeTypePrefixForObject(Object value, JsonGenerator jgen)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        jgen.writeStringField(_typePropertyName, _idResolver.idFromValue(value));
    }

    @Override
    public void writeTypePrefixForObject(Object value, JsonGenerator jgen, Class<?> type)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        jgen.writeStringField(_typePropertyName, _idResolver.idFromValueAndType(value, type));
    }
    
    //public void writeTypePrefixForArray(Object value, JsonGenerator jgen)
    //public void writeTypePrefixForArray(Object value, JsonGenerator jgen, Class<?> type)
    //public void writeTypePrefixForScalar(Object value, JsonGenerator jgen)
    //public void writeTypePrefixForScalar(Object value, JsonGenerator jgen, Class<?> type)

    @Override
    public void writeTypeSuffixForObject(Object value, JsonGenerator jgen)
        throws IOException, JsonProcessingException
    {
        jgen.writeEndObject();
    }

    //public void writeTypeSuffixForArray(Object value, JsonGenerator jgen)
    //public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen)
}
