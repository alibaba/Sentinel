package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

public class StdKeySerializers
{
    protected final static JsonSerializer<Object> DEFAULT_KEY_SERIALIZER = new StdKeySerializer();

    @SuppressWarnings("unchecked")
    protected final static JsonSerializer<Object> DEFAULT_STRING_SERIALIZER
        = (JsonSerializer<Object>)(JsonSerializer<?>) new StringKeySerializer();
    
    private StdKeySerializers() { }

    @SuppressWarnings("unchecked")
    public static JsonSerializer<Object> getStdKeySerializer(JavaType keyType)
    {
        if (keyType == null) {
            return DEFAULT_KEY_SERIALIZER;
        }
        Class<?> cls = keyType.getRawClass();
        if (cls == String.class) {
            return DEFAULT_STRING_SERIALIZER;
        }
        if (cls == Object.class) {
            return DEFAULT_KEY_SERIALIZER;
        }
        // [JACKSON-606] special handling for dates...
        if (Date.class.isAssignableFrom(cls)) {
            return (JsonSerializer<Object>) DateKeySerializer.instance;
        }
        if (Calendar.class.isAssignableFrom(cls)) {
            return (JsonSerializer<Object>) CalendarKeySerializer.instance;
        }
        // If no match, just use default one:
        return DEFAULT_KEY_SERIALIZER;
    }

    /*
    /**********************************************************
    /* Standard implementations
    /**********************************************************
     */

    public static class StringKeySerializer
        extends SerializerBase<String>
    {
        public StringKeySerializer() { super(String.class); }
        
        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeFieldName(value);
        }
    }

    public static class DateKeySerializer
        extends SerializerBase<Date>
    {
        protected final static JsonSerializer<?> instance = new DateKeySerializer();

        public DateKeySerializer() { super(Date.class); }
        
        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            provider.defaultSerializeDateKey(value, jgen);
        }
    }

    public static class CalendarKeySerializer
        extends SerializerBase<Calendar>
    {
        protected final static JsonSerializer<?> instance = new CalendarKeySerializer();

        public CalendarKeySerializer() { super(Calendar.class); }
        
        @Override
        public void serialize(Calendar value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            provider.defaultSerializeDateKey(value.getTimeInMillis(), jgen);
        }
    }
}
