package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;
import java.util.Arrays;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * Value node that contains Base64 encoded binary value, which will be
 * output and stored as Json String value.
 */
public final class BinaryNode
    extends ValueNode
{
    final static BinaryNode EMPTY_BINARY_NODE = new BinaryNode(new byte[0]);

    final byte[] _data;

    /* 
    /**********************************************************
    /* Construction
    /**********************************************************
     */
    
    public BinaryNode(byte[] data)
    {
        _data = data;
    }

    public BinaryNode(byte[] data, int offset, int length)
    {
        if (offset == 0 && length == data.length) {
            _data = data;
        } else {
            _data = new byte[length];
            System.arraycopy(data, offset, _data, 0, length);
        }
    }

    public static BinaryNode valueOf(byte[] data)
    {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data);
    }

    public static BinaryNode valueOf(byte[] data, int offset, int length)
    {
        if (data == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data, offset, length);
    }

    /* 
    /**********************************************************
    /* General type coercions
    /**********************************************************
     */
    
    @Override
    public JsonToken asToken() {
        /* No distinct type; could use one for textual values,
         * but given that it's not in text form at this point,
         * embedded-object is closest
         */
        return JsonToken.VALUE_EMBEDDED_OBJECT;
    }

    @Override
    public boolean isBinary() { return true; }

    /**
     *<p>
     * Note: caller is not to modify returned array in any way, since
     * it is not a copy but reference to the underlying byte array.
     */
    @Override
    public byte[] getBinaryValue() { return _data; }

    /**
     * Hmmh. This is not quite as efficient as using {@link #serialize},
     * but will work correctly.
     */
    @Override
    public String asText() {
        return Base64Variants.getDefaultVariant().encode(_data, false);
    }

    /*
    /**********************************************************
    /* Public API, serialization
    /**********************************************************
     */
    
    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jg.writeBinary(_data);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) { // final class, can do this
            return false;
        }
        return Arrays.equals(((BinaryNode) o)._data, _data);
    }

    @Override
    public int hashCode() {
        return (_data == null) ? -1 : _data.length;
    }

    /**
     * Different from other values, since contents need to be surrounded
     * by (double) quotes.
     */
    @Override
    public String toString()
    {
        return Base64Variants.getDefaultVariant().encode(_data, true);
    }
}
