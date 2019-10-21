package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.node.ObjectNode;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;

/**
 * Dummy container class to group standard homogenous array serializer implementations
 * (primitive arrays and String array).
 */
public class StdArraySerializers
{
    protected StdArraySerializers() { }

    /*
    /**********************************************************
    /* Base classes
    /**********************************************************
     */
    
    /**
     * Base class for serializers that will output contents as JSON
     * arrays.
     */
    public abstract static class ArraySerializerBase<T>
        extends ContainerSerializerBase<T>
    {
         /**
         * Type serializer used for values, if any.
         */
        protected final TypeSerializer _valueTypeSerializer;

        /**
         * Array-valued property being serialized with this instance
         * 
         * @since 1.7
         */
        protected final BeanProperty _property;
        
        protected ArraySerializerBase(Class<T> cls, TypeSerializer vts, BeanProperty property)
        {
            super(cls);
            _valueTypeSerializer = vts;
            _property = property;
        }
        
        @Override
        public final void serialize(T value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeStartArray();
            serializeContents(value, jgen, provider);
            jgen.writeEndArray();
        }
        
        @Override
        public final void serializeWithType(T value, JsonGenerator jgen, SerializerProvider provider,
                TypeSerializer typeSer)
            throws IOException, JsonGenerationException
        {
            typeSer.writeTypePrefixForArray(value, jgen);
            serializeContents(value, jgen, provider);
            typeSer.writeTypeSuffixForArray(value, jgen);
        }

        protected abstract void serializeContents(T value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException;
    }

    /*
     ****************************************************************
    /* Concrete serializers, arrays
     ****************************************************************
     */

    /**
     * Standard serializer used for <code>String[]</code> values.
     */
    @JacksonStdImpl
    public final static class StringArraySerializer
        extends ArraySerializerBase<String[]>
        implements ResolvableSerializer
    {
        /**
         * Value serializer to use, if it's not the standard one
         * (if it is we can optimize serialization a lot)
         * 
         * @since 1.7
         */
        protected JsonSerializer<Object> _elementSerializer;

        public StringArraySerializer(BeanProperty prop) {
            super(String[].class, null, prop);
        }

        /**
         * Strings never add type info; hence, even if type serializer is suggested,
         * we'll ignore it...
         */
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return this;
        }
        
        @Override
        public void serializeContents(String[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            final int len = value.length;
            if (len == 0) {
                return;
            }
            if (_elementSerializer != null) {
                serializeContentsSlow(value, jgen, provider, _elementSerializer);
                return;
            }
            /* 08-Dec-2008, tatus: If we want this to be fully overridable
             *  (for example, to support String cleanup during writing
             *  or something), we should find serializer  by provider.
             *  But for now, that seems like an overkill: and caller can
             *  add custom serializer if that is needed as well.
             * (ditto for null values)
             */
            //JsonSerializer<String> ser = (JsonSerializer<String>)provider.findValueSerializer(String.class);
            for (int i = 0; i < len; ++i) {
                String str = value[i];
                if (str == null) {
                    jgen.writeNull();
                } else {
                    //ser.serialize(value[i], jgen, provider);
                    jgen.writeString(value[i]);
                }
            }
        }

        private void serializeContentsSlow(String[] value, JsonGenerator jgen, SerializerProvider provider,
                JsonSerializer<Object> ser)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                String str = value[i];
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                } else {
                    ser.serialize(value[i], jgen, provider);
                }
            }
        }

        /**
         * Need to get callback to resolve value serializer, which may
         * be overridden by custom serializer
         */
        @Override
        public void resolve(SerializerProvider provider)
            throws JsonMappingException
        {
            JsonSerializer<Object> ser = provider.findValueSerializer(String.class, _property);
            // Retain if not the standard implementation
            if (ser != null && ser.getClass().getAnnotation(JacksonStdImpl.class) == null) {
                _elementSerializer = ser;
            }
        }        
        
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("string"));
            return o;
        }
    }

    @JacksonStdImpl
    public final static class BooleanArraySerializer
        extends ArraySerializerBase<boolean[]>
    {
        public BooleanArraySerializer() { super(boolean[].class, null, null); }

        /**
         * Booleans never add type info; hence, even if type serializer is suggested,
         * we'll ignore it...
         */
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts)
        {
            return this;
        }
        
        @Override
        public void serializeContents(boolean[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeBoolean(value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("boolean"));
            return o;
        }
    }

    /**
     * Unlike other integral number array serializers, we do not just print out byte values
     * as numbers. Instead, we assume that it would make more sense to output content
     * as base64 encoded bytes (using default base64 encoding).
     *<p>
     * NOTE: since it is NOT serialized as an array, can not use AsArraySerializer as base
     */
    @JacksonStdImpl
    public final static class ByteArraySerializer
        extends SerializerBase<byte[]>
    {
        public ByteArraySerializer() {
            super(byte[].class);
        }
        
        @Override
        public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            jgen.writeBinary(value);
        }

        @Override
        public void serializeWithType(byte[] value, JsonGenerator jgen, SerializerProvider provider,
                TypeSerializer typeSer)
            throws IOException, JsonGenerationException
        {
            typeSer.writeTypePrefixForScalar(value, jgen);
            jgen.writeBinary(value);
            typeSer.writeTypeSuffixForScalar(value, jgen);
        }
        
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            ObjectNode itemSchema = createSchemaNode("string"); //binary values written as strings?
            o.put("items", itemSchema);
            return o;
        }
    }

    @JacksonStdImpl
    public final static class ShortArraySerializer
        extends ArraySerializerBase<short[]>
    {
        public ShortArraySerializer() { this(null); }
        public ShortArraySerializer(TypeSerializer vts) { super(short[].class, vts, null); }

        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return new ShortArraySerializer(vts);
        }
        
        @SuppressWarnings("cast")
        @Override
        public void serializeContents(short[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber((int)value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            //no "short" type defined by json
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("integer"));
            return o;
        }
    }

    /**
     * Character arrays are different from other integral number arrays in that
     * they are most likely to be textual data, and should be written as
     * Strings, not arrays of entries.
     *<p>
     * NOTE: since it is NOT serialized as an array, can not use AsArraySerializer as base
     */
    @JacksonStdImpl
    public final static class CharArraySerializer
        extends SerializerBase<char[]>
    {
        public CharArraySerializer() { super(char[].class); }
        
        @Override
        public void serialize(char[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            // [JACKSON-289] allows serializing as 'sparse' char array too:
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                jgen.writeStartArray();
                _writeArrayContents(jgen, value);
                jgen.writeEndArray();
            } else {
                jgen.writeString(value, 0, value.length);
            }
        }

        @Override
        public void serializeWithType(char[] value, JsonGenerator jgen, SerializerProvider provider,
                TypeSerializer typeSer)
            throws IOException, JsonGenerationException
        {
            // [JACKSON-289] allows serializing as 'sparse' char array too:
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                typeSer.writeTypePrefixForArray(value, jgen);
                _writeArrayContents(jgen, value);
                typeSer.writeTypeSuffixForArray(value, jgen);
            } else { // default is to write as simple String
                typeSer.writeTypePrefixForScalar(value, jgen);
                jgen.writeString(value, 0, value.length);
                typeSer.writeTypeSuffixForScalar(value, jgen);
            }
        }

        private final void _writeArrayContents(JsonGenerator jgen, char[] value)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeString(value, i, 1);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            ObjectNode itemSchema = createSchemaNode("string");
            itemSchema.put("type", "string");
            o.put("items", itemSchema);
            return o;
        }
    }

    @JacksonStdImpl
    public final static class IntArraySerializer
        extends ArraySerializerBase<int[]>
    {
        public IntArraySerializer() { super(int[].class, null, null); }

        /**
         * Ints never add type info; hence, even if type serializer is suggested,
         * we'll ignore it...
         */
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts)
        {
            return this;
        }        
        
        @Override
        public void serializeContents(int[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("integer"));
            return o;
        }
    }

    @JacksonStdImpl
    public final static class LongArraySerializer
        extends ArraySerializerBase<long[]>
    {
        public LongArraySerializer() { this(null); }
        public LongArraySerializer(TypeSerializer vts) { super(long[].class, vts, null); }

        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return new LongArraySerializer(vts);
        }
        
        @Override
        public void serializeContents(long[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("number", true));
            return o;
        }
    }

    @JacksonStdImpl
    public final static class FloatArraySerializer
        extends ArraySerializerBase<float[]>
    {
        public FloatArraySerializer() { this(null); }
        public FloatArraySerializer(TypeSerializer vts) { super(float[].class, vts, null); }

        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
            return new FloatArraySerializer(vts);
        }
        
        @Override
        public void serializeContents(float[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("number"));
            return o;
        }
    }

    @JacksonStdImpl
    public final static class DoubleArraySerializer
        extends ArraySerializerBase<double[]>
    {
        public DoubleArraySerializer() { super(double[].class, null, null); }

        /**
         * Doubles never add type info; hence, even if type serializer is suggested,
         * we'll ignore it...
         */
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts)
        {
            return this;
        }
        
        @Override
        public void serializeContents(double[] value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException
        {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        {
            ObjectNode o = createSchemaNode("array", true);
            o.put("items", createSchemaNode("number"));
            return o;
        }
    }
}
