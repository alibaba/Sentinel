package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;

/**
 * Type serializer that will embed type information in an array,
 * as the first element, and actual value as the second element.
 * 
 * @since 1.5
 * @author tatu
 */
public class AsArrayTypeSerializer
    extends TypeSerializerBase
{
    public AsArrayTypeSerializer(TypeIdResolver idRes, BeanProperty property)
    {
        super(idRes, property);
    }

    @Override
    public As getTypeInclusion() { return As.WRAPPER_ARRAY; }
    
    @Override
    public void writeTypePrefixForObject(Object value, JsonGenerator jgen)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValue(value));
        jgen.writeStartObject();
    }

    @Override
    public void writeTypePrefixForObject(Object value, JsonGenerator jgen,
            Class<?> type)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValueAndType(value, type));
        jgen.writeStartObject();
    }
    
    @Override
    public void writeTypePrefixForArray(Object value, JsonGenerator jgen)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValue(value));
        jgen.writeStartArray();
    }

    @Override
    public void writeTypePrefixForArray(Object value, JsonGenerator jgen,
            Class<?> type)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValueAndType(value, type));
        jgen.writeStartArray();
    }
    
    @Override
    public void writeTypePrefixForScalar(Object value, JsonGenerator jgen)
            throws IOException, JsonProcessingException
    {
        // only need the wrapper array
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValue(value));
    }

    @Override
    public void writeTypePrefixForScalar(Object value, JsonGenerator jgen,
            Class<?> type)
        throws IOException, JsonProcessingException
    {
        // only need the wrapper array
        jgen.writeStartArray();
        jgen.writeString(_idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypeSuffixForObject(Object value, JsonGenerator jgen)
            throws IOException, JsonProcessingException
    {
        jgen.writeEndObject();
        jgen.writeEndArray();
    }

    @Override
    public void writeTypeSuffixForArray(Object value, JsonGenerator jgen)
            throws IOException, JsonProcessingException
    {
        // wrapper array first, and then array caller needs to close
        jgen.writeEndArray();
        jgen.writeEndArray();
    }

    @Override
    public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen)
            throws IOException, JsonProcessingException
    {
        // just the wrapper array to close
        jgen.writeEndArray();
    }
}
