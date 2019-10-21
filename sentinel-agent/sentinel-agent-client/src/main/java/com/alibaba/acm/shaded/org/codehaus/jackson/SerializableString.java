package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Interface that defines how Jackson package can interact with efficient
 * pre-serialized or lazily-serialized and reused String representations.
 * Typically implementations store possible serialized version(s) so that
 * serialization of String can be done more efficiently, especially when
 * used multiple times.
 *
 * @since 1.7 (1.6 introduced implementation, but interface extracted later)
 * 
 * @see com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString
 */
public interface SerializableString
{
    /**
     * Returns unquoted String that this object represents (and offers
     * serialized forms for)
     */
    public String getValue();
    
    /**
     * Returns length of the (unquoted) String as characters.
     * Functionally equvalent to:
     *<pre>
     *   getValue().length();
     *</pre>
     */
    public int charLength();

    /**
     * Returns JSON quoted form of the String, as character array. Result
     * can be embedded as-is in textual JSON as property name or JSON String.
     */
    public char[] asQuotedChars();

    /**
     * Returns UTF-8 encoded version of unquoted String.
     * Functionally equivalent to (but more efficient than):
     *<pre>
     * getValue().getBytes("UTF-8");
     *</pre>
     */
    public byte[] asUnquotedUTF8();

    /**
     * Returns UTF-8 encoded version of JSON-quoted String.
     * Functionally equivalent to (but more efficient than):
     *<pre>
     * new String(asQuotedChars()).getBytes("UTF-8");
     *</pre>
     */
    public byte[] asQuotedUTF8();
}
