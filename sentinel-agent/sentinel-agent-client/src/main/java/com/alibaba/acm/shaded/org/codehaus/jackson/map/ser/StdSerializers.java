package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerationException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.NonTypedScalarSerializerBase;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.ScalarSerializerBase;

/**
 * Container class for serializers used for handling standard JDK-provided types.
 * 
 * @since 1.5
 */
public class StdSerializers
{
    protected StdSerializers() { }
    
    /*
    /**********************************************************
    /* Concrete serializers, non-numeric primitives, Strings, Classes
    /**********************************************************
     */
    
    /**
     * Serializer used for primitive boolean, as well as java.util.Boolean
     * wrapper type.
     *<p>
     * Since this is one of "native" types, no type information is ever
     * included on serialization (unlike for most scalar types as of 1.5)
     */
    @JacksonStdImpl
    public final static class BooleanSerializer
        extends NonTypedScalarSerializerBase<Boolean>
    {
        /**
         * Whether type serialized is primitive (boolean) or wrapper
         * (java.lang.Boolean); if true, former, if false, latter.
         */
        final boolean _forPrimitive;
    
        public BooleanSerializer(boolean forPrimitive)
        {
            super(Boolean.class);
            _forPrimitive = forPrimitive;
        }
    
        @Override
        public void serialize(Boolean value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeBoolean(value.booleanValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            /*(ryan) it may not, in fact, be optional, but there's no way
             * to tell whether we're referencing a boolean or java.lang.Boolean.
             */
            /* 27-Jun-2009, tatu: Now we can tell, after passing
             *   'forPrimitive' flag...
             */
            return createSchemaNode("boolean", !_forPrimitive);
        }
    }

    /**
     * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.StringSerializer} instead
     */
    @Deprecated
    @JacksonStdImpl
    public final static class StringSerializer
        extends NonTypedScalarSerializerBase<String>
    {
        public StringSerializer() { super(String.class); }

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeString(value);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("string", true);
        }
    }

    /*
    /**********************************************************
    /* Concrete serializers, numerics
    /**********************************************************
     */

    /**
     * This is the special serializer for regular {@link java.lang.Integer}s
     * (and primitive ints)
     *<p>
     * Since this is one of "native" types, no type information is ever
     * included on serialization (unlike for most scalar types as of 1.5)
     */
    @JacksonStdImpl
    public final static class IntegerSerializer
        extends NonTypedScalarSerializerBase<Integer>
    {
        public IntegerSerializer() { super(Integer.class); }
    
        @Override
        public void serialize(Integer value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.intValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("integer", true);
        }
    }

    /**
     * Similar to {@link IntegerSerializer}, but will not cast to Integer:
     * instead, cast is to {@link java.lang.Number}, and conversion is
     * by calling {@link java.lang.Number#intValue}.
     */
    @JacksonStdImpl
    public final static class IntLikeSerializer
        extends ScalarSerializerBase<Number>
    {
        final static IntLikeSerializer instance = new IntLikeSerializer();
    
        public IntLikeSerializer() { super(Number.class); }
        
        @Override
        public void serialize(Number value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.intValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("integer", true);
        }
    }

    @JacksonStdImpl
    public final static class LongSerializer
        extends ScalarSerializerBase<Long>
    {
        final static LongSerializer instance = new LongSerializer();
    
        public LongSerializer() { super(Long.class); }
        
        @Override
        public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.longValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("number", true);
        }
    }
    
    @JacksonStdImpl
    public final static class FloatSerializer
        extends ScalarSerializerBase<Float>
    {
        final static FloatSerializer instance = new FloatSerializer();
    
        public FloatSerializer() { super(Float.class); }
        
        @Override
        public void serialize(Float value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.floatValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("number", true);
        }
    }

    /**
     * This is the special serializer for regular {@link java.lang.Double}s
     * (and primitive doubles)
     *<p>
     * Since this is one of "native" types, no type information is ever
     * included on serialization (unlike for most scalar types as of 1.5)
     */
    @JacksonStdImpl
    public final static class DoubleSerializer
        extends NonTypedScalarSerializerBase<Double>
    {
        final static DoubleSerializer instance = new DoubleSerializer();
    
        public DoubleSerializer() { super(Double.class); }
    
        @Override
        public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeNumber(value.doubleValue());
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("number", true);
        }
    }
    
    /**
     * As a fallback, we may need to use this serializer for other
     * types of {@link Number}s (custom types).
     */
    @JacksonStdImpl
    public final static class NumberSerializer
        extends ScalarSerializerBase<Number>
    {
        public final static NumberSerializer instance = new NumberSerializer();
    
        public NumberSerializer() { super(Number.class); }
    
        @Override
        public void serialize(Number value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            // As per [JACKSON-423], handling for BigInteger and BigDecimal was missing!
            if (value instanceof BigDecimal) {
                jgen.writeNumber((BigDecimal) value);
            } else if (value instanceof BigInteger) {
                jgen.writeNumber((BigInteger) value);
                
            /* These shouldn't match (as there are more specific ones),
             * but just to be sure:
             */
            } else if (value instanceof Integer) {
                jgen.writeNumber(value.intValue());
            } else if (value instanceof Long) {
                jgen.writeNumber(value.longValue());
            } else if (value instanceof Double) {
                jgen.writeNumber(value.doubleValue());
            } else if (value instanceof Float) {
                jgen.writeNumber(value.floatValue());
            } else if ((value instanceof Byte) || (value instanceof Short)) {
                jgen.writeNumber(value.intValue()); // doesn't need to be cast to smaller numbers
            } else {
                // We'll have to use fallback "untyped" number write method
                jgen.writeNumber(value.toString());
            }
        }
    
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("number", true);
        }
    }

    /*
    /**********************************************************
    /* Serializers for JDK date/time data types
    /**********************************************************
     */

    /**
     * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer} instead
     */
    @JacksonStdImpl
    @Deprecated
    public final static class CalendarSerializer
        extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.CalendarSerializer { }

    /**
     * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer} instead
     */
    @Deprecated
    @JacksonStdImpl
    public final static class UtilDateSerializer
        extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer {
        
    }

    /**
     * Compared to regular {@link UtilDateSerializer}, we do use String
     * representation here. Why? Basically to truncate of time part, since
     * that should not be used by plain SQL date.
     */
    @JacksonStdImpl
    public final static class SqlDateSerializer
        extends ScalarSerializerBase<java.sql.Date>
    {
        public SqlDateSerializer() { super(java.sql.Date.class); }

        @Override
        public void serialize(java.sql.Date value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeString(value.toString());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            //todo: (ryan) add a format for the date in the schema?
            return createSchemaNode("string", true);
        }
    }

    @JacksonStdImpl
    public final static class SqlTimeSerializer
        extends ScalarSerializerBase<java.sql.Time>
    {
        public SqlTimeSerializer() { super(java.sql.Time.class); }

        @Override
        public void serialize(java.sql.Time value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeString(value.toString());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            return createSchemaNode("string", true);
        }
    }

    
    /*
    /**********************************************************
    / Other serializers
    /**********************************************************
     */

    /**
     * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer} instead
     */
    @Deprecated
    @JacksonStdImpl
    public final static class SerializableSerializer
        extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializableSerializer { }

    /**
     * @deprecated Since 1.9, use {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.DateSerializer} instead
     */
    @Deprecated
    @JacksonStdImpl
    public final static class SerializableWithTypeSerializer
        extends com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std.SerializableWithTypeSerializer {
    }
}
