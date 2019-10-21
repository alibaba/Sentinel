package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Enumeration for basic token types used for returning results
 * of parsing JSON content.
 */
public enum JsonToken
{
    /* Some notes on implementation:
     *
     * - Entries are to be ordered such that start/end array/object
     *   markers come first, then field name marker (if any), and
     *   finally scalar value tokens. This is assumed by some
     *   typing checks.
     */

    /**
     * NOT_AVAILABLE can be returned if {@link JsonParser}
     * implementation can not currently return the requested
     * token (usually next one), or even if any will be
     * available, but that may be able to determine this in
     * future. This is the case with non-blocking parsers --
     * they can not block to wait for more data to parse and
     * must return something.
     *
     * @since 0.9.7
     */
    NOT_AVAILABLE(null),

    /**
     * START_OBJECT is returned when encountering '{'
     * which signals starting of an Object value.
     */
    START_OBJECT("{"),
        
    /**
     * START_OBJECT is returned when encountering '}'
     * which signals ending of an Object value
     */
    END_OBJECT("}"),
        
    /**
     * START_OBJECT is returned when encountering '['
     * which signals starting of an Array value
     */
    START_ARRAY("["),

    /**
     * START_OBJECT is returned when encountering ']'
     * which signals ending of an Array value
     */
    END_ARRAY("]"),
        
    /**
     * FIELD_NAME is returned when a String token is encountered
     * as a field name (same lexical value, different function)
     */
    FIELD_NAME(null),
        
    /**
     * Placeholder token returned when the input source has a concept
     * of embedded Object that are not accessible as usual structure
     * (of starting with {@link #START_OBJECT}, having values, ending with
     * {@link #END_OBJECT}), but as "raw" objects.
     *<p>
     * Note: this token is never returned by regular JSON readers, but
     * only by readers that expose other kinds of source (like
     * {@link JsonNode}-based JSON trees, Maps, Lists and such).
     *
     * @since 1.1
     */
    VALUE_EMBEDDED_OBJECT(null),

    /**
     * VALUE_STRING is returned when a String token is encountered
     * in value context (array element, field value, or root-level
     * stand-alone value)
     */
    VALUE_STRING(null),

    /**
     * VALUE_NUMBER_INT is returned when an integer numeric token is
     * encountered in value context: that is, a number that does
     * not have floating point or exponent marker in it (consists
     * only of an optional sign, followed by one or more digits)
     */
    VALUE_NUMBER_INT(null),

    /**
     * VALUE_NUMBER_INT is returned when a numeric token other
     * that is not an integer is encountered: that is, a number that does
     * have floating point or exponent marker in it, in addition
     * to one or more digits.
     */
    VALUE_NUMBER_FLOAT(null),

    /**
     * VALUE_TRUE is returned when encountering literal "true" in
     * value context
     */
    VALUE_TRUE("true"),

    /**
     * VALUE_FALSE is returned when encountering literal "false" in
     * value context
     */
    VALUE_FALSE("false"),

    /**
     * VALUE_NULL is returned when encountering literal "null" in
     * value context
     */
    VALUE_NULL("null")
        ;

    final String _serialized;

    final char[] _serializedChars;

    final byte[] _serializedBytes;

    /**
     * @param Textual representation for this token, if there is a
     *   single static representation; null otherwise
     */
    JsonToken(String token)
    {
        if (token == null) {
            _serialized = null;
            _serializedChars = null;
            _serializedBytes = null;
        } else {
            _serialized = token;
            _serializedChars = token.toCharArray();
            // It's all in ascii, can just case...
            int len = _serializedChars.length;
            _serializedBytes = new byte[len];
            for (int i = 0; i < len; ++i) {
                _serializedBytes[i] = (byte) _serializedChars[i];
            }
        }
    }

    public String asString() { return _serialized; }
    public char[] asCharArray() { return _serializedChars; }
    public byte[] asByteArray() { return _serializedBytes; }

    public boolean isNumeric() {
        return (this == VALUE_NUMBER_INT) || (this == VALUE_NUMBER_FLOAT);
    }

    /**
     * Method that can be used to check whether this token represents
     * a valid non-structured value. This means all tokens other than
     * Object/Array start/end markers all field names.
     */
    public boolean isScalarValue() {
        // note: up to 1.5, VALUE_EMBEDDED_OBJECT was incorrectly considered non-scalar!
        return ordinal() >= VALUE_EMBEDDED_OBJECT.ordinal();
    }
}
