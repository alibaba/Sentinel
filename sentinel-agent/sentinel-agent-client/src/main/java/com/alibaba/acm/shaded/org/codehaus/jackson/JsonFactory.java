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
import java.lang.ref.SoftReference;
import java.net.URL;

import com.alibaba.acm.shaded.org.codehaus.jackson.format.InputAccessor;
import com.alibaba.acm.shaded.org.codehaus.jackson.format.MatchStrength;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.ByteSourceBootstrapper;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.ReaderBasedParser;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.Utf8Generator;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.WriterBasedGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.BufferRecycler;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.VersionUtil;

/**
 * The main factory class of Jackson package, used to configure and
 * construct reader (aka parser, {@link JsonParser})
 * and writer (aka generator, {@link JsonGenerator})
 * instances.
 *<p>
 * Factory instances are thread-safe and reusable after configuration
 * (if any). Typically applications and services use only a single
 * globally shared factory instance, unless they need differently
 * configured factories. Factory reuse is important if efficiency matters;
 * most recycling of expensive construct is done on per-factory basis.
 *<p>
 * Creation of a factory instance is a light-weight operation,
 * and since there is no need for pluggable alternative implementations
 * (as there is no "standard" JSON processor API to implement),
 * the default constructor is used for constructing factory
 * instances.
 *
 * @author Tatu Saloranta
 */
public class JsonFactory implements Versioned
{
    /**
     * Name used to identify JSON format
     * (and returned by {@link #getFormatName()}
     */
    public final static String FORMAT_NAME_JSON = "JSON";
    
    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    final static int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    final static int DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();

    /*
    /**********************************************************
    /* Buffer, symbol table management
    /**********************************************************
     */

    /**
     * This <code>ThreadLocal</code> contains a {@link java.lang.ref.SoftRerefence}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling between reader and writer instances.
     */
    final protected static ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef
        = new ThreadLocal<SoftReference<BufferRecycler>>();

    /**
     * Each factory comes equipped with a shared root symbol table.
     * It should not be linked back to the original blueprint, to
     * avoid contents from leaking between factories.
     */
    protected CharsToNameCanonicalizer _rootCharSymbols = CharsToNameCanonicalizer.createRoot();

    /**
     * Alternative to the basic symbol table, some stream-based
     * parsers use different name canonicalization method.
     *<p>
     * TODO: should clean up this; looks messy having 2 alternatives
     * with not very clear differences.
     */
    protected BytesToNameCanonicalizer _rootByteSymbols = BytesToNameCanonicalizer.createRoot();

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Object that implements conversion functionality between
     * Java objects and JSON content. For base JsonFactory implementation
     * usually not set by default, but can be explicitly set.
     * Sub-classes (like @link com.alibaba.acm.shaded.org.codehaus.jackson.map.MappingJsonFactory}
     * usually provide an implementation.
     */
    protected ObjectCodec _objectCodec;

    /**
     * Currently enabled parser features.
     */
    protected int _parserFeatures = DEFAULT_PARSER_FEATURE_FLAGS;

    /**
     * Currently enabled generator features.
     */
    protected int _generatorFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;

    /**
     * Definition of custom character escapes to use for generators created
     * by this factory, if any. If null, standard data format specific
     * escapes are used.
     * 
     * @since 1.8
     */
    protected CharacterEscapes _characterEscapes;

    /**
     * Optional helper object that may decorate input sources, to do
     * additional processing on input during parsing.
     * 
     * @since 1.8
     */
    protected InputDecorator _inputDecorator;

    /**
     * Optional helper object that may decorate output object, to do
     * additional processing on output during content generation.
     * 
     * @since 1.8
     */
    protected OutputDecorator _outputDecorator;
    
    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */
    
    /**
     * Default constructor used to create factory instances.
     * Creation of a factory instance is a light-weight operation,
     * but it is still a good idea to reuse limited number of
     * factory instances (and quite often just a single instance):
     * factories are used as context for storing some reused
     * processing objects (such as symbol tables parsers use)
     * and this reuse only works within context of a single
     * factory instance.
     */
    public JsonFactory() { this(null); }

    public JsonFactory(ObjectCodec oc) { _objectCodec = oc; }

    /*
    /**********************************************************
    /* Format detection functionality (since 1.8)
    /**********************************************************
     */

    /**
     * Method that returns short textual id identifying format
     * this factory supports.
     *<p>
     * Note: sub-classes should override this method; default
     * implementation will return null for all sub-classes
     * 
     * @since 1.8
     */
    public String getFormatName()
    {
        /* Somewhat nasty check: since we can't make this abstract
         * (due to backwards compatibility concerns), need to prevent
         * format name "leakage"
         */
        if (getClass() == JsonFactory.class) {
            return FORMAT_NAME_JSON;
        }
        return null;
    }

    public MatchStrength hasFormat(InputAccessor acc) throws IOException
    {
        // since we can't keep this abstract, only implement for "vanilla" instance
        if (getClass() == JsonFactory.class) {
            return hasJSONFormat(acc);
        }
        return null;
    }

    protected MatchStrength hasJSONFormat(InputAccessor acc) throws IOException
    {
        return ByteSourceBootstrapper.hasJSONFormat(acc);
    }    
    
    /*
    /**********************************************************
    /* Versioned
    /**********************************************************
     */

    @Override
    public Version version() {
        // VERSION is included under impl, so can't pass this class:
        return VersionUtil.versionFor(Utf8Generator.class);
    }

    /*
    /**********************************************************
    /* Configuration, parser settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified parser feature
     * (check {@link JsonParser.Feature} for list of features)
     *
     * @since 1.2
     */
    public final JsonFactory configure(JsonParser.Feature f, boolean state)
    {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for enabling specified parser feature
     * (check {@link JsonParser.Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonFactory enable(JsonParser.Feature f) {
        _parserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified parser features
     * (check {@link JsonParser.Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonFactory disable(JsonParser.Feature f) {
        _parserFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checked whether specified parser feature is enabled.
     *
     * @since 1.2
     */
    public final boolean isEnabled(JsonParser.Feature f) {
        return (_parserFeatures & f.getMask()) != 0;
    }

    // // // Older deprecated (as of 1.2) methods

    /**
     * @deprecated Use {@link #enable(JsonParser.Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public final void enableParserFeature(JsonParser.Feature f) {
        enable(f);
    }

    /**
     * @deprecated Use {@link #disable(JsonParser.Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public final void disableParserFeature(JsonParser.Feature f) {
        disable(f);
    }

    /**
     * @deprecated Use {@link #configure(JsonParser.Feature, boolean)} instead
     */
    @SuppressWarnings("dep-ann")
    public final void setParserFeature(JsonParser.Feature f, boolean state) {
        configure(f, state);
    }

    /**
     * @deprecated Use {@link #isEnabled(JsonParser.Feature)} instead
     */
    @SuppressWarnings("dep-ann")
    public final boolean isParserFeatureEnabled(JsonParser.Feature f) {
        return (_parserFeatures & f.getMask()) != 0;
    }

    /**
     * Method for getting currently configured input decorator (if any;
     * there is no default decorator).
     * 
     * @since 1.8
     */
    public InputDecorator getInputDecorator() {
        return _inputDecorator;
    }

    /**
     * Method for overriding currently configured input decorator
     * 
     * @since 1.8
     */
    public JsonFactory setInputDecorator(InputDecorator d) {
        _inputDecorator = d;
        return this;
    }
    
    /*
    /**********************************************************
    /* Configuration, generator settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified generator feature
     * (check {@link JsonGenerator.Feature} for list of features)
     *
     * @since 1.2
     */
    public final JsonFactory configure(JsonGenerator.Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }


    /**
     * Method for enabling specified generator features
     * (check {@link JsonGenerator.Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonFactory enable(JsonGenerator.Feature f) {
        _generatorFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified generator feature
     * (check {@link JsonGenerator.Feature} for list of features)
     *
     * @since 1.2
     */
    public JsonFactory disable(JsonGenerator.Feature f) {
        _generatorFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Check whether specified generator feature is enabled.
     *
     * @since 1.2
     */
    public final boolean isEnabled(JsonGenerator.Feature f) {
        return (_generatorFeatures & f.getMask()) != 0;
    }

    // // // Older deprecated (as of 1.2) methods

    /**
     * @deprecated Use {@link #enable(JsonGenerator.Feature)} instead
     */
    @Deprecated
    public final void enableGeneratorFeature(JsonGenerator.Feature f) {
        enable(f);
    }

    /**
     * @deprecated Use {@link #disable(JsonGenerator.Feature)} instead
     */
    @Deprecated
    public final void disableGeneratorFeature(JsonGenerator.Feature f) {
        disable(f);
    }

    /**
     * @deprecated Use {@link #configure(JsonGenerator.Feature, boolean)} instead
     */
    @Deprecated
    public final void setGeneratorFeature(JsonGenerator.Feature f, boolean state) {
        configure(f, state);
    }

    /**
     * @deprecated Use {@link #isEnabled(JsonGenerator.Feature)} instead
     */
    @Deprecated
    public final boolean isGeneratorFeatureEnabled(JsonGenerator.Feature f) {
        return isEnabled(f);
    }

    /**
     * Method for accessing custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     * 
     * @since 1.8
     */
    public CharacterEscapes getCharacterEscapes() {
        return _characterEscapes;
    }

    /**
     * Method for defining custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     * 
     * @since 1.8
     */
    public JsonFactory setCharacterEscapes(CharacterEscapes esc) {
        _characterEscapes = esc;
        return this;
    }

    /**
     * Method for getting currently configured output decorator (if any;
     * there is no default decorator).
     * 
     * @since 1.8
     */
    public OutputDecorator getOutputDecorator() {
        return _outputDecorator;
    }

    /**
     * Method for overriding currently configured output decorator
     * 
     * @since 1.8
     */
    public JsonFactory setOutputDecorator(OutputDecorator d) {
        _outputDecorator = d;
        return this;
    }
    
    /*
    /**********************************************************
    /* Configuration, other
    /**********************************************************
     */

    /**
     * Method for associating a {@link ObjectCodec} (typically
     * a {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ObjectMapper}) with
     * this factory (and more importantly, parsers and generators
     * it constructs). This is needed to use data-binding methods
     * of {@link JsonParser} and {@link JsonGenerator} instances.
     */
    public JsonFactory setCodec(ObjectCodec oc) {
        _objectCodec = oc;
        return this;
    }

    public ObjectCodec getCodec() { return _objectCodec; }

    /*
    /**********************************************************
    /* Reader factories
    /**********************************************************
     */

    /**
     * Method for constructing JSON parser instance to parse
     * contents of specified file. Encoding is auto-detected
     * from contents according to JSON specification recommended
     * mechanism.
     *<p>
     * Underlying input stream (needed for reading contents)
     * will be <b>owned</b> (and managed, i.e. closed as need be) by
     * the parser, since caller has no access to it.
     *
     * @param f File that contains JSON content to parse
     */
    public JsonParser createJsonParser(File f)
        throws IOException, JsonParseException
    {
        // true, since we create InputStream from File
        IOContext ctxt = _createContext(f, true);
        InputStream in = new FileInputStream(f);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createJsonParser(in, ctxt);
    }

    /**
     * Method for constructing JSON parser instance to parse
     * contents of resource reference by given URL.
     * Encoding is auto-detected
     * from contents according to JSON specification recommended
     * mechanism.
     *<p>
     * Underlying input stream (needed for reading contents)
     * will be <b>owned</b> (and managed, i.e. closed as need be) by
     * the parser, since caller has no access to it.
     *
     * @param url URL pointing to resource that contains JSON content to parse
     */
    public JsonParser createJsonParser(URL url)
        throws IOException, JsonParseException
    {
        // true, since we create InputStream from URL
        IOContext ctxt = _createContext(url, true);
        InputStream in = _optimizedStreamFromURL(url);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createJsonParser(in, ctxt);
    }

    /**
     * Method for constructing JSON parser instance to parse
     * the contents accessed via specified input stream.
     *<p>
     * The input stream will <b>not be owned</b> by
     * the parser, it will still be managed (i.e. closed if
     * end-of-stream is reacher, or parser close method called)
     * if (and only if) {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser.Feature#AUTO_CLOSE_SOURCE}
     * is enabled.
     *<p>
     * Note: no encoding argument is taken since it can always be
     * auto-detected as suggested by Json RFC.
     *
     * @param in InputStream to use for reading JSON content to parse
     */
    public JsonParser createJsonParser(InputStream in)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(in, false);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createJsonParser(in, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * the contents accessed via specified Reader.
     <p>
     * The read stream will <b>not be owned</b> by
     * the parser, it will still be managed (i.e. closed if
     * end-of-stream is reacher, or parser close method called)
     * if (and only if) {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser.Feature#AUTO_CLOSE_SOURCE}
     * is enabled.
     *<p>
     *
     * @param r Reader to use for reading JSON content to parse
     */
    public JsonParser createJsonParser(Reader r)
        throws IOException, JsonParseException
    {
        // false -> we do NOT own Reader (did not create it)
        IOContext ctxt = _createContext(r, false);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            r = _inputDecorator.decorate(ctxt, r);
        }
	return _createJsonParser(r, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * the contents of given byte array.
     */
    public JsonParser createJsonParser(byte[] data)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(data, true);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, 0, data.length);
            if (in != null) {
                return _createJsonParser(in, ctxt);
            }
        }
        return _createJsonParser(data, 0, data.length, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * the contents of given byte array.
     * 
     * @param data Buffer that contains data to parse
     * @param offset Offset of the first data byte within buffer
     * @param len Length of contents to parse within buffer
     */
    public JsonParser createJsonParser(byte[] data, int offset, int len)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(data, true);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, offset, len);
            if (in != null) {
                return _createJsonParser(in, ctxt);
            }
        }
	return _createJsonParser(data, offset, len, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * contens of given String.
     */
    public JsonParser createJsonParser(String content)
        throws IOException, JsonParseException
    {
	Reader r = new StringReader(content);
        // true -> we own the Reader (and must close); not a big deal
        IOContext ctxt = _createContext(r, true);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            r = _inputDecorator.decorate(ctxt, r);
        }
	return _createJsonParser(r, ctxt);
    }

    /*
    /**********************************************************
    /* Generator factories
    /**********************************************************
     */

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified output stream.
     * Encoding to use must be specified, and needs to be one of available
     * types (as per JSON specification).
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the output stream when
     * {@link JsonGenerator#close} is called (unless auto-closing
     * feature,
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator.Feature#AUTO_CLOSE_TARGET}
     * is enabled).
     * Using application needs to close it explicitly if this is the case.
     *<p>
     * Note: there are formats that use fixed encoding (like most binary data formats)
     * and that ignore passed in encoding.
     *
     * @param out OutputStream to use for writing JSON content 
     * @param enc Character encoding to use
     */
    public JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc)
        throws IOException
    {
	// false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(out, false);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            // [JACKSON-512]: allow wrapping with _outputDecorator
            if (_outputDecorator != null) {
                out = _outputDecorator.decorate(ctxt, out);
            }
            return _createUTF8JsonGenerator(out, ctxt);
        }
        Writer w = _createWriter(out, enc, ctxt);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            w = _outputDecorator.decorate(ctxt, w);
        }
	return _createJsonGenerator(w, ctxt);
    }

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified Writer.
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the Reader when
     * {@link JsonGenerator#close} is called (unless auto-closing
     * feature,
     * {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator.Feature#AUTO_CLOSE_TARGET} is enabled).
     * Using application needs to close it explicitly.
     *
     * @param out Writer to use for writing JSON content 
     */
    public JsonGenerator createJsonGenerator(Writer out)
        throws IOException
    {
        IOContext ctxt = _createContext(out, false);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            out = _outputDecorator.decorate(ctxt, out);
        }
	return _createJsonGenerator(out, ctxt);
    }

    /**
     * Convenience method for constructing generator that uses default
     * encoding of the format (UTF-8 for JSON and most other data formats).
     *<p>
     * Note: there are formats that use fixed encoding (like most binary data formats).
     * 
     * @since 1.8
     */
    public JsonGenerator createJsonGenerator(OutputStream out) throws IOException {
        return createJsonGenerator(out, JsonEncoding.UTF8);
    }
    
    /**
     * Method for constructing JSON generator for writing JSON content
     * to specified file, overwriting contents it might have (or creating
     * it if such file does not yet exist).
     * Encoding to use must be specified, and needs to be one of available
     * types (as per JSON specification).
     *<p>
     * Underlying stream <b>is owned</b> by the generator constructed,
     * i.e. generator will handle closing of file when
     * {@link JsonGenerator#close} is called.
     *
     * @param f File to write contents to
     * @param enc Character encoding to use
     */
    public JsonGenerator createJsonGenerator(File f, JsonEncoding enc)
        throws IOException
    {
	OutputStream out = new FileOutputStream(f);
	// true -> yes, we have to manage the stream since we created it
        IOContext ctxt = _createContext(out, true);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            // [JACKSON-512]: allow wrapping with _outputDecorator
            if (_outputDecorator != null) {
                out = _outputDecorator.decorate(ctxt, out);
            }
            return _createUTF8JsonGenerator(out, ctxt);
        }
        Writer w = _createWriter(out, enc, ctxt);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            w = _outputDecorator.decorate(ctxt, w);
        }
	return _createJsonGenerator(w, ctxt);
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating parser instances,
    /* overridable by sub-classes
    /**********************************************************
     */

    /**
     * Overridable factory method that actually instantiates desired parser
     * given {@link InputStream} and context object.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     */
    protected JsonParser _createJsonParser(InputStream in, IOContext ctxt)
        throws IOException, JsonParseException
    {
        return new ByteSourceBootstrapper(ctxt, in).constructParser(_parserFeatures,
                _objectCodec, _rootByteSymbols, _rootCharSymbols);
    }

    /**
     * Overridable factory method that actually instantiates parser
     * using given {@link Reader} object for reading content.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     */
    protected JsonParser _createJsonParser(Reader r, IOContext ctxt)
	throws IOException, JsonParseException
    {
        return new ReaderBasedParser(ctxt, _parserFeatures, r, _objectCodec,
                _rootCharSymbols.makeChild(isEnabled(JsonParser.Feature.CANONICALIZE_FIELD_NAMES),
                    isEnabled(JsonParser.Feature.INTERN_FIELD_NAMES)));
    }

    /**
     * Overridable factory method that actually instantiates parser
     * using given {@link Reader} object for reading content
     * passed as raw byte array.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     */
    protected JsonParser _createJsonParser(byte[] data, int offset, int len, IOContext ctxt)
        throws IOException, JsonParseException
    {
        return new ByteSourceBootstrapper(ctxt, data, offset, len).constructParser(_parserFeatures,
                _objectCodec, _rootByteSymbols, _rootCharSymbols);
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating generator instances,
    /* overridable by sub-classes
    /**********************************************************
     */
    
    /**
     * Overridable factory method that actually instantiates generator for
     * given {@link Writer} and context object.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     */
    protected JsonGenerator _createJsonGenerator(Writer out, IOContext ctxt)
        throws IOException
    {
        WriterBasedGenerator gen = new WriterBasedGenerator(ctxt, _generatorFeatures, _objectCodec, out);
        if (_characterEscapes != null) {
            gen.setCharacterEscapes(_characterEscapes);
        }
        return gen;
    }

    /**
     * Overridable factory method that actually instantiates generator for
     * given {@link OutputStream} and context object, using UTF-8 encoding.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     */
    protected JsonGenerator _createUTF8JsonGenerator(OutputStream out, IOContext ctxt)
        throws IOException
    {
        Utf8Generator gen = new Utf8Generator(ctxt, _generatorFeatures, _objectCodec, out);
        if (_characterEscapes != null) {
            gen.setCharacterEscapes(_characterEscapes);
        }
        return gen;
    }

    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException
    {
        // note: this should not get called any more (caller checks, dispatches)
        if (enc == JsonEncoding.UTF8) { // We have optimized writer for UTF-8
            return new UTF8Writer(ctxt, out);
        }
        // not optimal, but should do unless we really care about UTF-16/32 encoding speed
        return new OutputStreamWriter(out, enc.getJavaName());
    }
    
    /*
    /**********************************************************
    /* Internal factory methods, other
    /**********************************************************
     */

    /**
     * Overridable factory method that actually instantiates desired
     * context object.
     */
    protected IOContext _createContext(Object srcRef, boolean resourceManaged)
    {
        return new IOContext(_getBufferRecycler(), srcRef, resourceManaged);
    }

    /**
     * Method used by factory to create buffer recycler instances
     * for parsers and generators.
     *<p>
     * Note: only public to give access for <code>ObjectMapper</code>
     */
    public BufferRecycler _getBufferRecycler()
    {
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();

        if (br == null) {
            br = new BufferRecycler();
            _recyclerRef.set(new SoftReference<BufferRecycler>(br));
        }
        return br;
    }
    
    /**
     * Helper methods used for constructing an optimal stream for
     * parsers to use, when input is to be read from an URL.
     * This helps when reading file content via URL.
     */
    protected InputStream _optimizedStreamFromURL(URL url)
        throws IOException
    {
        if ("file".equals(url.getProtocol())) {
            /* Can not do this if the path refers
             * to a network drive on windows. This fixes the problem;
             * might not be needed on all platforms (NFS?), but should not
             * matter a lot: performance penalty of extra wrapping is more
             * relevant when accessing local file system.
             */
            String host = url.getHost();
            if (host == null || host.length() == 0) {
                return new FileInputStream(url.getPath());
            }
        }
        return url.openStream();
    }
}
