package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser;

/**
 * Intermediate value node used for numeric nodes.
 */
public abstract class NumericNode
    extends ValueNode
{
    protected NumericNode() { }

    @Override
    public final boolean isNumber() { return true; }

    // // // Let's re-abstract so sub-classes handle them

    @Override
    public abstract JsonParser.NumberType getNumberType();

    @Override
    public abstract Number getNumberValue();
    @Override
    public abstract int getIntValue();
    @Override
    public abstract long getLongValue();
    @Override
    public abstract double getDoubleValue();
    @Override
    public abstract BigDecimal getDecimalValue();
    @Override
    public abstract BigInteger getBigIntegerValue();

    /* 
    /**********************************************************
    /* General type coercions
    /**********************************************************
     */
    
    @Override
    public abstract String asText();

    @Override
    public int asInt() {
        return getIntValue();
    }
    @Override
    public int asInt(int defaultValue) {
        return getIntValue();
    }

    @Override
    public long asLong() {
        return getLongValue();
    }
    @Override
    public long asLong(long defaultValue) {
        return getLongValue();
    }
    
    @Override
    public double asDouble() {
        return getDoubleValue();
    }
    @Override
    public double asDouble(double defaultValue) {
        return getDoubleValue();
    }
}
