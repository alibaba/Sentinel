package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * Numeric node that contains simple 64-bit integer values.
 */
public final class BigIntegerNode
    extends NumericNode
{
    final protected BigInteger _value;

    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */

    public BigIntegerNode(BigInteger v) { _value = v; }

    public static BigIntegerNode valueOf(BigInteger v) { return new BigIntegerNode(v); }

    /* 
    /**********************************************************
    /* Overrridden JsonNode methods
    /**********************************************************
     */

    @Override
    public JsonToken asToken() { return JsonToken.VALUE_NUMBER_INT; }

    @Override
    public JsonParser.NumberType getNumberType() { return JsonParser.NumberType.BIG_INTEGER; }

    @Override
    public boolean isIntegralNumber() { return true; }

    @Override
    public boolean isBigInteger() { return true; }

    @Override
    public Number getNumberValue() {
        return _value;
    }

    @Override
    public int getIntValue() { return _value.intValue(); }

    @Override
    public long getLongValue() { return _value.longValue(); }

    @Override
    public BigInteger getBigIntegerValue() { return _value; }

    @Override
    public double getDoubleValue() { return _value.doubleValue(); }

    @Override
    public BigDecimal getDecimalValue() { return new BigDecimal(_value); }

    /* 
    /**********************************************************
    /* General type coercions
    /**********************************************************
     */
    
    @Override
    public String asText() {
        return _value.toString();
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        return !BigInteger.ZERO.equals(_value);
    }
    
    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jg.writeNumber(_value);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) { // final class, can do this
            return false;
        }
        return ((BigIntegerNode) o)._value.equals(_value);
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }
}
