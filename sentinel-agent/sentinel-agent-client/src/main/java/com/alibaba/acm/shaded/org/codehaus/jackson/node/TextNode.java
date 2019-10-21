package com.alibaba.acm.shaded.org.codehaus.jackson.node;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.NumberInput;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.ByteArrayBuilder;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.CharTypes;

/**
 * Value node that contains a text value.
 */
public final class TextNode
    extends ValueNode
{
    final static int INT_SPACE = ' ';

    final static TextNode EMPTY_STRING_NODE = new TextNode("");

    final String _value;

    public TextNode(String v) { _value = v; }

    /**
     * Factory method that should be used to construct instances.
     * For some common cases, can reuse canonical instances: currently
     * this is the case for empty Strings, in future possible for
     * others as well. If null is passed, will return null.
     *
     * @return Resulting {@link TextNode} object, if <b>v</b>
     *   is NOT null; null if it is.
     */
    public static TextNode valueOf(String v)
    {
        if (v == null) {
            return null;
        }
        if (v.length() == 0) {
            return EMPTY_STRING_NODE;
        }
        return new TextNode(v);
    }

    @Override public JsonToken asToken() { return JsonToken.VALUE_STRING; }

    /**
     * Yes indeed it is textual
     */
    @Override
    public boolean isTextual() { return true; }

    @Override
    public String getTextValue() {
        return _value;
    }

    /**
     * Method for accessing textual contents assuming they were
     * base64 encoded; if so, they are decoded and resulting binary
     * data is returned.
     */
    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException
    {
        ByteArrayBuilder builder = new ByteArrayBuilder(100);
        final String str = _value;
        int ptr = 0;
        int len = str.length();

        main_loop:
        while (ptr < len) {
            // first, we'll skip preceding white space, if any
            char ch;
            do {
                ch = str.charAt(ptr++);
                if (ptr >= len) {
                    break main_loop;
                }
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                _reportInvalidBase64(b64variant, ch, 0);
            }
            int decodedData = bits;
            // then second base64 char; can't get padding yet, nor ws
            if (ptr >= len) {
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                _reportInvalidBase64(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;
            // third base64 char; can be padding, but not ws
            if (ptr >= len) {
                // but as per [JACKSON-631] can be end-of-input, iff not using padding
                if (!b64variant.usesPadding()) {
                    // Got 12 bits, only need 8, need to shift
                    decodedData >>= 4;
                    builder.append(decodedData);
                    break;
                }
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    _reportInvalidBase64(b64variant, ch, 2);
                }
                // Ok, must get padding
                if (ptr >= len) {
                    _reportBase64EOF();
                }
                ch = str.charAt(ptr++);
                if (!b64variant.usesPaddingChar(ch)) {
                    _reportInvalidBase64(b64variant, ch, 3, "expected padding character '"+b64variant.getPaddingChar()+"'");
                }
                // Got 12 bits, only need 8, need to shift
                decodedData >>= 4;
                builder.append(decodedData);
                continue;
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (ptr >= len) {
                // but as per [JACKSON-631] can be end-of-input, iff not using padding
                if (!b64variant.usesPadding()) {
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    break;
                }
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    _reportInvalidBase64(b64variant, ch, 3);
                }
                decodedData >>= 2;
                builder.appendTwoBytes(decodedData);
            } else {
                // otherwise, our triple is now complete
                decodedData = (decodedData << 6) | bits;
                builder.appendThreeBytes(decodedData);
            }
        }
        return builder.toByteArray();
    }

    @Override
    public byte[] getBinaryValue() throws IOException
    {
        return getBinaryValue(Base64Variants.getDefaultVariant());
    }
    
    /* 
    /**********************************************************
    /* General type coercions
    /**********************************************************
     */

    @Override
    public String asText() {
        return _value;
    }

    // note: neither fast nor elegant, but these work for now:

    @Override
    public boolean asBoolean(boolean defaultValue) {
        if (_value != null) {
            if ("true".equals(_value.trim())) {
                return true;
            }
        }
        return defaultValue;
    }
    
    @Override
    public int asInt(int defaultValue) {
        return NumberInput.parseAsInt(_value, defaultValue);
    }

    @Override
    public long asLong(long defaultValue) {
        return NumberInput.parseAsLong(_value, defaultValue);
    }
    
    @Override
    public double asDouble(double defaultValue) {
        return NumberInput.parseAsDouble(_value, defaultValue);
    }
    
    /* 
    /**********************************************************
    /* Serialization
    /**********************************************************
     */
    
    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        if (_value == null) {
            jg.writeNull();
        } else {
            jg.writeString(_value);
        }
    }

    /*
    /**********************************************************
    /* Overridden standard methods
    /**********************************************************
     */
    
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) { // final class, can do this
            return false;
        }
        return ((TextNode) o)._value.equals(_value);
    }
    
    @Override
    public int hashCode() { return _value.hashCode(); }

    /**
     * Different from other values, Strings need quoting
     */
    @Override
    public String toString()
    {
        int len = _value.length();
        len = len + 2 + (len >> 4);
        StringBuilder sb = new StringBuilder(len);
        appendQuoted(sb, _value);
        return sb.toString();
    }

    protected static void appendQuoted(StringBuilder sb, String content)
    {
        sb.append('"');
        CharTypes.appendQuoted(sb, content);
        sb.append('"');
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    protected void _reportInvalidBase64(Base64Variant b64variant, char ch, int bindex)
        throws JsonParseException
    {
        _reportInvalidBase64(b64variant, ch, bindex, null);
    }

    /**
     * @param bindex Relative index within base64 character unit; between 0
     *   and 3 (as unit has exactly 4 characters)
     */
    protected void _reportInvalidBase64(Base64Variant b64variant, char ch, int bindex, String msg)
        throws JsonParseException
    {
        String base;
        if (ch <= INT_SPACE) {
            base = "Illegal white space character (code 0x"+Integer.toHexString(ch)+") as character #"+(bindex+1)+" of 4-char base64 unit: can only used between units";
        } else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('"+b64variant.getPaddingChar()+"') as character #"+(bindex+1)+" of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x"+Integer.toHexString(ch)+") in base64 content";
        } else {
            base = "Illegal character '"+ch+"' (code 0x"+Integer.toHexString(ch)+") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        throw new JsonParseException(base, JsonLocation.NA);
    }

    protected void _reportBase64EOF()
        throws JsonParseException
    {
        throw new JsonParseException("Unexpected end-of-String when base64 content", JsonLocation.NA);
    }
}
