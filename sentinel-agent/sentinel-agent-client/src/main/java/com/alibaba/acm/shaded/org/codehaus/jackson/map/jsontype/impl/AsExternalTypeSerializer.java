package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo.As;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.BeanProperty;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;

/**
 * Type serializer that preferably embeds type information as an "external"
 * type property; embedded in enclosing JSON object.
 * Note that this serializer should only be used when value is being output
 * at JSON Object context; otherwise it can not work reliably, and will have
 * to revert operation similar to {@link AsPropertyTypeSerializer}.
 *<p>
 * Note that implementation of serialization is bit cumbersome as we must
 * serialized external type id AFTER object; this because callback only
 * occurs after field name has been written.
 * 
 * @since 1.9
 */
public class AsExternalTypeSerializer
   extends TypeSerializerBase
{
   protected final String _typePropertyName;

   public AsExternalTypeSerializer(TypeIdResolver idRes, BeanProperty property,
           String propName)
   {
       super(idRes, property);
       _typePropertyName = propName;
   }

   @Override
   public String getPropertyName() { return _typePropertyName; }

   @Override
   public As getTypeInclusion() { return As.EXTERNAL_PROPERTY; }
   
   @Override
   public void writeTypePrefixForObject(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen);
   }

   @Override
   public void writeTypePrefixForObject(Object value, JsonGenerator jgen, Class<?> type)
       throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen, type);
   }
   
   @Override
   public void writeTypePrefixForArray(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen);
   }

   @Override
   public void writeTypePrefixForArray(Object value, JsonGenerator jgen, Class<?> type)
           throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen, type);
   }

   @Override
   public void writeTypePrefixForScalar(Object value, JsonGenerator jgen)
           throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen);
   }

   @Override
   public void writeTypePrefixForScalar(Object value, JsonGenerator jgen, Class<?> type)
           throws IOException, JsonProcessingException
   {
       _writePrefix(value, jgen, type);
   }

   @Override
   public void writeTypeSuffixForObject(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       _writeSuffix(value, jgen);
   }

   @Override
   public void writeTypeSuffixForArray(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       _writeSuffix(value, jgen);
   }
   
   @Override
   public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       _writeSuffix(value, jgen);
   }

   /*
   /**********************************************************
   /* Helper methods
   /**********************************************************
    */
   
   protected final void _writePrefix(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       jgen.writeStartObject();
   }

   protected final void _writePrefix(Object value, JsonGenerator jgen, Class<?> type)
       throws IOException, JsonProcessingException
   {
       jgen.writeStartObject();
   }
   
   protected final void _writeSuffix(Object value, JsonGenerator jgen)
       throws IOException, JsonProcessingException
   {
       jgen.writeEndObject();
       jgen.writeStringField(_typePropertyName, _idResolver.idFromValue(value));
   }
}
