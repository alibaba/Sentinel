/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code and binary code bundles.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.acm.shaded.org.codehaus.jackson;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.TypeReference;

/**
 * Base class that defines public API for reading JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonParser
    implements Closeable, Versioned
{
    private final static int MIN_BYTE_I = (int) Byte.MIN_VALUE;
    // [JACKSON-804]: allow range up to 255 (instead of Java's 127) for better interoperability
    private final static int MAX_BYTE_I = 255;

    private final static int MIN_SHORT_I = (int) Short.MIN_VALUE;
    private final static int MAX_SHORT_I = (int) Short.MAX_VALUE;

    /**
     * Enumeration of possible "native" (optimal) types that can be
     * used for numbers.
     */
    public enum NumberType {
        INT, LONG, BIG_INTEGER, FLOAT, DOUBLE, BIG_DECIMAL
    };

    /**
     * Enumeration that defines all togglable features for parsers.
     */
    public enum Feature {
        
        // // // Low-level I/O handling features:
        
        /**
         * Feature that determines whether parser will automatically
         * close underlying input source that is NOT owned by the
         * parser. If disabled, calling application has to separately
         * close the underlying {@link InputStream} and {@link Reader}
         * instances used to create the parser. If enabled, parser
         * will handle closing, as long as parser itself gets closed:
         * this happens when end-of-input is encountered, or parser
         * is closed by a call to {@link JsonParser#close}.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_SOURCE(true),
            
        // // // Support for non-standard data format constructs

        /**
         * Feature that determines whether parser will allow use
         * of Java/C++ style comments (both '/'+'*' and
         * '//' varieties) within parsed content or not.
         *<p>
         * Since JSON specification does not mention comments as legal
         * construct,
         * this is a non-standard feature; however, in the wild
         * this is extensively used. As such, feature is
         * <b>disabled by default</b> for parsers and must be
         * explicitly enabled (via factory or parser instance).
         *<p>
         * This feature can be changed for parser instances.
         */
        ALLOW_COMMENTS(false),

        /**
         * Feature that determines whether parser will allow use
         * of unquoted field names (which is allowed by Javascript,
         * but not by JSON specification).
         *<p>
         * Since JSON specification requires use of double quotes for
         * field names,
         * this is a non-standard feature, and as such disabled by
         * default.
         *<p>
         * This feature can be changed for parser instances.
         *
         * @since 1.2
         */
        ALLOW_UNQUOTED_FIELD_NAMES(false),

        /**
         * Feature that determines whether parser will allow use
         * of single quotes (apostrophe, character '\'') for
         * quoting Strings (names and String values). If so,
         * this is in addition to other acceptabl markers.
         * but not by JSON specification).
         *<p>
         * Since JSON specification requires use of double quotes for
         * field names,
         * this is a non-standard feature, and as such disabled by
         * default.
         *<p>
         * This feature can be changed for parser instances.
         *
         * @since 1.3
         */
        ALLOW_SINGLE_QUOTES(false),

        /**
         * Feature that determines whether parser will allow
         * JSON Strings to contain unquoted control characters
         * (ASCII characters with value less than 32, including
         * tab and line feed characters) or not.
         * If feature is set false, an exception is thrown if such a
         * character is encountered.
         *<p>
         * Since JSON specification requires quoting for all control characters,
         * this is a non-standard feature, and as such disabled by default.
         *<p>
         * This feature can be changed for parser instances.
         *
         * @since 1.4
         */
        ALLOW_UNQUOTED_CONTROL_CHARS(false),

        /**
         * Feature that can be enabled to accept quoting of all character
         * using backslash qooting mechanism: if not enabled, only characters
         * that are explicitly listed by JSON specification can be thus
         * escaped (see JSON spec for small list of these characters)
         *<p>
         * Since JSON specification requires quoting for all control characters,
         * this is a non-standard feature, and as such disabled by default.
         *<p>
         * This feature can be changed for parser instances.
         * 
         * @since 1.6
         */
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),

        /**
         * Feature that determines whether parser will allow
         * JSON integral numbers to start with additional (ignorable) 
         * zeroes (like: 000001). If enabled, no exception is thrown, and extra
         * nulls are silently ignored (and not included in textual representation
         * exposed via {@link JsonParser#getText}).
         *<p>
         * Since JSON specification does not allow leading zeroes,
         * this is a non-standard feature, and as such disabled by default.
         *<p>
         * This feature can be changed for parser instances.
         *
         * @since 1.8
         */
        ALLOW_NUMERIC_LEADING_ZEROS(false),
        
        /**
         * Feature that allows parser to recognize set of
         * "Not-a-Number" (NaN) tokens as legal floating number
         * values (similar to how many other data formats and
         * programming language source code allows it).
         * Specific subset contains values that
         * <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema</a>
         * (see section 3.2.4.1, Lexical Representation)
         * allows (tokens are quoted contents, not including quotes):
         *<ul>
         *  <li>"INF" (for positive infinity), as well as alias of "Infinity"
         *  <li>"-INF" (for negative infinity), alias "-Infinity"
         *  <li>"NaN" (for other not-a-numbers, like result of division by zero)
         *</ul>
         */

         ALLOW_NON_NUMERIC_NUMBERS(false),
        
        // // // Controlling canonicalization (interning etc)
        
        /**
         * Feature that determines whether JSON object field names are
         * to be canonicalized using {@link String#intern} or not:
         * if enabled, all field names will be intern()ed (and caller
         * can count on this being true for all such names); if disabled,
         * no intern()ing is done. There may still be basic
         * canonicalization (that is, same String will be used to represent
         * all identical object property names for a single document).
         *<p>
         * Note: this setting only has effect if
         * {@link #CANONICALIZE_FIELD_NAMES} is true -- otherwise no
         * canonicalization of any sort is done.
         *
         * @since 1.3
         */
        INTERN_FIELD_NAMES(true),

        /**
         * Feature that determines whether JSON object field names are
         * to be canonicalized (details of how canonicalization is done
         * then further specified by
         * {@link #INTERN_FIELD_NAMES}).
         *
         * @since 1.5
         */
        CANONICALIZE_FIELD_NAMES(true),


            ;

        final boolean _defaultState;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         */
        public static int collectDefaults()
        {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }
        
        private Feature(boolean defaultState) {
            _defaultState = defaultState;
        }
        
        public boolean enabledByDefault() { return _defaultState; }

        public boolean enabledIn(int flags) { return (flags & getMask()) != 0; }
        
        public int getMask() { return (1 << ordinal()); }
    };

    /*
    /**********************************************************
    /* Minimal configuration state
    /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser.Feature}s
     * are enabled.
     */
    protected int _features;

    /*
    /**********************************************************
    /* Minimal generic state
    /**********************************************************
     */

    /**
     * Last token retrieved via {@link #nextToken}, if any.
     * Null before the first call to <code>nextToken()</code>,
     * as well as if token has been explicitly cleared
     * (by call to {@link #clearCurrentToken})
     */
    protected JsonToken _currToken;

    /**
     * Last cleared token, if any: that is, value that was in
     * effect when {@link #clearCurrentToken} was called.
     */
    protected JsonToken _lastClearedToken;

    /*
    /**********************************************************
    /* Construction, configuration, initialization
    /**********************************************************
     */

    protected JsonParser() { }
    protected JsonParser(int features) {
        _features = features;
    }

    /**
     * Accessor for {@link ObjectCodec} associated with this
     * parser, if any. Codec is used by {@link #readValueAs(Class)}
     * method (and its variants).
     *
     * @since 1.3
     */
    public abstract ObjectCodec getCodec();

    /**
     * Setter that allows defining {@link ObjectCodec} associated with this
     * parser, if any. Codec is used by {@link #readValueAs(Class)}
     * method (and its variants).
     *
     * @since 1.3
     */
    public abstract void setCodec(ObjectCodec c);

    /**
     * Method to call to make this parser use specified schema. Method must
     * be called before trying to parse any content, right after parser instance
     * has been created.
     * Note that not all parsers support schemas; and those that do usually only
     * accept specific types of schemas: ones defined for data format parser can read.
     *<p>
     * If parser does not support specified schema, {@link UnsupportedOperationException}
     * is thrown.
     * 
     * @param schema Schema to use
     * 
     * @throws UnsupportedOperationException if parser does not support schema
     * 
     * @since 1.8
     */
    public void setSchema(FormatSchema schema)
    {
        throw new UnsupportedOperationException("Parser of type "+getClass().getName()+" does not support schema of type '"
                +schema.getSchemaType()+"'");
    }
    
    /**
     * Method that can be used to verify that given schema can be used with
     * this parser (using {@link #setSchema}).
     * 
     * @param schema Schema to check
     * 
     * @return True if this parser can use given schema; false if not
     * 
     * @since 1.8
     */
    public boolean canUseSchema(FormatSchema schema) {
        return false;
    }
    
    /**
     * @since 1.6
     */
    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    /**
     * Method that can be used to get access to object that is used
     * to access input being parsed; this is usually either
     * {@link InputStream} or {@link Reader}, depending on what
     * parser was constructed with.
     * Note that returned value may be null in some cases; including
     * case where parser implementation does not want to exposed raw
     * source to caller.
     * In cases where input has been decorated, object returned here
     * is the decorated version; this allows some level of interaction
     * between users of parser and decorator object.
     *<p>
     * In general use of this accessor should be considered as
     * "last effort", i.e. only used if no other mechanism is applicable.
     * 
     * @since 1.8
     */
    public Object getInputSource() {
        return null;
    }
    
    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    /**
     * Closes the parser so that no further iteration or data access
     * can be made; will also close the underlying input source
     * if parser either <b>owns</b> the input source, or feature
     * {@link Feature#AUTO_CLOSE_SOURCE} is enabled.
     * Whether parser owns the input source depends on factory
     * method that was used to construct instance (so check
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory} for details,
     * but the general
     * idea is that if caller passes in closable resource (such
     * as {@link InputStream} or {@link Reader}) parser does NOT
     * own the source; but if it passes a reference (such as
     * {@link java.io.File} or {@link java.net.URL} and creates
     * stream or reader it does own them.
     */
    @Override
    public abstract void close() throws IOException;

    /*
    /**********************************************************
    /* Buffer handling
    /**********************************************************
     */

    /**
     * Method that can be called to push back any content that
     * has been read but not consumed by the parser. This is usually
     * done after reading all content of interest using parser.
     * Content is released by writing it to given stream if possible;
     * if underlying input is byte-based it can released, if not (char-based)
     * it can not.
     * 
     * @return -1 if the underlying content source is not byte based
     *    (that is, input can not be sent to {@link OutputStream};
     *    otherwise number of bytes released (0 if there was nothing to release)
     *    
     * @throws IOException if write to stream threw exception
     * 
     * @since 1.6
     */    
    public int releaseBuffered(OutputStream out) throws IOException
    {
        return -1;
    }

    /**
     * Method that can be called to push back any content that
     * has been read but not consumed by the parser.
     * This is usually
     * done after reading all content of interest using parser.
     * Content is released by writing it to given writer if possible;
     * if underlying input is char-based it can released, if not (byte-based)
     * it can not.
     * 
     * @return -1 if the underlying content source is not char-based
     *    (that is, input can not be sent to {@link Writer};
     *    otherwise number of chars released (0 if there was nothing to release)
     *    
     * @throws IOException if write using Writer threw exception
     * 
     * @since 1.6
     */    
    public int releaseBuffered(Writer w) throws IOException
    {
        return -1;
    }
    
    /*
    /***************************************************
    /* Public API, configuration
    /***************************************************
     */

    /**
     * Method for enabling specified parser feature
     * (check {@link Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonParser enable(Feature f)
    {
        _features |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified  feature
     * (check {@link Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonParser disable(Feature f)
    {
        _features &= ~f.getMask();
        return this;
    }

    /**
     * Method for enabling or disabling specified feature
     * (check {@link Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonParser configure(Feature f, boolean state)
    {
        if (state) {
            enableFeature(f);
        } else {
            disableFeature(f);
        }
        return this;
    }

    /**
     * Method for checking whether specified {@link Feature}
     * is enabled.
     *
     * @since 1.2
     */
    public boolean isEnabled(Feature f) {
        return (_features & f.getMask()) != 0;
    }

    /** @deprecated Use {@link #configure} instead
     */
    @SuppressWarnings("dep-ann")
    public void setFeature(Feature f, boolean state) { configure(f, state); }

    /** @deprecated Use {@link #enable(Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public void enableFeature(Feature f) { enable(f); }

    /** @deprecated Use {@link #disable(Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public void disableFeature(Feature f) { disable(f); }

    /** @deprecated Use {@link #isEnabled(Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public final boolean isFeatureEnabled(Feature f) { return isEnabled(f); }


    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    /**
     * Main iteration method, which will advance stream enough
     * to determine type of the next token, if any. If none
     * remaining (stream has no content other than possible
     * white space before ending), null will be returned.
     *
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     */
    public abstract JsonToken nextToken()
        throws IOException, JsonParseException;

    /**
     * Iteration method that will advance stream enough
     * to determine type of the next token that is a value type
     * (including JSON Array and Object start/end markers).
     * Or put another way, nextToken() will be called once,
     * and if {@link JsonToken#FIELD_NAME} is returned, another
     * time to get the value for the field.
     * Method is most useful for iterating over value entries
     * of JSON objects; field name will still be available
     * by calling {@link #getCurrentName} when parser points to
     * the value.
     *
     * @return Next non-field-name token from the stream, if any found,
     *   or null to indicate end-of-input (or, for non-blocking
     *   parsers, {@link JsonToken#NOT_AVAILABLE} if no tokens were
     *   available yet)
     */
    public JsonToken nextValue()
        throws IOException, JsonParseException
    {
        /* Implementation should be as trivial as follows; only
         * needs to change if we are to skip other tokens (for
         * example, if comments were exposed as tokens)
         */
        JsonToken t = nextToken();
        if (t == JsonToken.FIELD_NAME) {
            t = nextToken();
        }
        return t;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * verifies whether it is {@link JsonToken#FIELD_NAME} with specified name
     * and returns result of that comparison.
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.FIELD_NAME) && str.getValue().equals(getCurrentName());
     *</pre>
     * but may be faster for parser to verify, and can therefore be used if caller
     * expects to get such a property name from input next.
     * 
     * @param str Property name to compare next token to (if next token is <code>JsonToken.FIELD_NAME<code>)
     * 
     * @since 1.9
     */
    public boolean nextFieldName(SerializableString str)
        throws IOException, JsonParseException
    {
        return (nextToken() == JsonToken.FIELD_NAME) && str.getValue().equals(getCurrentName());
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_STRING} returns contained String value;
     * otherwise returns null.
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a String value next from input.
     * 
     * @since 1.9
     */
    public String nextTextValue()
        throws IOException, JsonParseException
    {
        return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_NUMBER_INT} returns 32-bit int value;
     * otherwise returns specified default value
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a String value next from input.
     * 
     * @since 1.9
     */
    public int nextIntValue(int defaultValue)
        throws IOException, JsonParseException
    {
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_NUMBER_INT} returns 64-bit long value;
     * otherwise returns specified default value
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a String value next from input.
     * 
     * @since 1.9
     */
    public long nextLongValue(long defaultValue)
        throws IOException, JsonParseException
    {
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_TRUE} or {@link JsonToken#VALUE_FALSE}
     * returns matching Boolean value; otherwise return null.
     * It is functionally equivalent to:
     *<pre>
     *  JsonToken t = nextToken();
     *  if (t == JsonToken.VALUE_TRUE) return Boolean.TRUE;
     *  if (t == JsonToken.VALUE_FALSE) return Boolean.FALSE;
     *  return null;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a String value next from input.
     * 
     * @since 1.9
     */
    public Boolean nextBooleanValue()
        throws IOException, JsonParseException
    {
        switch (nextToken()) {
        case VALUE_TRUE:
            return Boolean.TRUE;
        case VALUE_FALSE:
            return Boolean.FALSE;
        }
        return null;
    }
    
    /**
     * Method that will skip all child tokens of an array or
     * object token that the parser currently points to,
     * iff stream points to 
     * {@link JsonToken#START_OBJECT} or {@link JsonToken#START_ARRAY}.
     * If not, it will do nothing.
     * After skipping, stream will point to <b>matching</b>
     * {@link JsonToken#END_OBJECT} or {@link JsonToken#END_ARRAY}
     * (possibly skipping nested pairs of START/END OBJECT/ARRAY tokens
     * as well as value tokens).
     * The idea is that after calling this method, application
     * will call {@link #nextToken} to point to the next
     * available token, if any.
     */
    public abstract JsonParser skipChildren()
        throws IOException, JsonParseException;
    
    /**
     * Method that can be called to determine whether this parser
     * is closed or not. If it is closed, no new tokens can be
     * retrieved by calling {@link #nextToken} (and the underlying
     * stream may be closed). Closing may be due to an explicit
     * call to {@link #close} or because parser has encountered
     * end of input.
     */
    public abstract boolean isClosed();
    
    /*
    /**********************************************************
    /* Public API, token accessors
    /**********************************************************
     */

    /**
     * Accessor to find which token parser currently points to, if any;
     * null will be returned if none.
     * If return value is non-null, data associated with the token
     * is available via other accessor methods.
     *
     * @return Type of the token this parser currently points to,
     *   if any: null before any tokens have been read, and
     *   after end-of-input has been encountered, as well as
     *   if the current token has been explicitly cleared.
     */
    public JsonToken getCurrentToken() {
        return _currToken;
    }

    /**
     * Method for checking whether parser currently points to
     * a token (and data for that token is available).
     * Equivalent to check for <code>parser.getCurrentToken() != null</code>.
     *
     * @return True if the parser just returned a valid
     *   token via {@link #nextToken}; false otherwise (parser
     *   was just constructed, encountered end-of-input
     *   and returned null from {@link #nextToken}, or the token
     *   has been consumed)
     */
    public boolean hasCurrentToken() {
        return _currToken != null;
    }


    /**
     * Method called to "consume" the current token by effectively
     * removing it so that {@link #hasCurrentToken} returns false, and
     * {@link #getCurrentToken} null).
     * Cleared token value can still be accessed by calling
     * {@link #getLastClearedToken} (if absolutely needed), but
     * usually isn't.
     *<p>
     * Method was added to be used by the optional data binder, since
     * it has to be able to consume last token used for binding (so that
     * it will not be used again).
     */
    public void clearCurrentToken() {
        if (_currToken != null) {
            _lastClearedToken = _currToken;
            _currToken = null;
        }
    }

    /**
     * Method that can be called to get the name associated with
     * the current token: for {@link JsonToken#FIELD_NAME}s it will
     * be the same as what {@link #getText} returns;
     * for field values it will be preceding field name;
     * and for others (array values, root-level values) null.
     */
    public abstract String getCurrentName()
        throws IOException, JsonParseException;

    /**
     * Method that can be used to access current parsing context reader
     * is in. There are 3 different types: root, array and object contexts,
     * with slightly different available information. Contexts are
     * hierarchically nested, and can be used for example for figuring
     * out part of the input document that correspond to specific
     * array or object (for highlighting purposes, or error reporting).
     * Contexts can also be used for simple xpath-like matching of
     * input, if so desired.
     */
    public abstract JsonStreamContext getParsingContext();

    /**
     * Method that return the <b>starting</b> location of the current
     * token; that is, position of the first character from input
     * that starts the current token.
     */
    public abstract JsonLocation getTokenLocation();

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes.
     */
    public abstract JsonLocation getCurrentLocation();

    /**
     * Method that can be called to get the last token that was
     * cleared using {@link #clearCurrentToken}. This is not necessarily
     * the latest token read.
     * Will return null if no tokens have been cleared,
     * or if parser has been closed.
     */
    public JsonToken getLastClearedToken() {
        return _lastClearedToken;
    }

    /**
     * Specialized accessor that can be used to verify that the current
     * token indicates start array (usually meaning that current token
     * is {@link JsonToken#START_ARRAY}) when start array is expected.
     * For some specialized parsers this can return true for other cases
     * as well; this is usually done to emulate arrays.
     *<p>
     * Default implementation is equivalent to:
     *<pre>
     *   getCurrentToken() == JsonToken.START_ARRAY
     *</pre>
     * but may be overridden by custom parser implementations.
     *
     * @return True if the current token can be considered as a
     *   start-array marker (such {@link JsonToken#START_ARRAY});
     *   false if not.
     * 
     * @since 1.7
     */
    public boolean isExpectedStartArrayToken() {
        return getCurrentToken() == JsonToken.START_ARRAY;
    }
    
    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    /**
     * Method for accessing textual representation of the current token;
     * if no current token (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any token type.
     */
    public abstract String getText()
        throws IOException, JsonParseException;

    /**
     * Method similar to {@link #getText}, but that will return
     * underlying (unmodifiable) character array that contains
     * textual value, instead of constructing a String object
     * to contain this information.
     * Note, however, that:
     *<ul>
     * <li>Textual contents are not guaranteed to start at
     *   index 0 (rather, call {@link #getTextOffset}) to
     *   know the actual offset
     *  </li>
     * <li>Length of textual contents may be less than the
     *  length of returned buffer: call {@link #getTextLength}
     *  for actual length of returned content.
     *  </li>
     * </ul>
     *<p>
     * Note that caller <b>MUST NOT</b> modify the returned
     * character array in any way -- doing so may corrupt
     * current parser state and render parser instance useless.
     *<p>
     * The only reason to call this method (over {@link #getText})
     * is to avoid construction of a String object (which
     * will make a copy of contents).
     */
    public abstract char[] getTextCharacters()
        throws IOException, JsonParseException;

    /**
     * Accessor used with {@link #getTextCharacters}, to know length
     * of String stored in returned buffer.
     *
     * @return Number of characters within buffer returned
     *   by {@link #getTextCharacters} that are part of
     *   textual content of the current token.
     */
    public abstract int getTextLength()
        throws IOException, JsonParseException;

    /**
     * Accessor used with {@link #getTextCharacters}, to know offset
     * of the first text content character within buffer.
     *
     * @return Offset of the first character within buffer returned
     *   by {@link #getTextCharacters} that is part of
     *   textual content of the current token.
     */
    public abstract int getTextOffset()
        throws IOException, JsonParseException;

    /**
     * Method that can be used to determine whether calling of
     * {@link #getTextCharacters} would be the most efficient
     * way to access textual content for the event parser currently
     * points to.
     *<p> 
     * Default implementation simply returns false since only actual
     * implementation class has knowledge of its internal buffering
     * state.
     * Implementations are strongly encouraged to properly override
     * this method, to allow efficient copying of content by other
     * code.
     * 
     * @return True if parser currently has character array that can
     *   be efficiently returned via {@link #getTextCharacters}; false
     *   means that it may or may not exist
     * 
     * @since 1.6
     */
    public boolean hasTextCharacters() {
        return false;
    }
    
    /*
    /**********************************************************
    /* Public API, access to token information, numeric
    /**********************************************************
     */

    /**
     * Generic number value accessor method that will work for
     * all kinds of numeric values. It will return the optimal
     * (simplest/smallest possible) wrapper object that can
     * express the numeric value just parsed.
     */
    public abstract Number getNumberValue()
        throws IOException, JsonParseException;

    /**
     * If current token is of type 
     * {@link JsonToken#VALUE_NUMBER_INT} or
     * {@link JsonToken#VALUE_NUMBER_FLOAT}, returns
     * one of {@link NumberType} constants; otherwise returns null.
     */
    public abstract NumberType getNumberType()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java byte primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java byte, a {@link JsonParseException}
     * will be thrown to indicate numeric overflow/underflow.
     */
    public byte getByteValue()
        throws IOException, JsonParseException
    {
        int value = getIntValue();
        // So far so good: but does it fit?
        if (value < MIN_BYTE_I || value > MAX_BYTE_I) {
            throw _constructError("Numeric value ("+getText()+") out of range of Java byte");
        }
        return (byte) value;
    }

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java short primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java short, a {@link JsonParseException}
     * will be thrown to indicate numeric overflow/underflow.
     */
    public short getShortValue()
        throws IOException, JsonParseException
    {
        int value = getIntValue();
        if (value < MIN_SHORT_I || value > MAX_SHORT_I) {
            throw _constructError("Numeric value ("+getText()+") out of range of Java short");
        }
        return (short) value;
    }

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java int primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java int, a {@link JsonParseException}
     * may be thrown to indicate numeric overflow/underflow.
     */
    public abstract int getIntValue()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a Java long primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting to int; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the token is an integer, but its value falls
     * outside of range of Java long, a {@link JsonParseException}
     * may be thrown to indicate numeric overflow/underflow.
     */
    public abstract long getLongValue()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can not be used as a Java long primitive type due to its
     * magnitude.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDecimalValue}
     * and then constructing a {@link BigInteger} from that value.
     */
    public abstract BigInteger getBigIntegerValue()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java float primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java float, a {@link JsonParseException}
     * will be thrown to indicate numeric overflow/underflow.
     */
    public abstract float getFloatValue()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java double primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java double, a {@link JsonParseException}
     * will be thrown to indicate numeric overflow/underflow.
     */
    public abstract double getDoubleValue()
        throws IOException, JsonParseException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} or
     * {@link JsonToken#VALUE_NUMBER_INT}. No under/overflow exceptions
     * are ever thrown.
     */
    public abstract BigDecimal getDecimalValue()
        throws IOException, JsonParseException;

    /*
    /**********************************************************
    /* Public API, access to token information, other
    /**********************************************************
     */
    
    /**
     * Convenience accessor that can be called when the current
     * token is {@link JsonToken#VALUE_TRUE} or
     * {@link JsonToken#VALUE_FALSE}.
     *<p>
     * Note: if the token is not of above-mentioned boolean types,
 an integer, but its value falls
     * outside of range of Java long, a {@link JsonParseException}
     * may be thrown to indicate numeric overflow/underflow.
     *
     * @since 1.3
     */
    public boolean getBooleanValue()
        throws IOException, JsonParseException
    {
        if (getCurrentToken() == JsonToken.VALUE_TRUE) return true;
        if (getCurrentToken() == JsonToken.VALUE_FALSE) return false;
        throw new JsonParseException("Current token ("+_currToken+") not of boolean type", getCurrentLocation());
    }

    /**
     * Accessor that can be called if (and only if) the current token
     * is {@link JsonToken#VALUE_EMBEDDED_OBJECT}. For other token types,
     * null is returned.
     *<p>
     * Note: only some specialized parser implementations support
     * embedding of objects (usually ones that are facades on top
     * of non-streaming sources, such as object trees).
     *
     * @since 1.3
     */
    public Object getEmbeddedObject()
        throws IOException, JsonParseException
    {
        // By default we will always return null
        return null;
    }

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    /**
     * Method that can be used to read (and consume -- results
     * may not be accessible using other methods after the call)
     * base64-encoded binary data
     * included in the current textual JSON value.
     * It works similar to getting String value via {@link #getText}
     * and decoding result (except for decoding part),
     * but should be significantly more performant.
     *<p>
     * Note that non-decoded textual contents of the current token
     * are not guaranteed to be accessible after this method
     * is called. Current implementation, for example, clears up
     * textual content during decoding.
     * Decoded binary content, however, will be retained until
     * parser is advanced to the next event.
     *
     * @param b64variant Expected variant of base64 encoded
     *   content (see {@link Base64Variants} for definitions
     *   of "standard" variants).
     *
     * @return Decoded binary data
     */
    public abstract byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException;

    /**
     * Convenience alternative to {@link #getBinaryValue(Base64Variant)}
     * that defaults to using
     * {@link Base64Variants#getDefaultVariant} as the default encoding.
     */
    public byte[] getBinaryValue() throws IOException, JsonParseException
    {
        return getBinaryValue(Base64Variants.getDefaultVariant());
    }

    /*
    /**********************************************************
    /* Public API, access to token information, coercion/conversion
    /**********************************************************
     */
    
    /**
     * Method that will try to convert value of current token to a
     * <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public int getValueAsInt() throws IOException, JsonParseException {
        return getValueAsInt(0);
    }
    
    /**
     * Method that will try to convert value of current token to a
     * <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public int getValueAsInt(int defaultValue) throws IOException, JsonParseException {
        return defaultValue;
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public long getValueAsLong() throws IOException, JsonParseException {
        return getValueAsInt(0);
    }
    
    /**
     * Method that will try to convert value of current token to a
     * <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public long getValueAsLong(long defaultValue) throws IOException, JsonParseException {
        return defaultValue;
    }
    
    /**
     * Method that will try to convert value of current token to a Java
     * <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * default value of <b>0.0</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public double getValueAsDouble() throws IOException, JsonParseException {
        return getValueAsDouble(0.0);
    }
    
    /**
     * Method that will try to convert value of current token to a
     * Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.6
     */
    public double getValueAsDouble(double defaultValue) throws IOException, JsonParseException {
        return defaultValue;
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * default value of <b>false</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.7
     */
    public boolean getValueAsBoolean() throws IOException, JsonParseException {
        return getValueAsBoolean(false);
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
     * 
     * @since 1.7
     */
    public boolean getValueAsBoolean(boolean defaultValue) throws IOException, JsonParseException {
        return defaultValue;
    }
    
    /*
    /**********************************************************
    /* Public API, optional data binding functionality
    /**********************************************************
     */

    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link java.lang.Boolean}).
     * <b>Note</b>: method can only be called if the parser has
     * an object codec assigned; this is true for parsers constructed
     * by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.MappingJsonFactory} but
     * not for {@link JsonFactory} (unless its <code>setCodec</code>
     * method has been explicitly called).
     *<p>
     * This method may advance the event stream, for structured types
     * the current token will be the closing end marker (END_ARRAY,
     * END_OBJECT) of the bound structure. For non-structured Json types
     * (and for {@link JsonToken#VALUE_EMBEDDED_OBJECT})
     * stream is not advanced.
     *<p>
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     */
    public <T> T readValueAs(Class<T> valueType)
        throws IOException, JsonProcessingException
    {
        ObjectCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
        }
        return codec.readValue(this, valueType);
    }

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using so-called
     * "super type token"
     * and specifically needs to be used if the root type is a 
     * parameterized (generic) container type.
     * <b>Note</b>: method can only be called if the parser has
     * an object codec assigned; this is true for parsers constructed
     * by {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.MappingJsonFactory} but
     * not for {@link JsonFactory} (unless its <code>setCodec</code>
     * method has been explicitly called).
     *<p>
     * This method may advance the event stream, for structured types
     * the current token will be the closing end marker (END_ARRAY,
     * END_OBJECT) of the bound structure. For non-structured Json types
     * (and for {@link JsonToken#VALUE_EMBEDDED_OBJECT})
     * stream is not advanced.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValueAs(TypeReference<?> valueTypeRef)
        throws IOException, JsonProcessingException
    {
        ObjectCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
        }
        /* Ugh. Stupid Java type erasure... can't just chain call,s
         * must cast here also.
         */
        return (T) codec.readValue(this, valueTypeRef);
    }

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     * 
     * @since 1.9
     */
    public <T> Iterator<T> readValuesAs(Class<T> valueType)
        throws IOException, JsonProcessingException
    {
        ObjectCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
        }
        return codec.readValues(this, valueType);
    }

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     * 
     * @since 1.9
     */
    public <T> Iterator<T> readValuesAs(TypeReference<?> valueTypeRef)
        throws IOException, JsonProcessingException
    {
        ObjectCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
        }
        return codec.readValues(this, valueTypeRef);
    }
    
    /**
     * Method to deserialize JSON content into equivalent "tree model",
     * represented by root {@link JsonNode} of resulting model.
     * For JSON Arrays it will an array node (with child nodes),
     * for objects object node (with child nodes), and for other types
     * matching leaf node type
     */
    public JsonNode readValueAsTree()
        throws IOException, JsonProcessingException
    {
        ObjectCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into JsonNode tree");
        }
        return codec.readTree(this);
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Helper method for constructing {@link JsonParseException}s
     * based on current state of the parser
     */
    protected JsonParseException _constructError(String msg)
    {
        return new JsonParseException(msg, getCurrentLocation());
    }
}
