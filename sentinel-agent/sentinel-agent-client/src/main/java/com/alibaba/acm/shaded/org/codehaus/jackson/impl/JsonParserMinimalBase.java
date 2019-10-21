package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser.Feature;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.NumberInput;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.ByteArrayBuilder;

/**
 * Intermediate base class used by all Jackson {@link JsonParser}
 * implementations, but does not add any additional fields that depend
 * on particular method of obtaining input.
 *<p>
 * Note that 'minimal' here mostly refers to minimal number of fields
 * (size) and functionality that is specific to certain types
 * of parser implementations; but not necessarily to number of methods.
 * 
 * @since 1.6
 *
 * @author Tatu Saloranta
 */
public abstract class JsonParserMinimalBase
    extends JsonParser
{
    // Control chars:
    protected final static int INT_TAB = '\t';
    protected final static int INT_LF = '\n';
    protected final static int INT_CR = '\r';
    protected final static int INT_SPACE = 0x0020;

    // Markup
    protected final static int INT_LBRACKET = '[';
    protected final static int INT_RBRACKET = ']';
    protected final static int INT_LCURLY = '{';
    protected final static int INT_RCURLY = '}';
    protected final static int INT_QUOTE = '"';
    protected final static int INT_BACKSLASH = '\\';
    protected final static int INT_SLASH = '/';
    protected final static int INT_COLON = ':';
    protected final static int INT_COMMA = ',';
    protected final static int INT_ASTERISK = '*';
    protected final static int INT_APOSTROPHE = '\'';

    // Letters we need
    protected final static int INT_b = 'b';
    protected final static int INT_f = 'f';
    protected final static int INT_n = 'n';
    protected final static int INT_r = 'r';
    protected final static int INT_t = 't';
    protected final static int INT_u = 'u';

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected JsonParserMinimalBase() { }
    protected JsonParserMinimalBase(int features) {
        super(features);
    }

    /*
    /**********************************************************
    /* Configuration overrides if any
    /**********************************************************
     */

    // from base class:

    //public void enableFeature(Feature f)
    //public void disableFeature(Feature f)
    //public void setFeature(Feature f, boolean state)
    //public boolean isFeatureEnabled(Feature f)

    /*
    /**********************************************************
    /* JsonParser impl
    /**********************************************************
     */

    @Override
    public abstract JsonToken nextToken() throws IOException, JsonParseException;

    //public final JsonToken nextValue()

    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException
    {
        if (_currToken != JsonToken.START_OBJECT
            && _currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;

        /* Since proper matching of start/end markers is handled
         * by nextToken(), we'll just count nesting levels here
         */
        while (true) {
            JsonToken t = nextToken();
            if (t == null) {
                _handleEOF();
                /* given constraints, above should never return;
                 * however, FindBugs doesn't know about it and
                 * complains... so let's add dummy break here
                 */
                return this;
            }
            switch (t) {
            case START_OBJECT:
            case START_ARRAY:
                ++open;
                break;
            case END_OBJECT:
            case END_ARRAY:
                if (--open == 0) {
                    return this;
                }
                break;
            }
        }
    }

    /**
     * Method sub-classes need to implement
     */
    protected abstract void _handleEOF() throws JsonParseException;

    //public JsonToken getCurrentToken()

    //public boolean hasCurrentToken()

    @Override
    public abstract String getCurrentName() throws IOException, JsonParseException;
    
    @Override
    public abstract void close() throws IOException;

    @Override
    public abstract boolean isClosed();

    @Override
    public abstract JsonStreamContext getParsingContext();

//    public abstract JsonLocation getTokenLocation();

//   public abstract JsonLocation getCurrentLocation();
    
    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override
    public abstract String getText() throws IOException, JsonParseException;

    @Override
    public abstract char[] getTextCharacters() throws IOException, JsonParseException;

    @Override
    public abstract boolean hasTextCharacters();

    @Override
    public abstract int getTextLength() throws IOException, JsonParseException;

    @Override
    public abstract int getTextOffset() throws IOException, JsonParseException;  

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    @Override
    public abstract byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException;

    /*
    /**********************************************************
    /* Public API, access with conversion/coercion
    /**********************************************************
     */

    @Override
    public boolean getValueAsBoolean(boolean defaultValue) throws IOException, JsonParseException
    {
        if (_currToken != null) {
            switch (_currToken) {
            case VALUE_NUMBER_INT:
                return getIntValue() != 0;
            case VALUE_TRUE:
                return true;
            case VALUE_FALSE:
            case VALUE_NULL:
                return false;
            case VALUE_EMBEDDED_OBJECT:
                {
                    Object value = this.getEmbeddedObject();
                    if (value instanceof Boolean) {
                        return ((Boolean) value).booleanValue();
                    }
                }
            case VALUE_STRING:
                String str = getText().trim();
                if ("true".equals(str)) {
                    return true;
                }
                break;
            }
        }
        return defaultValue;
    }
    
    @Override
    public int getValueAsInt(int defaultValue) throws IOException, JsonParseException
    {
        if (_currToken != null) {
            switch (_currToken) {
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return getIntValue();
            case VALUE_TRUE:
                return 1;
            case VALUE_FALSE:
            case VALUE_NULL:
                return 0;
            case VALUE_STRING:
                return NumberInput.parseAsInt(getText(), defaultValue);
            case VALUE_EMBEDDED_OBJECT:
                {
                    Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                }
            }
        }
        return defaultValue;
    }
    
    @Override
    public long getValueAsLong(long defaultValue) throws IOException, JsonParseException
    {
        if (_currToken != null) {
            switch (_currToken) {
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return getLongValue();
            case VALUE_TRUE:
                return 1;
            case VALUE_FALSE:
            case VALUE_NULL:
                return 0;
            case VALUE_STRING:
                return NumberInput.parseAsLong(getText(), defaultValue);
            case VALUE_EMBEDDED_OBJECT:
                {
                    Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                }
            }
        }
        return defaultValue;
    }

    @Override
    public double getValueAsDouble(double defaultValue) throws IOException, JsonParseException
    {
        if (_currToken != null) {
            switch (_currToken) {
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return getDoubleValue();
            case VALUE_TRUE:
                return 1;
            case VALUE_FALSE:
            case VALUE_NULL:
                return 0;
            case VALUE_STRING:
                return NumberInput.parseAsDouble(getText(), defaultValue);
            case VALUE_EMBEDDED_OBJECT:
                {
                    Object value = this.getEmbeddedObject();
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                }
            }
        }
        return defaultValue;
    }

    /*
    /**********************************************************
    /* Base64 decoding
    /**********************************************************
     */

    /**
     * Helper method that can be used for base64 decoding in cases where
     * encoded content has already been read as a String.
     * 
     * @since 1.9.3
     */
    protected void _decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant)
        throws IOException, JsonParseException
    {
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
                _reportInvalidBase64(b64variant, ch, 0, null);
            }
            int decodedData = bits;
            // then second base64 char; can't get padding yet, nor ws
            if (ptr >= len) {
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                _reportInvalidBase64(b64variant, ch, 1, null);
            }
            decodedData = (decodedData << 6) | bits;
            // third base64 char; can be padding, but not ws
            if (ptr >= len) {
                // but as per [JACKSON-631] can be end-of-input, iff not using padding
                if (!b64variant.usesPadding()) {
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
                    _reportInvalidBase64(b64variant, ch, 2, null);
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
                    _reportInvalidBase64(b64variant, ch, 3, null);
                }
                decodedData >>= 2;
                builder.appendTwoBytes(decodedData);
            } else {
                // otherwise, our triple is now complete
                decodedData = (decodedData << 6) | bits;
                builder.appendThreeBytes(decodedData);
            }
        }
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
        throw _constructError(base);
    }

    protected void _reportBase64EOF() throws JsonParseException {
        throw _constructError("Unexpected end-of-String in base64 content");
    }
    
    
    /*
    /**********************************************************
    /* Error reporting
    /**********************************************************
     */
    
    protected void _reportUnexpectedChar(int ch, String comment)
        throws JsonParseException
    {
        String msg = "Unexpected character ("+_getCharDesc(ch)+")";
        if (comment != null) {
            msg += ": "+comment;
        }
        _reportError(msg);
    }

    protected void _reportInvalidEOF()
        throws JsonParseException
    {
        _reportInvalidEOF(" in "+_currToken);
    }

    protected void _reportInvalidEOF(String msg)
        throws JsonParseException
    {
        _reportError("Unexpected end-of-input"+msg);
    }

    protected void _reportInvalidEOFInValue() throws JsonParseException
    {
        _reportInvalidEOF(" in a value");
    }
    
    protected void _throwInvalidSpace(int i)
        throws JsonParseException
    {
        char c = (char) i;
        String msg = "Illegal character ("+_getCharDesc(c)+"): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        _reportError(msg);
    }

    /**
     * Method called to report a problem with unquoted control character.
     * Note: starting with version 1.4, it is possible to suppress
     * exception by enabling {@link Feature#ALLOW_UNQUOTED_CONTROL_CHARS}.
     */
    protected void _throwUnquotedSpace(int i, String ctxtDesc)
        throws JsonParseException
    {
        // JACKSON-208; possible to allow unquoted control chars:
        if (!isEnabled(Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i >= INT_SPACE) {
            char c = (char) i;
            String msg = "Illegal unquoted character ("+_getCharDesc(c)+"): has to be escaped using backslash to be included in "+ctxtDesc;
            _reportError(msg);
        }
    }

    protected char _handleUnrecognizedCharacterEscape(char ch) throws JsonProcessingException
    {
        // as per [JACKSON-300]
        if (isEnabled(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)) {
            return ch;
        }
        // and [JACKSON-548]
        if (ch == '\'' && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return ch;
        }
        _reportError("Unrecognized character escape "+_getCharDesc(ch));
        return ch;
    }
    
    /*
    /**********************************************************
    /* Error reporting, generic
    /**********************************************************
     */

    protected final static String _getCharDesc(int ch)
    {
        char c = (char) ch;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code "+ch+")";
        }
        if (ch > 255) {
            return "'"+c+"' (code "+ch+" / 0x"+Integer.toHexString(ch)+")";
        }
        return "'"+c+"' (code "+ch+")";
    }

    protected final void _reportError(String msg)
        throws JsonParseException
    {
        throw _constructError(msg);
    }

    protected final void _wrapError(String msg, Throwable t)
        throws JsonParseException
    {
        throw _constructError(msg, t);
    }

    protected final void _throwInternal()
    {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }

    protected final JsonParseException _constructError(String msg, Throwable t)
    {
        return new JsonParseException(msg, getCurrentLocation(), t);
    }
}
