package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * Numeric node that contains values that do not fit in simple
 * integer (int, long) or floating point (double) values.
 */
public final class DecimalNode
    extends NumericNode
{
    final protected BigDecimal _value;

    /* 
    /**********************************************************
    /* Construction
    /**********************************************************
     */

    public DecimalNode(BigDecimal v) { _value = v; }

    public static DecimalNode valueOf(BigDecimal d) { return new DecimalNode(d); }

    /* 
    /**********************************************************
    /* BaseJsonNode extended API
    /**********************************************************
     */

    @Override public JsonToken asToken() { return JsonToken.VALUE_NUMBER_FLOAT; }

    @Override
    public JsonParser.NumberType getNumberType() { return JsonParser.NumberType.BIG_DECIMAL; }

    /* 
    /**********************************************************
    /* Overrridden JsonNode methods
    /**********************************************************
     */

    @Override
    public boolean isFloatingPointNumber() { return true; }
    
    @Override
    public boolean isBigDecimal() { return true; }
    
    @Override
    public Number getNumberValue() { return _value; }

    @Override
    public int getIntValue() { return _value.intValue(); }

    @Override
    public long getLongValue() { return _value.longValue(); }


    @Override
    public BigInteger getBigIntegerValue() { return _value.toBigInteger(); }

    @Override
    public double getDoubleValue() { return _value.doubleValue(); }

    @Override
    public BigDecimal getDecimalValue() { return _value; }

    @Override
    public String asText() {
        return _value.toString();
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
        return ((DecimalNode) o)._value.equals(_value);
    }

    @Override
    public int hashCode() { return _value.hashCode(); }
}
