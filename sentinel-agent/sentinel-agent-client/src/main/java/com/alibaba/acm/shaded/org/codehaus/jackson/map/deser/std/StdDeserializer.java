package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser.NumberType;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonProcessingException;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonToken;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.NumberInput;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate.JacksonStdImpl;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Base class for common deserializers. Contains shared
 * base functionality for dealing with primitive values, such
 * as (re)parsing from String.
 * 
 * @since 1.9 (moved from higher-level package)
 */
public abstract class StdDeserializer<T>
    extends JsonDeserializer<T>
{
    /**
     * Type of values this deserializer handles: sometimes
     * exact types, other time most specific supertype of
     * types deserializer handles (which may be as generic
     * as {@link Object} in some case)
     */
    final protected Class<?> _valueClass;

    protected StdDeserializer(Class<?> vc) {
        _valueClass = vc;
    }

    protected StdDeserializer(JavaType valueType) {
        _valueClass = (valueType == null) ? null : valueType.getRawClass();
    }
    
    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public Class<?> getValueClass() { return _valueClass; }

    /**
     * Exact structured type deserializer handles, if known.
     *<p>
     * Default implementation just returns null.
     */
    public JavaType getValueType() { return null; }

    /**
     * Method that can be called to determine if given deserializer is the default
     * deserializer Jackson uses; as opposed to a custom deserializer installed by
     * a module or calling application. Determination is done using
     * {@link JacksonStdImpl} annotation on deserializer class.
     * 
     * @since 1.7
     */
    protected boolean isDefaultSerializer(JsonDeserializer<?> deserializer)
    {
        return (deserializer != null && deserializer.getClass().getAnnotation(JacksonStdImpl.class) != null);
    }
    
    /*
    /**********************************************************
    /* Partial JsonDeserializer implementation 
    /**********************************************************
     */
    
    /**
     * Base implementation that does not assume specific type
     * inclusion mechanism. Sub-classes are expected to override
     * this method if they are to handle type information.
     */
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }
    
    /*
    /**********************************************************
    /* Helper methods for sub-classes, parsing
    /**********************************************************
     */

    protected final boolean _parseBooleanPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return false;
        }
        if (t == JsonToken.VALUE_NULL) {
            return false;
        }
        // [JACKSON-78]: should accept ints too, (0 == false, otherwise true)
        if (t == JsonToken.VALUE_NUMBER_INT) {
            // 11-Jan-2012, tatus: May be outside of int...
            if (jp.getNumberType() == NumberType.INT) {
                return (jp.getIntValue() != 0);
            }
            return _parseBooleanFromNumber(jp, ctxt);
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("true".equals(text)) {
                return true;
            }
            if ("false".equals(text) || text.length() == 0) {
                return Boolean.FALSE;
            }
            throw ctxt.weirdStringException(_valueClass, "only \"true\" or \"false\" recognized");
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Boolean _parseBoolean(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        // [JACKSON-78]: should accept ints too, (0 == false, otherwise true)
        if (t == JsonToken.VALUE_NUMBER_INT) {
            // 11-Jan-2012, tatus: May be outside of int...
            if (jp.getNumberType() == NumberType.INT) {
                return (jp.getIntValue() == 0) ? Boolean.FALSE : Boolean.TRUE;
            }
            return Boolean.valueOf(_parseBooleanFromNumber(jp, ctxt));
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Boolean) getNullValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("true".equals(text)) {
                return Boolean.TRUE;
            }
            if ("false".equals(text)) {
                return Boolean.FALSE;
            }
            if (text.length() == 0) {
                return (Boolean) getEmptyValue();
            }
            throw ctxt.weirdStringException(_valueClass, "only \"true\" or \"false\" recognized");
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final boolean _parseBooleanFromNumber(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        if (jp.getNumberType() == NumberType.LONG) {
            return (jp.getLongValue() == 0L) ? Boolean.FALSE : Boolean.TRUE;
        }
        // no really good logic; let's actually resort to textual comparison
        String str = jp.getText();
        if ("0.0".equals(str) || "0".equals(str)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    protected Byte _parseByte(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getByteValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            int value;
            try {
                int len = text.length();
                if (len == 0) {
                    return (Byte) getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Byte value");
            }
            // So far so good: but does it fit?
	    // as per [JACKSON-804], allow range up to 255, inclusive
            if (value < Byte.MIN_VALUE || value > 255) {
                throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 8-bit value");
            }
            return Byte.valueOf((byte) value);
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Byte) getNullValue();
        }
        throw ctxt.mappingException(_valueClass, t);
    }
    
    protected Short _parseShort(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getShortValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            int value;
            try {
                int len = text.length();
                if (len == 0) {
                    return (Short) getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Short value");
            }
            // So far so good: but does it fit?
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 16-bit value");
            }
            return Short.valueOf((short) value);
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Short) getNullValue();
        }
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final short _parseShortPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        int value = _parseIntPrimitive(jp, ctxt);
        // So far so good: but does it fit?
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw ctxt.weirdStringException(_valueClass, "overflow, value can not be represented as 16-bit value");
        }
        return (short) value;
    }
    
    protected final int _parseIntPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();

        // Int works as is, coercing fine as well
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getIntValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            /* 31-Dec-2009, tatus: Should improve handling of overflow
             *   values... but this'll have to do for now
             */
            String text = jp.getText().trim();
            try {
                int len = text.length();
                if (len > 9) {
                    long l = Long.parseLong(text);
                    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                        throw ctxt.weirdStringException(_valueClass,
                            "Overflow: numeric value ("+text+") out of range of int ("+Integer.MIN_VALUE+" - "+Integer.MAX_VALUE+")");
                    }
                    return (int) l;
                }
                if (len == 0) {
                    return 0;
                }
                return NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid int value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0;
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Integer _parseInteger(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return Integer.valueOf(jp.getIntValue());
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = jp.getText().trim();
            try {
                int len = text.length();
                if (len > 9) {
                    long l = Long.parseLong(text);
                    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                        throw ctxt.weirdStringException(_valueClass,
                            "Overflow: numeric value ("+text+") out of range of Integer ("+Integer.MIN_VALUE+" - "+Integer.MAX_VALUE+")");
                    }
                    return Integer.valueOf((int) l);
                }
                if (len == 0) {
                    return (Integer) getEmptyValue();
                }
                return Integer.valueOf(NumberInput.parseInt(text));
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid Integer value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Integer) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Long _parseLong(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
    
        // it should be ok to coerce (although may fail, too)
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        // let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            // !!! 05-Jan-2009, tatu: Should we try to limit value space, JDK is too lenient?
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Long) getEmptyValue();
            }
            try {
                return Long.valueOf(NumberInput.parseLong(text));
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Long value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Long) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final long _parseLongPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0L;
            }
            try {
                return NumberInput.parseLong(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid long value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0L;
        }
        throw ctxt.mappingException(_valueClass, t);
    }
    
    protected final Float _parseFloat(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // We accept couple of different types; obvious ones first:
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getFloatValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Float) getEmptyValue();
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Float.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Float.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Float.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return Float.parseFloat(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Float value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Float) getNullValue();
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final float _parseFloatPrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getFloatValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0f;
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Float.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Float.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Float.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return Float.parseFloat(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid float value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0f;
        }
        // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final Double _parseDouble(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getDoubleValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return (Double) getEmptyValue();
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Double.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Double.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Double.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return parseDouble(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid Double value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return (Double) getNullValue();
        }
            // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    protected final double _parseDoublePrimitive(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // We accept couple of different types; obvious ones first:
        JsonToken t = jp.getCurrentToken();
        
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return jp.getDoubleValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0;
            }
            switch (text.charAt(0)) {
            case 'I':
                if ("Infinity".equals(text) || "INF".equals(text)) {
                    return Double.POSITIVE_INFINITY;
                }
                break;
            case 'N':
                if ("NaN".equals(text)) {
                    return Double.NaN;
                }
                break;
            case '-':
                if ("-Infinity".equals(text) || "-INF".equals(text)) {
                    return Double.NEGATIVE_INFINITY;
                }
                break;
            }
            try {
                return parseDouble(text);
            } catch (IllegalArgumentException iae) { }
            throw ctxt.weirdStringException(_valueClass, "not a valid double value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0;
        }
            // Otherwise, no can do:
        throw ctxt.mappingException(_valueClass, t);
    }

    
    protected java.util.Date _parseDate(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new java.util.Date(jp.getLongValue());
        }
        if (t == JsonToken.VALUE_NULL) {
            return (java.util.Date) getNullValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                /* As per [JACKSON-203], take empty Strings to mean
                 * null
                 */
                String str = jp.getText().trim();
                if (str.length() == 0) {
                    return (Date) getEmptyValue();
                }
                return ctxt.parseDate(str);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid representation (error: "+iae.getMessage()+")");
            }
        }
        throw ctxt.mappingException(_valueClass, t);
    }

    /**
     * Helper method for encapsulating calls to low-level double value parsing; single place
     * just because we need a work-around that must be applied to all calls.
     *<p>
     * Note: copied from <code>com.alibaba.acm.shaded.org.codehaus.jackson.io.NumberUtil</code> (to avoid dependency to
     * version 1.8; except for String constants, but that gets compiled in bytecode here)
     */
    protected final static double parseDouble(String numStr) throws NumberFormatException
    {
        // [JACKSON-486]: avoid some nasty float representations... but should it be MIN_NORMAL or MIN_VALUE?
        if (NumberInput.NASTY_SMALL_DOUBLE.equals(numStr)) {
            return Double.MIN_NORMAL;
        }
        return Double.parseDouble(numStr);
    }
    
    /*
    /****************************************************
    /* Helper methods for sub-classes, resolving dependencies
    /****************************************************
    */

    /**
     * Helper method used to locate deserializers for properties the
     * type this deserializer handles contains (usually for properties of
     * bean types)
     * 
     * @param config Active deserialization configuration 
     * @param provider Deserializer provider to use for actually finding deserializer(s)
     * @param type Type of property to deserialize
     * @param property Actual property object (field, method, constuctor parameter) used
     *     for passing deserialized values; provided so deserializer can be contextualized if necessary (since 1.7)
     */
    protected JsonDeserializer<Object> findDeserializer(DeserializationConfig config, DeserializerProvider provider,
                                                        JavaType type, BeanProperty property)
        throws JsonMappingException
    {
        JsonDeserializer<Object> deser = provider.findValueDeserializer(config, type, property);
        return deser;
    }

    /*
    /**********************************************************
    /* Helper methods for sub-classes, problem reporting
    /**********************************************************
     */

    /**
     * Method called to deal with a property that did not map to a known
     * Bean property. Method can deal with the problem as it sees fit (ignore,
     * throw exception); but if it does return, it has to skip the matching
     * Json content parser has.
     *<p>
     * NOTE: method signature was changed in version 1.5; explicit JsonParser
     * <b>must</b> be passed since it may be something other than what
     * context has. Prior versions did not include the first parameter.
     *
     * @param jp Parser that points to value of the unknown property
     * @param ctxt Context for deserialization; allows access to the parser,
     *    error reporting functionality
     * @param instanceOrClass Instance that is being populated by this
     *   deserializer, or if not known, Class that would be instantiated.
     *   If null, will assume type is what {@link #getValueClass} returns.
     * @param propName Name of the property that can not be mapped
     */
    protected void handleUnknownProperty(JsonParser jp, DeserializationContext ctxt, Object instanceOrClass, String propName)
        throws IOException, JsonProcessingException
    {
        if (instanceOrClass == null) {
            instanceOrClass = getValueClass();
        }
        // Maybe we have configured handler(s) to take care of it?
        if (ctxt.handleUnknownProperty(jp, this, instanceOrClass, propName)) {
            return;
        }
        // Nope, not handled. Potentially that's a problem...
        reportUnknownProperty(ctxt, instanceOrClass, propName);

        /* If we get this far, need to skip now; we point to first token of
         * value (START_xxx for structured, or the value token for others)
         */
        jp.skipChildren();
    }
        
    protected void reportUnknownProperty(DeserializationContext ctxt,
                                         Object instanceOrClass, String fieldName)
        throws IOException, JsonProcessingException
    {
        // throw exception if that's what we are expected to do
        if (ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            throw ctxt.unknownFieldException(instanceOrClass, fieldName);
        }
        // ... or if not, just ignore
    }


    /*
    /**********************************************************
    /* Then one intermediate base class for things that have
    /* both primitive and wrapper types
    /**********************************************************
     */

    protected abstract static class PrimitiveOrWrapperDeserializer<T>
        extends StdScalarDeserializer<T>
    {
        final T _nullValue;
        
        protected PrimitiveOrWrapperDeserializer(Class<T> vc, T nvl)
        {
            super(vc);
            _nullValue = nvl;
        }
        
        @Override
        public final T getNullValue() {
            return _nullValue;
        }
    }
    
    /*
    /**********************************************************
    /* Then primitive/wrapper types
    /**********************************************************
     */

    @JacksonStdImpl
    public final static class BooleanDeserializer
        extends PrimitiveOrWrapperDeserializer<Boolean>
    {
        public BooleanDeserializer(Class<Boolean> cls, Boolean nvl)
        {
            super(cls, nvl);
        }
        
        @Override
	public Boolean deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseBoolean(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Boolean deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseBoolean(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class ByteDeserializer
        extends PrimitiveOrWrapperDeserializer<Byte>
    {
        public ByteDeserializer(Class<Byte> cls, Byte nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Byte deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseByte(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class ShortDeserializer
        extends PrimitiveOrWrapperDeserializer<Short>
    {
        public ShortDeserializer(Class<Short> cls, Short nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Short deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseShort(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class CharacterDeserializer
        extends PrimitiveOrWrapperDeserializer<Character>
    {
        public CharacterDeserializer(Class<Character> cls, Character nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Character deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            int value;

            if (t == JsonToken.VALUE_NUMBER_INT) { // ok iff ascii value
                value = jp.getIntValue();
                if (value >= 0 && value <= 0xFFFF) {
                    return Character.valueOf((char) value);
                }
            } else if (t == JsonToken.VALUE_STRING) { // this is the usual type
                // But does it have to be exactly one char?
                String text = jp.getText();
                if (text.length() == 1) {
                    return Character.valueOf(text.charAt(0));
                }
                // actually, empty should become null?
                if (text.length() == 0) {
                    return (Character) getEmptyValue();
                }
            }
            throw ctxt.mappingException(_valueClass, t);
        }
    }

    @JacksonStdImpl
    public final static class IntegerDeserializer
        extends PrimitiveOrWrapperDeserializer<Integer>
    {
        public IntegerDeserializer(Class<Integer> cls, Integer nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseInteger(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Integer deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseInteger(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class LongDeserializer
        extends PrimitiveOrWrapperDeserializer<Long>
    {
        public LongDeserializer(Class<Long> cls, Long nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Long deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseLong(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class FloatDeserializer
        extends PrimitiveOrWrapperDeserializer<Float>
    {
        public FloatDeserializer(Class<Float> cls, Float nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Float deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            /* 22-Jan-2009, tatu: Bounds/range checks would be tricky
             *   here, so let's not bother even trying...
             */
            return _parseFloat(jp, ctxt);
        }
    }

    @JacksonStdImpl
    public final static class DoubleDeserializer
        extends PrimitiveOrWrapperDeserializer<Double>
    {
        public DoubleDeserializer(Class<Double> cls, Double nvl)
        {
            super(cls, nvl);
        }

        @Override
        public Double deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            return _parseDouble(jp, ctxt);
        }

        // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
        // (is it an error to even call this version?)
        @Override
        public Double deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            return _parseDouble(jp, ctxt);
        }
    }

    /**
     * For type <code>Number.class</code>, we can just rely on type
     * mappings that plain {@link JsonParser#getNumberValue} returns.
     *<p>
     * Since 1.5, there is one additional complication: some numeric
     * types (specifically, int/Integer and double/Double) are "non-typed";
     * meaning that they will NEVER be output with type information.
     * But other numeric types may need such type information.
     * This is why {@link #deserializeWithType} must be overridden.
     */
    @JacksonStdImpl
    public final static class NumberDeserializer
        extends StdScalarDeserializer<Number>
    {
        public NumberDeserializer() { super(Number.class); }

        @Override
        public Number deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return jp.getBigIntegerValue();
                }
                return jp.getNumberValue();
            } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
                /* [JACKSON-72]: need to allow overriding the behavior
                 * regarding which type to use
                 */
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return jp.getDecimalValue();
                }
                return Double.valueOf(jp.getDoubleValue());
            }

            /* Textual values are more difficult... not parsing itself, but figuring
             * out 'minimal' type to use 
             */
            if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
                String text = jp.getText().trim();
                try {
                    if (text.indexOf('.') >= 0) { // floating point
                        // as per [JACKSON-72]:
                        if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                            return new BigDecimal(text);
                        }
                        return new Double(text);
                    }
                    // as per [JACKSON-100]:
                    if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                        return new BigInteger(text);
                    }
                    long value = Long.parseLong(text);
                    if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                        return Integer.valueOf((int) value);
                    }
                    return Long.valueOf(value);
                } catch (IllegalArgumentException iae) {
                    throw ctxt.weirdStringException(_valueClass, "not a valid number");
                }
            }
            // Otherwise, no can do:
            throw ctxt.mappingException(_valueClass, t);
        }

        /**
         * As mentioned in class Javadoc, there is additional complexity in
         * handling potentially mixed type information here. Because of this,
         * we must actually check for "raw" integers and doubles first, before
         * calling type deserializer.
         */
        @Override
        public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException
        {
            switch (jp.getCurrentToken()) {
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
            case VALUE_STRING:
                // can not point to type information: hence must be non-typed (int/double)
                return deserialize(jp, ctxt);
            }
            return typeDeserializer.deserializeTypedFromScalar(jp, ctxt);
        }
    }
    
    /*
    /**********************************************************
    /* And then bit more complicated (but non-structured) number
    /* types
    /**********************************************************
     */

    @JacksonStdImpl
    public static class BigDecimalDeserializer
        extends StdScalarDeserializer<BigDecimal>
    {
        public BigDecimalDeserializer() { super(BigDecimal.class); }

        @Override
	public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                return jp.getDecimalValue();
            }
            // String is ok too, can easily convert
            if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
                String text = jp.getText().trim();
                if (text.length() == 0) {
                    return null;
                }
                try {
                    return new BigDecimal(text);
                } catch (IllegalArgumentException iae) {
                    throw ctxt.weirdStringException(_valueClass, "not a valid representation");
                }
            }
            // Otherwise, no can do:
            throw ctxt.mappingException(_valueClass, t);
        }
    }

    /**
     * This is bit trickier to implement efficiently, while avoiding
     * overflow problems.
     */
    @JacksonStdImpl
    public static class BigIntegerDeserializer
        extends StdScalarDeserializer<BigInteger>
    {
        public BigIntegerDeserializer() { super(BigInteger.class); }

        @Override
		public BigInteger deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            String text;

            if (t == JsonToken.VALUE_NUMBER_INT) {
                switch (jp.getNumberType()) {
                case INT:
                case LONG:
                    return BigInteger.valueOf(jp.getLongValue());
                }
            } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
                /* Whether to fail if there's non-integer part?
                 * Could do by calling BigDecimal.toBigIntegerExact()
                 */
                return jp.getDecimalValue().toBigInteger();
            } else if (t != JsonToken.VALUE_STRING) { // let's do implicit re-parse
                // String is ok too, can easily convert; otherwise, no can do:
                throw ctxt.mappingException(_valueClass, t);
            }
            text = jp.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            try {
                return new BigInteger(text);
            } catch (IllegalArgumentException iae) {
                throw ctxt.weirdStringException(_valueClass, "not a valid representation");
            }
        }
    }

    /*
    /****************************************************
    /* Then trickier things: Date/Calendar types
    /****************************************************
     */

    /**
     * Compared to plain old {@link java.util.Date}, SQL version is easier
     * to deal with: mostly because it is more limited.
     */
    public static class SqlDateDeserializer
        extends StdScalarDeserializer<java.sql.Date>
    {
        public SqlDateDeserializer() { super(java.sql.Date.class); }

        @Override
        public java.sql.Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            Date d = _parseDate(jp, ctxt);
            return (d == null) ? null : new java.sql.Date(d.getTime());
        }
    }

    /*
    /****************************************************
    /* And other oddities
    /****************************************************
    */

    public static class StackTraceElementDeserializer
        extends StdScalarDeserializer<StackTraceElement>
    {
        public StackTraceElementDeserializer() { super(StackTraceElement.class); }

        @Override
        public StackTraceElement deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
        {
            JsonToken t = jp.getCurrentToken();
            // Must get an Object
            if (t == JsonToken.START_OBJECT) {
                String className = "", methodName = "", fileName = "";
                int lineNumber = -1;

                while ((t = jp.nextValue()) != JsonToken.END_OBJECT) {
                    String propName = jp.getCurrentName();
                    if ("className".equals(propName)) {
                        className = jp.getText();
                    } else if ("fileName".equals(propName)) {
                        fileName = jp.getText();
                    } else if ("lineNumber".equals(propName)) {
                        if (t.isNumeric()) {
                            lineNumber = jp.getIntValue();
                        } else {
                            throw JsonMappingException.from(jp, "Non-numeric token ("+t+") for property 'lineNumber'");
                        }
                    } else if ("methodName".equals(propName)) {
                        methodName = jp.getText();
                    } else if ("nativeMethod".equals(propName)) {
                        // no setter, not passed via constructor: ignore
                    } else {
                        handleUnknownProperty(jp, ctxt, _valueClass, propName);
                    }
                }
                return new StackTraceElement(className, methodName, fileName, lineNumber);
            }
            throw ctxt.mappingException(_valueClass, t);
        }
    }
}
