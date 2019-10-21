package com.alibaba.acm.shaded.org.codehaus.jackson.map.ext;

import java.io.IOException;
import java.util.*;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializerBase;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ToStringSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Provider;

/**
 * Provider for serializers that handle some basic data types
 * for <a href="http://joda-time.sourceforge.net/">Joda</a> date/time library.
 *<p>
 * Since version 1.5, more types are supported. These types use slightly
 * different approach to serialization than core date types: "timestamp"
 * notation is implemented using JSON arrays, for improved readability.
 *
 * @since 1.4
 */
public class JodaSerializers
    implements Provider<Map.Entry<Class<?>,JsonSerializer<?>>>
{
    final static HashMap<Class<?>,JsonSerializer<?>> _serializers = new HashMap<Class<?>,JsonSerializer<?>>();
    static {
        _serializers.put(DateTime.class, new DateTimeSerializer());
        _serializers.put(LocalDateTime.class, new LocalDateTimeSerializer());
        _serializers.put(LocalDate.class, new LocalDateSerializer());
        _serializers.put(DateMidnight.class, new DateMidnightSerializer());
        // [JACKSON-706]:
        _serializers.put(Period.class, ToStringSerializer.instance);
    }

    public JodaSerializers() { }
    
    @Override
    public Collection<Map.Entry<Class<?>,JsonSerializer<?>>> provide() {
        return _serializers.entrySet();
    }

    /*
    /**********************************************************
    /* Intermediate base classes
    /**********************************************************
     */

    protected abstract static class JodaSerializer<T> extends SerializerBase<T>
    {
        final static DateTimeFormatter _localDateTimeFormat = ISODateTimeFormat.dateTime();
        final static DateTimeFormatter _localDateFormat = ISODateTimeFormat.date();

        protected JodaSerializer(Class<T> cls) { super(cls); }

        protected String printLocalDateTime(ReadablePartial dateValue)
            throws IOException, JsonProcessingException
        {
            return _localDateTimeFormat.print(dateValue);
        }

        protected String printLocalDate(ReadablePartial dateValue)
            throws IOException, JsonProcessingException
        {
            return _localDateFormat.print(dateValue);
        }

        protected String printLocalDate(ReadableInstant dateValue)
            throws IOException, JsonProcessingException
        {
            return _localDateFormat.print(dateValue);
        }
    }
    
    /*
    /**********************************************************
    /* Concrete serializers
    /**********************************************************
     */

    public final static class DateTimeSerializer
        extends JodaSerializer<DateTime>
    {
        public DateTimeSerializer() { super(DateTime.class); }

        @Override
        public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                jgen.writeNumber(value.getMillis());
            } else {
                jgen.writeString(value.toString());
            }
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, java.lang.reflect.Type typeHint)
        {
            return createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)
                    ? "number" : "string", true);
        }
    }
 
    /**
     * 
     * @since 1.5
     */
    public final static class LocalDateTimeSerializer
        extends JodaSerializer<LocalDateTime>
    {
        public LocalDateTimeSerializer() { super(LocalDateTime.class); }
    
        @Override
        public void serialize(LocalDateTime dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                // Timestamp here actually means an array of values
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeNumber(dt.hourOfDay().get());
                jgen.writeNumber(dt.minuteOfHour().get());
                jgen.writeNumber(dt.secondOfMinute().get());
                jgen.writeNumber(dt.millisOfSecond().get());
                jgen.writeEndArray();
            } else {
                jgen.writeString(printLocalDateTime(dt));
            }
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, java.lang.reflect.Type typeHint)
        {
            return createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)
                    ? "array" : "string", true);
        }
    }

    public final static class LocalDateSerializer
        extends JodaSerializer<LocalDate>
    {
        public LocalDateSerializer() { super(LocalDate.class); }
    
        @Override
        public void serialize(LocalDate dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                // Timestamp here actually means an array of values
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeEndArray();
            } else {
                jgen.writeString(printLocalDate(dt));
            }
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, java.lang.reflect.Type typeHint)
        {
            return createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)
                    ? "array" : "string", true);
        }
    }

    public final static class DateMidnightSerializer
        extends JodaSerializer<DateMidnight>
    {
        public DateMidnightSerializer() { super(DateMidnight.class); }
    
        @Override
        public void serialize(DateMidnight dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                // same as with other date-only values
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeEndArray();
            } else {
                jgen.writeString(printLocalDate(dt));
            }
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, java.lang.reflect.Type typeHint)
        {
            return createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)
                    ? "array" : "string", true);
        }
    }
    
}
