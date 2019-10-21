package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonMappingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Variant of {@link BeanPropertyWriter} which will handle unwrapping
 * of JSON Object (including of properties of Object within surrounding
 * JSON object, and not as sub-object).
 * 
 * @since 1.9
 */
public class UnwrappingBeanPropertyWriter
    extends BeanPropertyWriter
{
    public UnwrappingBeanPropertyWriter(BeanPropertyWriter base) {
        super(base);
    }

    public UnwrappingBeanPropertyWriter(BeanPropertyWriter base, JsonSerializer<Object> ser) {
        super(base, ser);
    }
    
    @Override
    public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser)
    {
        if (getClass() != UnwrappingBeanPropertyWriter.class) {
            throw new IllegalStateException("UnwrappingBeanPropertyWriter sub-class does not override 'withSerializer()'; needs to!");
        }
        // better try to create unwrapping instance
        if (!ser.isUnwrappingSerializer()) {
            ser = ser.unwrappingSerializer();
        }
        return new UnwrappingBeanPropertyWriter(this, ser);
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
        throws Exception
    {
        Object value = get(bean);
        if (value == null) {
            /* Hmmh. I assume we MUST pretty much suppress nulls, since we
             * can't really unwrap it...
             */
            return;
        }
        // For non-nulls, first: simple check for direct cycles
        if (value == bean) {
            _reportSelfReference(bean);
        }
        if (_suppressableValue != null && _suppressableValue.equals(value)) {
            return;
        }
        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap map = _dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(map, cls, prov);
            }
        }

        // note: must verify we are using unwrapping serializer; if not, will write field name
        if (!ser.isUnwrappingSerializer()) {
            jgen.writeFieldName(_name);
        }

        if (_typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, _typeSerializer);
        }
    }

    // need to override as we must get unwrapping instance...
    @Override
    protected JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
            Class<?> type, SerializerProvider provider) throws JsonMappingException
    {
        JsonSerializer<Object> serializer;
        if (_nonTrivialBaseType != null) {
            JavaType subtype = provider.constructSpecializedType(_nonTrivialBaseType, type);
            serializer = provider.findValueSerializer(subtype, this);
        } else {
            serializer = provider.findValueSerializer(type, this);
        }
        if (!serializer.isUnwrappingSerializer()) {
            serializer = serializer.unwrappingSerializer();
        }
        _dynamicSerializers = _dynamicSerializers.newWith(type, serializer);
        return serializer;
    }
}
