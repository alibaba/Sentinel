package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import com.alibaba.acm.shaded.org.codehaus.jackson.SerializableString;

/**
 * String token that can lazily serialize String contained and then reuse that
 * serialization later on. This is similar to JDBC prepared statements, for example,
 * in that instances should only be created when they are used more than use;
 * prime candidates are various serializers.
 *<p>
 * Class is final for performance reasons and since this is not designed to
 * be extensible or customizable (customizations would occur in calling code)
 *
 * @since 1.6
 */
public class SerializedString implements SerializableString
{
    protected final String _value;

    /* 13-Dec-2010, tatu: Whether use volatile or not is actually an important
     *   decision for multi-core use cases. Cost of volatility can be non-trivial
     *   for heavy use cases, and serialized-string instances are accessed often.
     *   Given that all code paths with common Jackson usage patterns go through
     *   a few memory barriers (mostly with cache/reuse pool access) it seems safe
     *   enough to omit volatiles here, given how simple lazy initialization is.
     *   This can be compared to how {@link String#intern} works; lazily and
     *   without synchronization or use of volatile keyword.
     */
    
    protected /*volatile*/ byte[] _quotedUTF8Ref;

    protected /*volatile*/ byte[] _unquotedUTF8Ref;

    protected /*volatile*/ char[] _quotedChars;

    public SerializedString(String v) { _value = v; }

    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

    @Override
    public final String getValue() { return _value; }
    
    /**
     * Returns length of the String as characters
     */
    @Override
    public final int charLength() { return _value.length(); }
    
    @Override
    public final char[] asQuotedChars()
    {
        char[] result = _quotedChars;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsString(_value);
            _quotedChars = result;
        }
        return result;
    }

    /**
     * Accessor for accessing value that has been quoted using JSON
     * quoting rules, and encoded using UTF-8 encoding.
     */
    @Override
    public final byte[] asUnquotedUTF8()
    {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(_value);
            _unquotedUTF8Ref  = result;
        }
        return result;
    }

    /**
     * Accessor for accessing value as is (without JSON quoting)
     * encoded using UTF-8 encoding.
     */
    @Override
    public final byte[] asQuotedUTF8()
    {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(_value);
            _quotedUTF8Ref = result;
        }
        return result;
    }
    
    /*
    /**********************************************************
    /* Standard method overrides
    /**********************************************************
     */

    @Override
    public final String toString() { return _value; }
    
    @Override
    public final int hashCode() { return _value.hashCode(); }

    @Override
    public final boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SerializedString other = (SerializedString) o;
        return _value.equals(other._value);
    }
}
