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

import com.alibaba.acm.shaded.org.codehaus.jackson.io.CharacterEscapes;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;

/**
 * Base class that defines public API for writing JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonGenerator
    implements Closeable, Versioned
{
    /**
     * Enumeration that defines all togglable features for generators.
     */
    public enum Feature {
        /**
         * Feature that determines whether generator will automatically
         * close underlying output target that is NOT owned by the
         * generator.
         * If disabled, calling application has to separately
         * close the underlying {@link OutputStream} and {@link Writer}
         * instances used to create the generator. If enabled, generator
         * will handle closing, as long as generator itself gets closed:
         * this happens when end-of-input is encountered, or generator
         * is closed by a call to {@link JsonGenerator#close}.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_TARGET(true),

        /**
         * Feature that determines what happens when the generator is
         * closed while there are still unmatched
         * {@link JsonToken#START_ARRAY} or {@link JsonToken#START_OBJECT}
         * entries in output content. If enabled, such Array(s) and/or
         * Object(s) are automatically closed; if disabled, nothing
         * specific is done.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_JSON_CONTENT(true),

        /**
         * Feature that determines whether JSON Object field names are
         * quoted using double-quotes, as specified by JSON specification
         * or not. Ability to disable quoting was added to support use
         * cases where they are not usually expected, which most commonly
         * occurs when used straight from Javascript.
         */
        QUOTE_FIELD_NAMES(true),

        /**
         * Feature that determines whether "exceptional" (not real number)
         * float/double values are output as quoted strings.
         * The values checked are Double.Nan,
         * Double.POSITIVE_INFINITY and Double.NEGATIVE_INIFINTY (and 
         * associated Float values).
         * If feature is disabled, these numbers are still output using
         * associated literal values, resulting in non-conformant
         * output.
         *<p>
         * Feature is enabled by default.
         */
        QUOTE_NON_NUMERIC_NUMBERS(true),

        /**
         * Feature that forces all Java numbers to be written as JSON strings.
         * Default state is 'false', meaning that Java numbers are to
         * be serialized using basic numeric serialization (as JSON
         * numbers, integral or floating point). If enabled, all such
         * numeric values are instead written out as JSON Strings.
         *<p>
         * One use case is to avoid problems with Javascript limitations:
         * since Javascript standard specifies that all number handling
         * should be done using 64-bit IEEE 754 floating point values,
         * result being that some 64-bit integer values can not be
         * accurately represent (as mantissa is only 51 bit wide).
         *<p>
         * Feature is disabled by default.
         *
         * @since 1.3
         */
        WRITE_NUMBERS_AS_STRINGS(false),

        /**
         * Feature that specifies that calls to {@link #flush} will cause
         * matching <code>flush()</code> to underlying {@link OutputStream}
         * or {@link Writer}; if disabled this will not be done.
         * Main reason to disable this feature is to prevent flushing at
         * generator level, if it is not possible to prevent method being
         * called by other code (like <code>ObjectMapper</code> or third
         * party libraries).
         *<p>
         * Feature is enabled by default.
         * 
         * @since 1.7
         */
        FLUSH_PASSED_TO_STREAM(true),
        
        /**
         * Feature that specifies that all characters beyond 7-bit ASCII
         * range (i.e. code points of 128 and above) need to be output
         * using format-specific escapes (for JSON, backslash escapes),
         * if format uses escaping mechanisms (which is generally true
         * for textual formats but not for binary formats).
         * 
         * @since 1.8
         */
        ESCAPE_NON_ASCII(false)
        
            ;

        final boolean _defaultState;

        final int _mask;
        
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
            _mask = (1 << ordinal());
        }
        
        public boolean enabledByDefault() { return _defaultState; }
    
        public int getMask() { return _mask; }
    }

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Object that handles pretty-printing (usually additional
     * white space to make results more human-readable) during
     * output. If null, no pretty-printing is done.
     */
    protected PrettyPrinter _cfgPrettyPrinter;

    /*
    /**********************************************************
    /* Construction, configuration, initialization
    /**********************************************************
     */
    
    protected JsonGenerator() { }

    /**
     * Method to call to make this generator use specified schema.
     * Method must be called before generating any content, right after instance
     * has been created.
     * Note that not all generators support schemas; and those that do usually only
     * accept specific types of schemas: ones defined for data format this generator
     * produces.
     *<p>
     * If generator does not support specified schema, {@link UnsupportedOperationException}
     * is thrown.
     * 
     * @param schema Schema to use
     * 
     * @throws UnsupportedOperationException if generator does not support schema
     * 
     * @since 1.8
     */
    public void setSchema(FormatSchema schema)
    {
        throw new UnsupportedOperationException("Generator of type "+getClass().getName()+" does not support schema of type '"
                +schema.getSchemaType()+"'");
    }
    
    /**
     * Method that can be used to verify that given schema can be used with
     * this generator (using {@link #setSchema}).
     * 
     * @param schema Schema to check
     * 
     * @return True if this generator can use given schema; false if not
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
     * as target for generated output; this is usually either
     * {@link OutputStream} or {@link Writer}, depending on what
     * generator was constructed with.
     * Note that returned value may be null in some cases; including
     * case where implementation does not want to exposed raw
     * source to caller.
     * In cases where output has been decorated, object returned here
     * is the decorated version; this allows some level of interaction
     * between users of generator and decorator object.
     *<p>
     * In general use of this accessor should be considered as
     * "last effort", i.e. only used if no other mechanism is applicable.
     * 
     * @since 1.8
     */
    public Object getOutputTarget() {
        return null;
    }
    
    /*
    /**********************************************************
    /* Public API, configuration
    /**********************************************************
     */

    /**
     * Method for enabling specified parser features:
     * check {@link Feature} for list of available features.
     *
     * @return Generator itself (this), to allow chaining
     *
     * @since 1.2
     */
    public abstract JsonGenerator enable(Feature f);

    /**
     * Method for disabling specified  features
     * (check {@link Feature} for list of features)
     *
     * @return Generator itself (this), to allow chaining
     *
     * @since 1.2
     */
    public abstract JsonGenerator disable(Feature f);

    /**
     * Method for enabling or disabling specified feature:
     * check {@link Feature} for list of available features.
     *
     * @return Generator itself (this), to allow chaining
     *
     * @since 1.2
     */
    public JsonGenerator configure(Feature f, boolean state)
    {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for checking whether given feature is enabled.
     * Check {@link Feature} for list of available features.
     *
     * @since 1.2
     */
    public abstract boolean isEnabled(Feature f);

    /**
     * Method that can be called to set or reset the object to
     * use for writing Java objects as JsonContent
     * (using method {@link #writeObject}).
     *
     * @return Generator itself (this), to allow chaining
     */
    public abstract JsonGenerator setCodec(ObjectCodec oc);

    /**
     * Method for accessing the object used for writing Java
     * object as Json content
     * (using method {@link #writeObject}).
     */
    public abstract ObjectCodec getCodec();

    // // // Older deprecated versions

    /** @deprecated Use {@link #enable} instead
     */
    @Deprecated
    public void enableFeature(Feature f) { enable(f); }

    /** @deprecated Use {@link #disable} instead
     */
    @Deprecated
    public void disableFeature(Feature f) { disable(f); }

    /** @deprecated Use {@link #configure} instead
     */
    @Deprecated
    public void setFeature(Feature f, boolean state) { configure(f, state); }

    /** @deprecated Use {@link #isEnabled} instead
     */
    @Deprecated
    public boolean isFeatureEnabled(Feature f) { return isEnabled(f); }

    /*
    /**********************************************************
    /* Configuring generator
    /**********************************************************
      */

    /**
     * Method for setting a custom pretty printer, which is usually
     * used to add indentation for improved human readability.
     * By default, generator does not do pretty printing.
     *<p>
     * To use the default pretty printer that comes with core
     * Jackson distribution, call {@link #useDefaultPrettyPrinter}
     * instead.
     *
     * @return Generator itself (this), to allow chaining
     */
    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        _cfgPrettyPrinter = pp;
        return this;
    }

    /**
     * Convenience method for enabling pretty-printing using
     * the default pretty printer
     * ({@link com.alibaba.acm.shaded.org.codehaus.jackson.util.DefaultPrettyPrinter}).
     *
     * @return Generator itself (this), to allow chaining
     */
    public abstract JsonGenerator useDefaultPrettyPrinter();

    /**
     * Method that can be called to request that generator escapes
     * all character codes above specified code point (if positive value);
     * or, to not escape any characters except for ones that must be
     * escaped for the data format (if -1).
     * To force escaping of all non-ASCII characters, for example,
     * this method would be called with value of 127.
     *<p>
     * Note that generators are NOT required to support setting of value
     * higher than 127, because there are other ways to affect quoting
     * (or lack thereof) of character codes between 0 and 127.
     * Not all generators support concept of escaping, either; if so,
     * calling this method will have no effect.
     *<p>
     * Default implementation does nothing; sub-classes need to redefine
     * it according to rules of supported data format.
     * 
     * @param charCode Either -1 to indicate that no additional escaping
     *   is to be done; or highest code point not to escape (meaning higher
     *   ones will be), if positive value.
     * 
     * @since 1.8
     */
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        return this;
    }

    /**
     * Accessor method for testing what is the highest unescaped character
     * configured for this generator. This may be either positive value
     * (when escaping configuration has been set and is in effect), or
     * 0 to indicate that no additional escaping is in effect.
     * Some generators may not support additional escaping: for example,
     * generators for binary formats that do not use escaping should
     * simply return 0.
     * 
     * @return Currently active limitation for highest non-escaped character,
     *   if defined; or -1 to indicate no additional escaping is performed.
     */
    public int getHighestEscapedChar() {
        return 0;
    }
    /**
     * Method for accessing custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     * 
     * @since 1.8
     */
    public CharacterEscapes getCharacterEscapes() {
        return null;
    }

    /**
     * Method for defining custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     * 
     * @since 1.8
     */
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
        return this;
    }

    /*
    /**********************************************************
    /* Public API, write methods, structural
    /**********************************************************
     */

    /**
     * Method for writing starting marker of a JSON Array value
     * (character '['; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Array values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     */
    public abstract void writeStartArray()
        throws IOException, JsonGenerationException;

    /**
     * Method for writing closing marker of a JSON Array value
     * (character ']'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Marker can be written if the innermost structured type
     * is Array.
     */
    public abstract void writeEndArray()
        throws IOException, JsonGenerationException;

    /**
     * Method for writing starting marker of a JSON Object value
     * (character '{'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Object values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     */
    public abstract void writeStartObject()
        throws IOException, JsonGenerationException;

    /**
     * Method for writing closing marker of a JSON Object value
     * (character '}'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Marker can be written if the innermost structured type
     * is Object, and the last written event was either a
     * complete value, or START-OBJECT marker (see JSON specification
     * for more details).
     */
    public abstract void writeEndObject()
        throws IOException, JsonGenerationException;

    /**
     * Method for writing a field name (JSON String surrounded by
     * double quotes: syntactically identical to a JSON String value),
     * possibly decorated by white space if pretty-printing is enabled.
     *<p>
     * Field names can only be written in Object context (check out
     * JSON specification for details), when field name is expected
     * (field names alternate with values).
     */
    public abstract void writeFieldName(String name)
        throws IOException, JsonGenerationException;

    /**
     * Method similar to {@link #writeFieldName(String)}, main difference
     * being that it may perform better as some of processing (such as
     * quoting of certain characters, or encoding into external encoding
     * if supported by generator) can be done just once and reused for
     * later calls.
     *<p>
     * Default implementation simple uses unprocessed name container in
     * serialized String; implementations are strongly encouraged to make
     * use of more efficient methods argument object has.
     * 
     * @since 1.6
     */
    public void writeFieldName(SerializedString name)
        throws IOException, JsonGenerationException
    {
        writeFieldName(name.getValue());
    }

    /**
     * Method similar to {@link #writeFieldName(String)}, main difference
     * being that it may perform better as some of processing (such as
     * quoting of certain characters, or encoding into external encoding
     * if supported by generator) can be done just once and reused for
     * later calls.
     *<p>
     * Default implementation simple uses unprocessed name container in
     * serialized String; implementations are strongly encouraged to make
     * use of more efficient methods argument object has.
     * 
     * @since 1.7
     */
    public void writeFieldName(SerializableString name)
        throws IOException, JsonGenerationException
    {
        writeFieldName(name.getValue());
    }

    /*
    /**********************************************************
    /* Public API, write methods, text/String values
    /**********************************************************
     */

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     */
    public abstract void writeString(String text)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     */
    public abstract void writeString(char[] text, int offset, int len)
        throws IOException, JsonGenerationException;

    /**
     * Method similar to {@link #writeString(String)}, but that takes
     * {@link SerializableString} which can make this potentially
     * more efficient to call as generator may be able to reuse
     * quoted and/or encoded representation.
     *<p>
     * Default implementation just calls {@link #writeString(String)};
     * sub-classes should override it with more efficient implementation
     * if possible.
     * 
     * @since 1.7
     */
    public void writeString(SerializableString text)
        throws IOException, JsonGenerationException
    {
        writeString(text.getValue());
    }

    /**
     * Method similar to {@link #writeString(String)} but that takes as
     * its input a UTF-8 encoded String that is to be output as-is, without additional
     * escaping (type of which depends on data format; backslashes for JSON).
     * However, quoting that data format requires (like double-quotes for JSON) will be added
     * around the value if and as necessary.
     *<p>
     * Note that some backends may choose not to support this method: for
     * example, if underlying destination is a {@link java.io.Writer}
     * using this method would require UTF-8 decoding.
     * If so, implementation may instead choose to throw a
     * {@link UnsupportedOperationException} due to ineffectiveness
     * of having to decode input.
     * 
     * @since 1.7
     */
    public abstract void writeRawUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException;

    /**
     * Method similar to {@link #writeString(String)} but that takes as its input
     * a UTF-8 encoded String which has <b>not</b> been escaped using whatever
     * escaping scheme data format requires (for JSON that is backslash-escaping
     * for control characters and double-quotes; for other formats something else).
     * This means that textual JSON backends need to check if value needs
     * JSON escaping, but otherwise can just be copied as is to output.
     * Also, quoting that data format requires (like double-quotes for JSON) will be added
     * around the value if and as necessary.
     *<p>
     * Note that some backends may choose not to support this method: for
     * example, if underlying destination is a {@link java.io.Writer}
     * using this method would require UTF-8 decoding.
     * In this case
     * generator implementation may instead choose to throw a
     * {@link UnsupportedOperationException} due to ineffectiveness
     * of having to decode input.
     * 
     * @since 1.7
     */
    public abstract void writeUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException;
    
    /*
    /**********************************************************
    /* Public API, write methods, binary/raw content
    /**********************************************************
     */
    
    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     */
    public abstract void writeRaw(String text)
        throws IOException, JsonGenerationException;

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     */
    public abstract void writeRaw(String text, int offset, int len)
        throws IOException, JsonGenerationException;

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     */
    public abstract void writeRaw(char[] text, int offset, int len)
        throws IOException, JsonGenerationException;

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     */
    public abstract void writeRaw(char c)
        throws IOException, JsonGenerationException;

    /**
     * Method that will force generator to copy
     * input text verbatim without any modifications, but assuming
     * it must constitute a single legal JSON value (number, string,
     * boolean, null, Array or List). Assuming this, proper separators
     * are added if and as needed (comma or colon), and generator
     * state updated to reflect this.
     */
    public abstract void writeRawValue(String text)
        throws IOException, JsonGenerationException;

    public abstract void writeRawValue(String text, int offset, int len)
        throws IOException, JsonGenerationException;

    public abstract void writeRawValue(char[] text, int offset, int len)
        throws IOException, JsonGenerationException;

    /**
     * Method that will output given chunk of binary data as base64
     * encoded, as a complete String value (surrounded by double quotes).
     * This method defaults
     *<p>
     * Note: because Json Strings can not contain unescaped linefeeds,
     * if linefeeds are included (as per last argument), they must be
     * escaped. This adds overhead for decoding without improving
     * readability.
     * Alternatively if linefeeds are not included,
     * resulting String value may violate the requirement of base64
     * RFC which mandates line-length of 76 characters and use of
     * linefeeds. However, all {@link JsonParser} implementations
     * are required to accept such "long line base64"; as do
     * typical production-level base64 decoders.
     *
     * @param b64variant Base64 variant to use: defines details such as
     *   whether padding is used (and if so, using which character);
     *   what is the maximum line length before adding linefeed,
     *   and also the underlying alphabet to use.
     */
    public abstract void writeBinary(Base64Variant b64variant,
                                     byte[] data, int offset, int len)
        throws IOException, JsonGenerationException;

    /**
     * Similar to {@link #writeBinary(Base64Variant,byte[],int,int)},
     * but default to using the Jackson default Base64 variant 
     * (which is {@link Base64Variants#MIME_NO_LINEFEEDS}).
     */
    public void writeBinary(byte[] data, int offset, int len)
        throws IOException, JsonGenerationException
    {
        writeBinary(Base64Variants.getDefaultVariant(), data, offset, len);
    }

    /**
     * Similar to {@link #writeBinary(Base64Variant,byte[],int,int)},
     * but assumes default to using the Jackson default Base64 variant 
     * (which is {@link Base64Variants#MIME_NO_LINEFEEDS}). Also
     * assumes that whole byte array is to be output.
     */
    public void writeBinary(byte[] data)
        throws IOException, JsonGenerationException
    {
        writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
    }

    /*
    /**********************************************************
    /* Public API, write methods, other value types
    /**********************************************************
     */

    /**
     * Method for outputting given value as Json number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(int v)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting given value as Json number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(long v)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting given value as Json number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(BigInteger v)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting indicate Json numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(double d)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting indicate Json numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(float f)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting indicate Json numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNumber(BigDecimal dec)
        throws IOException, JsonGenerationException;

    /**
     * Write method that can be used for custom numeric types that can
     * not be (easily?) converted to "standard" Java number types.
     * Because numbers are not surrounded by double quotes, regular
     * {@link #writeString} method can not be used; nor
     * {@link #writeRaw} because that does not properly handle
     * value separators needed in Array or Object contexts.
     *<p>
     * Note: because of lack of type safety, some generator
     * implementations may not be able to implement this
     * method. For example, if a binary json format is used,
     * it may require type information for encoding; similarly
     * for generator-wrappers around Java objects or Json nodes.
     * If implementation does not implement this method,
     * it needs to throw {@link UnsupportedOperationException}.
     */
    public abstract void writeNumber(String encodedValue)
        throws IOException, JsonGenerationException,
               UnsupportedOperationException;

    /**
     * Method for outputting literal Json boolean value (one of
     * Strings 'true' and 'false').
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeBoolean(boolean state)
        throws IOException, JsonGenerationException;

    /**
     * Method for outputting literal Json null value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     */
    public abstract void writeNull()
        throws IOException, JsonGenerationException;

    /*
    /**********************************************************
    /* Public API, write methods, serializing Java objects
    /**********************************************************
     */

    /**
     * Method for writing given Java object (POJO) as Json.
     * Exactly how the object gets written depends on object
     * in question (ad on codec, its configuration); for most
     * beans it will result in Json object, but for others Json
     * array, or String or numeric value (and for nulls, Json
     * null literal.
     * <b>NOTE</b>: generator must have its <b>object codec</b>
     * set to non-null value; for generators created by a mapping
     * factory this is the case, for others not.
     */
    public abstract void writeObject(Object pojo)
        throws IOException, JsonProcessingException;

    /**
     * Method for writing given JSON tree (expressed as a tree
     * where given JsonNode is the root) using this generator.
     * This will generally just call
     * {@link #writeObject} with given node, but is added
     * for convenience and to make code more explicit in cases
     * where it deals specifically with trees.
     */
    public abstract void writeTree(JsonNode rootNode)
        throws IOException, JsonProcessingException;

    /*
    /**********************************************************
    /* Public API, convenience field write methods
    /**********************************************************
     */

    /**
     * Convenience method for outputting a field entry ("member")
     * that has a String value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeString(value);
     *</pre>
     *<p>
     * Note: many performance-sensitive implementations override this method
     */
    public void writeStringField(String fieldName, String value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeString(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has a boolean value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeBoolean(value);
     *</pre>
     */
    public final void writeBooleanField(String fieldName, boolean value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has JSON literal value null. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNull();
     *</pre>
     */
    public final void writeNullField(String fieldName)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNull();
    }
    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     */
    public final void writeNumberField(String fieldName, int value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     */
    public final void writeNumberField(String fieldName, long value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     */
    public final void writeNumberField(String fieldName, double value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     */
    public final void writeNumberField(String fieldName, float value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     */
    public final void writeNumberField(String fieldName, BigDecimal value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that contains specified data in base64-encoded form.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeBinary(value);
     *</pre>
     */
    public final void writeBinaryField(String fieldName, byte[] data)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * (that will contain a JSON Array value), and the START_ARRAY marker.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeStartArray();
     *</pre>
     *<p>
     * Note: caller still has to take care to close the array
     * (by calling {#link #writeEndArray}) after writing all values
     * of the value Array.
     */
    public final void writeArrayFieldStart(String fieldName)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeStartArray();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * (that will contain a JSON Object value), and the START_OBJECT marker.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeStartObject();
     *</pre>
     *<p>
     * Note: caller still has to take care to close the Object
     * (by calling {#link #writeEndObject}) after writing all
     * entries of the value Object.
     */
    public final void writeObjectFieldStart(String fieldName)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeStartObject();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has contents of specific Java object as its value.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeObject(pojo);
     *</pre>
     */
    public final void writeObjectField(String fieldName, Object pojo)
        throws IOException, JsonProcessingException
    {
        writeFieldName(fieldName);
        writeObject(pojo);
    }

    /*
    /**********************************************************
    /* Public API, copy-through methods
    /**********************************************************
     */

    /**
     * Method for copying contents of the current event that
     * the given parser instance points to.
     * Note that the method <b>will not</b> copy any other events,
     * such as events contained within Json Array or Object structures.
     *<p>
     * Calling this method will not advance the given
     * parser, although it may cause parser to internally process
     * more data (if it lazy loads contents of value events, for example)
     */
    public abstract void copyCurrentEvent(JsonParser jp)
        throws IOException, JsonProcessingException;

    /**
     * Method for copying contents of the current event
     * <b>and following events that it encloses</b>
     * the given parser instance points to.
     *<p>
     * So what constitutes enclosing? Here is the list of
     * events that have associated enclosed events that will
     * get copied:
     *<ul>
     * <li>{@link JsonToken#START_OBJECT}:
     *   all events up to and including matching (closing)
     *   {@link JsonToken#END_OBJECT} will be copied
     *  </li>
     * <li>{@link JsonToken#START_ARRAY}
     *   all events up to and including matching (closing)
     *   {@link JsonToken#END_ARRAY} will be copied
     *  </li>
     * <li>{@link JsonToken#FIELD_NAME} the logical value (which
     *   can consist of a single scalar value; or a sequence of related
     *   events for structured types (Json Arrays, Objects)) will
     *   be copied along with the name itself. So essentially the
     *   whole <b>field entry</b> (name and value) will be copied.
     *  </li>
     *</ul>
     *<p>
     * After calling this method, parser will point to the
     * <b>last event</b> that was copied. This will either be
     * the event parser already pointed to (if there were no
     * enclosed events), or the last enclosed event copied.
     */
    public abstract void copyCurrentStructure(JsonParser jp)
        throws IOException, JsonProcessingException;

    /*
    /**********************************************************
    /* Public API, context access
    /**********************************************************
     */

    /**
     * @return Context object that can give information about logical
     *   position within generated json content.
     */
    public abstract JsonStreamContext getOutputContext();

    /*
    /**********************************************************
    /* Public API, buffer handling
    /**********************************************************
     */

    /**
     * Method called to flush any buffered content to the underlying
     * target (output stream, writer), and to flush the target itself
     * as well.
     */
    public abstract void flush() throws IOException;

    /**
     * Method that can be called to determine whether this generator
     * is closed or not. If it is closed, no more output can be done.
     */
    public abstract boolean isClosed();

    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    /**
     * Method called to close this generator, so that no more content
     * can be written.
     *<p>
     * Whether the underlying target (stream, writer) gets closed depends
     * on whether this generator either manages the target (i.e. is the
     * only one with access to the target -- case if caller passes a
     * reference to the resource such as File, but not stream); or
     * has feature {@link Feature#AUTO_CLOSE_TARGET} enabled.
     * If either of above is true, the target is also closed. Otherwise
     * (not managing, feature not enabled), target is not closed.
     */
    @Override
    public abstract void close()
        throws IOException;
}
