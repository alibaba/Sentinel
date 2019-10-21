package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ArrayBuilders;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ObjectBuffer;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Container for deserializers used for instantiating "primitive arrays",
 * arrays that contain non-object java primitive types.
 * 
 * @since 1.9 (renamed from 'com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ArrayDeserilizers)
 */
public class PrimitiveArrayDeserializers
{
    HashMap<JavaType,JsonDeserializer<Object>> _allDeserializers;

    final static PrimitiveArrayDeserializers instance = new PrimitiveArrayDeserializers();

    protected PrimitiveArrayDeserializers()
    {
        _allDeserializers = new HashMap<JavaType,JsonDeserializer<Object>>();
        // note: we'll use component type as key, not array type
        add(boolean.class, new BooleanDeser());

        /* ByteDeser is bit special, as it has 2 separate modes of operation;
         * one for String input (-> base64 input), the other for
         * numeric input
         */
        add(byte.class, new ByteDeser());
        add(short.class, new ShortDeser());
        add(int.class, new IntDeser());
        add(long.class, new LongDeser());

        add(float.class, new FloatDeser());
        add(double.class, new DoubleDeser());

        add(String.class, new StringDeser());
        /* also: char[] is most likely only used with Strings; doesn't
         * seem to make sense to transfer as numbers
         */
        add(char.class, new CharDeser());
    }

    public static HashMap<JavaType,JsonDeserializer<Object>> getAll()
    {
        return instance._allDeserializers;
    }

    @SuppressWarnings("unchecked")
    private void add(Class<?> cls, JsonDeserializer<?> deser)
    {
        /* Not super clean to use default TypeFactory in general, but
         * since primitive array types can't be modified for anything
         * useful, this should be ok:
         */
        _allDeserializers.put(TypeFactory.defaultInstance().constructType(cls),
                (JsonDeserializer<Object>) deser);
    }

    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        /* Should there be separate handling for base64 stuff?
         * for now this should be enough:
         */
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }

    /*
    /********************************************************
    /* Intermediate base class
    /********************************************************
     */
    
    /**
     * Intermediate base class for primitive array deserializers
     */
    static abstract class Base<T> extends StdDeserializer<T>
    {
        protected Base(Class<T> cls) {
            super(cls);
        }

        @Override
        public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
        }
    }
    
    /*
    /********************************************************
    /* Actual deserializers: efficient String[], char[] deserializers
    /********************************************************
    */

    @JacksonStdImpl
    final static class StringDeser
        extends Base<String[]>
    {
        public StringDeser() { super(String[].class); }

        @Override
        public String[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // Ok: must point to START_ARRAY (or equivalent)
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
            Object[] chunk = buffer.resetAndStart();
            int ix = 0;
            JsonToken t;
            
            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Ok: no need to convert Strings, but must recognize nulls
                String value = (t == JsonToken.VALUE_NULL) ? null : jp.getText();
                if (ix >= chunk.length) {
                    chunk = buffer.appendCompletedChunk(chunk);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
            ctxt.returnObjectBuffer(buffer);
            return result;
        }
    
        private final String[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-526]: implicit arrays from single values?
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                // [JACKSON-620] Empty String can become null...
                if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                        && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                    String str = jp.getText();
                    if (str.length() == 0) {
                        return null;
                    }
                }
                throw ctxt.mappingException(_valueClass);
            }
            return new String[] { (jp.getCurrentToken() == JsonToken.VALUE_NULL) ? null : jp.getText() };
        }
    }
    
    @JacksonStdImpl
    final static class CharDeser
        extends Base<char[]>
    {
        public CharDeser() { super(char[].class); }

        @Override
        public char[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            /* Won't take arrays, must get a String (could also
             * convert other tokens to Strings... but let's not bother
             * yet, doesn't seem to make sense)
             */
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_STRING) {
                // note: can NOT return shared internal buffer, must copy:
                char[] buffer = jp.getTextCharacters();
                int offset = jp.getTextOffset();
                int len = jp.getTextLength();
    
                char[] result = new char[len];
                System.arraycopy(buffer, offset, result, 0, len);
                return result;
            }
            if (jp.isExpectedStartArrayToken()) {
                // Let's actually build as a String, then get chars
                StringBuilder sb = new StringBuilder(64);
                while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                    if (t != JsonToken.VALUE_STRING) {
                        throw ctxt.mappingException(Character.TYPE);
                    }
                    String str = jp.getText();
                    if (str.length() != 1) {
                        throw JsonMappingException.from(jp, "Can not convert a JSON String of length "+str.length()+" into a char element of char array");
                    }
                    sb.append(str.charAt(0));
                }
                return sb.toString().toCharArray();
            }
            // or, maybe an embedded object?
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                Object ob = jp.getEmbeddedObject();
                if (ob == null) return null;
                if (ob instanceof char[]) {
                    return (char[]) ob;
                }
                if (ob instanceof String) {
                    return ((String) ob).toCharArray();
                }
                // 04-Feb-2011, tatu: byte[] can be converted; assuming base64 is wanted
                if (ob instanceof byte[]) {
                    return Base64Variants.getDefaultVariant().encode((byte[]) ob, false).toCharArray();
                }
                // not recognized, just fall through
            }
            throw ctxt.mappingException(_valueClass);
        }
    }

    /*
    /********************************************************
    /* Actual deserializers: primivate array desers
    /********************************************************
    */

    @JacksonStdImpl
    final static class BooleanDeser
        extends Base<boolean[]>
    {
        public BooleanDeser() { super(boolean[].class); }

        @Override
        public boolean[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.BooleanBuilder builder = ctxt.getArrayBuilders().getBooleanBuilder();
            boolean[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                // whether we should allow truncating conversions?
                boolean value = _parseBooleanPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final boolean[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new boolean[] { _parseBooleanPrimitive(jp, ctxt) };
        }
    }

    /**
     * When dealing with byte arrays we have one more alternative (compared
     * to int/long/shorts): base64 encoded data.
     */
    @JacksonStdImpl
    final static class ByteDeser
        extends Base<byte[]>
    {
        public ByteDeser() { super(byte[].class); }

        @Override
        public byte[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            
            // Most likely case: base64 encoded String?
            if (t == JsonToken.VALUE_STRING) {
                return jp.getBinaryValue(ctxt.getBase64Variant());
            }
            // 31-Dec-2009, tatu: Also may be hidden as embedded Object
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                Object ob = jp.getEmbeddedObject();
                if (ob == null) return null;
                if (ob instanceof byte[]) {
                    return (byte[]) ob;
                }
            }
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.ByteBuilder builder = ctxt.getArrayBuilders().getByteBuilder();
            byte[] chunk = builder.resetAndStart();
            int ix = 0;

            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // whether we should allow truncating conversions?
                byte value;
                if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                    // should we catch overflow exceptions?
                    value = jp.getByteValue();
                } else {
                    // [JACKSON-79]: should probably accept nulls as 0
                    if (t != JsonToken.VALUE_NULL) {
                        throw ctxt.mappingException(_valueClass.getComponentType());
                    }
                    value = (byte) 0;
                }
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final byte[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            byte value;
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                // should we catch overflow exceptions?
                value = jp.getByteValue();
            } else {
                // [JACKSON-79]: should probably accept nulls as 'false'
                if (t != JsonToken.VALUE_NULL) {
                    throw ctxt.mappingException(_valueClass.getComponentType());
                }
                value = (byte) 0;
            }
            return new byte[] { value };
        }
    }

    @JacksonStdImpl
    final static class ShortDeser
        extends Base<short[]>
    {
        public ShortDeser() { super(short[].class); }

        @Override
        public short[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.ShortBuilder builder = ctxt.getArrayBuilders().getShortBuilder();
            short[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                short value = _parseShortPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final short[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new short[] { _parseShortPrimitive(jp, ctxt) };
        }
    }

    @JacksonStdImpl
    final static class IntDeser
        extends Base<int[]>
    {
        public IntDeser() { super(int[].class); }

        @Override
        public int[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.IntBuilder builder = ctxt.getArrayBuilders().getIntBuilder();
            int[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                // whether we should allow truncating conversions?
                int value = _parseIntPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final int[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new int[] { _parseIntPrimitive(jp, ctxt) };
        }
    }

    @JacksonStdImpl
    final static class LongDeser
        extends Base<long[]>
    {
        public LongDeser() { super(long[].class); }

        @Override
        public long[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.LongBuilder builder = ctxt.getArrayBuilders().getLongBuilder();
            long[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                long value = _parseLongPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final long[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new long[] { _parseLongPrimitive(jp, ctxt) };
        }
    }

    @JacksonStdImpl
    final static class FloatDeser
        extends Base<float[]>
    {
        public FloatDeser() { super(float[].class); }

        @Override
        public float[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.FloatBuilder builder = ctxt.getArrayBuilders().getFloatBuilder();
            float[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                // whether we should allow truncating conversions?
                float value = _parseFloatPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final float[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new float[] { _parseFloatPrimitive(jp, ctxt) };
        }
    }

    @JacksonStdImpl
    final static class DoubleDeser
        extends Base<double[]>
    {
        public DoubleDeser() { super(double[].class); }

        @Override
        public double[] deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            if (!jp.isExpectedStartArrayToken()) {
                return handleNonArray(jp, ctxt);
            }
            ArrayBuilders.DoubleBuilder builder = ctxt.getArrayBuilders().getDoubleBuilder();
            double[] chunk = builder.resetAndStart();
            int ix = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                double value = _parseDoublePrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        private final double[] handleNonArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            // [JACKSON-620] Empty String can become null...
            if ((jp.getCurrentToken() == JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                if (jp.getText().length() == 0) {
                    return null;
                }
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(_valueClass);
            }
            return new double[] { _parseDoublePrimitive(jp, ctxt) };
        }
    }
}
