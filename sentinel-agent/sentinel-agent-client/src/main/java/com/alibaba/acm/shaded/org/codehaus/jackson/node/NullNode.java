package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * This singleton value class is used to contain explicit JSON null
 * value.
 */
public final class NullNode
    extends ValueNode
{
    // // Just need a fly-weight singleton

    public final static NullNode instance = new NullNode();

    private NullNode() { }

    public static NullNode getInstance() { return instance; }

    @Override public JsonToken asToken() { return JsonToken.VALUE_NULL; }

    @Override
    public boolean isNull() { return true; }

    @Override
    public String asText() {
        return "null";
    }

    @Override
    public int asInt(int defaultValue) {
        return 0;
    }
    @Override
    public long asLong(long defaultValue) {
        return 0L;
    }
    @Override
    public double asDouble(double defaultValue) {
        return 0.0;
    }
    
    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jg.writeNull();
    }

    @Override
    public boolean equals(Object o)
    {
        return (o == this);
    }
}
