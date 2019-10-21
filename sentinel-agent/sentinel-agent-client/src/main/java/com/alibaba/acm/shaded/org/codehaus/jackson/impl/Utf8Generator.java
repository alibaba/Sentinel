package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.CharacterEscapes;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.IOContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.NumberOutput;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.CharTypes;

public class Utf8Generator
    extends JsonGeneratorBase
{
    private final static byte BYTE_u = (byte) 'u';

    private final static byte BYTE_0 = (byte) '0';
    
    private final static byte BYTE_LBRACKET = (byte) '[';
    private final static byte BYTE_RBRACKET = (byte) ']';
    private final static byte BYTE_LCURLY = (byte) '{';
    private final static byte BYTE_RCURLY = (byte) '}';
 
    private final static byte BYTE_BACKSLASH = (byte) '\\';
    private final static byte BYTE_SPACE = (byte) ' ';
    private final static byte BYTE_COMMA = (byte) ',';
    private final static byte BYTE_COLON = (byte) ':';
    private final static byte BYTE_QUOTE = (byte) '"';

    protected final static int SURR1_FIRST = 0xD800;
    protected final static int SURR1_LAST = 0xDBFF;
    protected final static int SURR2_FIRST = 0xDC00;
    protected final static int SURR2_LAST = 0xDFFF;

    // intermediate copies only made up to certain length...
    private final static int MAX_BYTES_TO_BUFFER = 512;
    
    final static byte[] HEX_CHARS = CharTypes.copyHexBytes();

    private final static byte[] NULL_BYTES = { 'n', 'u', 'l', 'l' };
    private final static byte[] TRUE_BYTES = { 't', 'r', 'u', 'e' };
    private final static byte[] FALSE_BYTES = { 'f', 'a', 'l', 's', 'e' };

    /**
     * This is the default set of escape codes, over 7-bit ASCII range
     * (first 128 character codes), used for single-byte UTF-8 characters.
     */
    protected final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
    
    /*
    /**********************************************************
    /* Configuration, basic I/O
    /**********************************************************
     */

    final protected IOContext _ioContext;

    /**
     * Underlying output stream used for writing JSON content.
     */
    final protected OutputStream _outputStream;

    /*
    /**********************************************************
    /* Configuration, output escaping
    /**********************************************************
     */

    /**
     * Currently active set of output escape code definitions (whether
     * and how to escape or not) for 7-bit ASCII range (first 128
     * character codes). Defined separately to make potentially
     * customizable
     */
    protected int[] _outputEscapes = sOutputEscapes;

    /**
     * Value between 128 (0x80) and 65535 (0xFFFF) that indicates highest
     * Unicode code point that will not need escaping; or 0 to indicate
     * that all characters can be represented without escaping.
     * Typically used to force escaping of some portion of character set;
     * for example to always escape non-ASCII characters (if value was 127).
     *<p>
     * NOTE: not all sub-classes make use of this setting.
     */
    protected int _maximumNonEscapedChar;

    /**
     * Definition of custom character escapes to use for generators created
     * by this factory, if any. If null, standard data format specific
     * escapes are used.
     * 
     * @since 1.8
     */
    protected CharacterEscapes _characterEscapes;
    
    /*
    /**********************************************************
    /* Output buffering
    /**********************************************************
     */

    /**
     * Intermediate buffer in which contents are buffered before
     * being written using {@link #_outputStream}.
     */
    protected byte[] _outputBuffer;

    /**
     * Pointer to the position right beyond the last character to output
     * (end marker; may be past the buffer)
     */
    protected int _outputTail = 0;

    /**
     * End marker of the output buffer; one past the last valid position
     * within the buffer.
     */
    protected final int _outputEnd;

    /**
     * Maximum number of <code>char</code>s that we know will always fit
     * in the output buffer after escaping
     */
    protected final int _outputMaxContiguous;
    
    /**
     * Intermediate buffer in which characters of a String are copied
     * before being encoded.
     */
    protected char[] _charBuffer;
    
    /**
     * Length of <code>_charBuffer</code>
     */
    protected final int _charBufferLength;
    
    /**
     * 6 character temporary buffer allocated if needed, for constructing
     * escape sequences
     */
    protected byte[] _entityBuffer;

    /**
     * Flag that indicates whether the output buffer is recycable (and
     * needs to be returned to recycler once we are done) or not.
     */
    protected boolean _bufferRecyclable;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public Utf8Generator(IOContext ctxt, int features, ObjectCodec codec,
            OutputStream out)
    {
        
        super(features, codec);
        _ioContext = ctxt;
        _outputStream = out;
        _bufferRecyclable = true;
        _outputBuffer = ctxt.allocWriteEncodingBuffer();
        _outputEnd = _outputBuffer.length;
        /* To be exact, each char can take up to 6 bytes when escaped (Unicode
         * escape with backslash, 'u' and 4 hex digits); but to avoid fluctuation,
         * we will actually round down to only do up to 1/8 number of chars
         */
        _outputMaxContiguous = _outputEnd >> 3;
        _charBuffer = ctxt.allocConcatBuffer();
        _charBufferLength = _charBuffer.length;

        // By default we use this feature to determine additional quoting
        if (isEnabled(Feature.ESCAPE_NON_ASCII)) {
            setHighestNonEscapedChar(127);
        }
    }

    public Utf8Generator(IOContext ctxt, int features, ObjectCodec codec,
            OutputStream out, byte[] outputBuffer, int outputOffset, boolean bufferRecyclable)
    {
        
        super(features, codec);
        _ioContext = ctxt;
        _outputStream = out;
        _bufferRecyclable = bufferRecyclable;
        _outputTail = outputOffset;
        _outputBuffer = outputBuffer;
        _outputEnd = _outputBuffer.length;
        // up to 6 bytes per char (see above), rounded up to 1/8
        _outputMaxContiguous = _outputEnd >> 3;
        _charBuffer = ctxt.allocConcatBuffer();
        _charBufferLength = _charBuffer.length;

        if (isEnabled(Feature.ESCAPE_NON_ASCII)) {
            setHighestNonEscapedChar(127);
        }
    }

    /*
    /**********************************************************
    /* Overridden configuration methods
    /**********************************************************
     */
    
    @Override
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        _maximumNonEscapedChar = (charCode < 0) ? 0 : charCode;
        return this;
    }

    @Override
    public int getHighestEscapedChar() {
        return _maximumNonEscapedChar;
    }

    @Override
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc)
    {
        _characterEscapes = esc;
        if (esc == null) { // revert to standard escapes
            _outputEscapes = sOutputEscapes;
        } else {
            _outputEscapes = esc.getEscapeCodesForAscii();
        }
        return this;
    }

    /**
     * Method for accessing custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     * 
     * @since 1.8
     */
    @Override
    public CharacterEscapes getCharacterEscapes() {
        return _characterEscapes;
    }

    @Override
    public Object getOutputTarget() {
        return _outputStream;
    }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    /* Most overrides in this section are just to make methods final,
     * to allow better inlining...
     */
    @Override
    public final void writeStringField(String fieldName, String value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeString(value);
    }

    @Override
    public final void writeFieldName(String name)  throws IOException, JsonGenerationException
    {
        int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
            return;
        }
        if (status == JsonWriteContext.STATUS_OK_AFTER_COMMA) { // need comma
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_COMMA;
        }
        _writeFieldName(name);
    }
    
    @Override
    public final void writeFieldName(SerializedString name)
        throws IOException, JsonGenerationException
    {
        // Object is a value, need to verify it's allowed
        int status = _writeContext.writeFieldName(name.getValue());
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
            return;
        }
        if (status == JsonWriteContext.STATUS_OK_AFTER_COMMA) {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_COMMA;
        }
        _writeFieldName(name);
    }

    @Override
    public final void writeFieldName(SerializableString name)
        throws IOException, JsonGenerationException
    {
        // Object is a value, need to verify it's allowed
        int status = _writeContext.writeFieldName(name.getValue());
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
            return;
        }
        if (status == JsonWriteContext.STATUS_OK_AFTER_COMMA) {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_COMMA;
        }
        _writeFieldName(name);
    }

    /*
    /**********************************************************
    /* Output method implementations, structural
    /**********************************************************
     */

    @Override
    public final void writeStartArray() throws IOException, JsonGenerationException
    {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_LBRACKET;
        }
    }

    @Override
    public final void writeEndArray() throws IOException, JsonGenerationException
    {
        if (!_writeContext.inArray()) {
            _reportError("Current context not an ARRAY but "+_writeContext.getTypeDesc());
        }
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndArray(this, _writeContext.getEntryCount());
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_RBRACKET;
        }
        _writeContext = _writeContext.getParent();
    }

    @Override
    public final void writeStartObject() throws IOException, JsonGenerationException
    {
        _verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartObject(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_LCURLY;
        }
    }

    @Override
    public final void writeEndObject() throws IOException, JsonGenerationException
    {
        if (!_writeContext.inObject()) {
            _reportError("Current context not an object but "+_writeContext.getTypeDesc());
        }
        _writeContext = _writeContext.getParent();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndObject(this, _writeContext.getEntryCount());
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_RCURLY;
        }
    }

    protected final void _writeFieldName(String name)
        throws IOException, JsonGenerationException
    {
        /* To support [JACKSON-46], we'll do this:
         * (Question: should quoting of spaces (etc) still be enabled?)
         */
        if (!isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            _writeStringSegments(name);
            return;
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        // The beef:
        final int len = name.length();
        if (len <= _charBufferLength) { // yes, fits right in
            name.getChars(0, len, _charBuffer, 0);
            // But as one segment, or multiple?
            if (len <= _outputMaxContiguous) {
                if ((_outputTail + len) > _outputEnd) { // caller must ensure enough space
                    _flushBuffer();
                }
                _writeStringSegment(_charBuffer, 0, len);
            } else {
                _writeStringSegments(_charBuffer, 0, len);
            }
        } else {
            _writeStringSegments(name);
        }

        // and closing quotes; need room for one more char:
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    protected final void _writeFieldName(SerializableString name)
        throws IOException, JsonGenerationException
    {
        byte[] raw = name.asQuotedUTF8();
        if (!isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            _writeBytes(raw);
            return;
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;

        // Can do it all in buffer?
        final int len = raw.length;
        if ((_outputTail + len + 1) < _outputEnd) { // yup
            System.arraycopy(raw, 0, _outputBuffer, _outputTail, len);
            _outputTail += len;
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
        } else {
            _writeBytes(raw);
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
        }
    }    
    
    /**
     * Specialized version of <code>_writeFieldName</code>, off-lined
     * to keep the "fast path" as simple (and hopefully fast) as possible.
     */
    protected final void _writePPFieldName(String name, boolean commaBefore)
        throws IOException, JsonGenerationException
    {
        if (commaBefore) {
            _cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            _cfgPrettyPrinter.beforeObjectEntries(this);
        }

        if (isEnabled(Feature.QUOTE_FIELD_NAMES)) { // standard
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
            final int len = name.length();
            if (len <= _charBufferLength) { // yes, fits right in
                name.getChars(0, len, _charBuffer, 0);
                // But as one segment, or multiple?
                if (len <= _outputMaxContiguous) {
                    if ((_outputTail + len) > _outputEnd) { // caller must ensure enough space
                        _flushBuffer();
                    }
                    _writeStringSegment(_charBuffer, 0, len);
                } else {
                    _writeStringSegments(_charBuffer, 0, len);
                }
            } else {
                _writeStringSegments(name);
            }
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
        } else { // non-standard, omit quotes
            _writeStringSegments(name);
        }
    }

    protected final void _writePPFieldName(SerializableString name, boolean commaBefore)
        throws IOException, JsonGenerationException
    {
        if (commaBefore) {
            _cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            _cfgPrettyPrinter.beforeObjectEntries(this);
        }

        boolean addQuotes = isEnabled(Feature.QUOTE_FIELD_NAMES); // standard
        if (addQuotes) {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
        }
        _writeBytes(name.asQuotedUTF8());
        if (addQuotes) {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = BYTE_QUOTE;
        }
    }
    
    /*
    /**********************************************************
    /* Output method implementations, textual
    /**********************************************************
     */

    @Override
    public void writeString(String text)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (text == null) {
            _writeNull();
            return;
        }
        // First: can we make a local copy of chars that make up text?
        final int len = text.length();
        if (len > _charBufferLength) { // nope: off-line handling
            _writeLongString(text);
            return;
        }
        // yes: good.
        text.getChars(0, len, _charBuffer, 0);
        // Output: if we can't guarantee it fits in output buffer, off-line as well:
        if (len > _outputMaxContiguous) {
            _writeLongString(_charBuffer, 0, len);
            return;
        }
        if ((_outputTail + len) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeStringSegment(_charBuffer, 0, len); // we checked space already above
        /* [JACKSON-462] But that method may have had to expand multi-byte Unicode
         *   chars, so we must check again
         */
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    private final void _writeLongString(String text)
        throws IOException, JsonGenerationException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeStringSegments(text);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    private final void _writeLongString(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeStringSegments(_charBuffer, 0, len);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    @Override
    public void writeString(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        // One or multiple segments?
        if (len <= _outputMaxContiguous) {
            if ((_outputTail + len) > _outputEnd) { // caller must ensure enough space
                _flushBuffer();
            }
            _writeStringSegment(text, offset, len);
        } else {
            _writeStringSegments(text, offset, len);
        }
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    @Override
    public final void writeString(SerializableString text)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeBytes(text.asQuotedUTF8());
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeBytes(text, offset, length);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        // One or multiple segments?
        if (len <= _outputMaxContiguous) {
            _writeUTF8Segment(text, offset, len);
        } else {
            _writeUTF8Segments(text, offset, len);
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }
    
    /*
    /**********************************************************
    /* Output method implementations, unprocessed ("raw")
    /**********************************************************
     */

    @Override
    public void writeRaw(String text)
        throws IOException, JsonGenerationException
    {
        int start = 0;
        int len = text.length();
        while (len > 0) {
            char[] buf = _charBuffer;
            final int blen = buf.length;
            final int len2 = (len < blen) ? len : blen;
            text.getChars(start, start+len2, buf, 0);
            writeRaw(buf, 0, len2);
            start += len2;
            len -= len2;
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        while (len > 0) {
            char[] buf = _charBuffer;
            final int blen = buf.length;
            final int len2 = (len < blen) ? len : blen;
            text.getChars(offset, offset+len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    // @TODO: rewrite for speed...
    @Override
    public final void writeRaw(char[] cbuf, int offset, int len)
        throws IOException, JsonGenerationException
    {
        // First: if we have 3 x charCount spaces, we know it'll fit just fine
        {
            int len3 = len+len+len;
            if ((_outputTail + len3) > _outputEnd) {
                // maybe we could flush?
                if (_outputEnd < len3) { // wouldn't be enough...
                    _writeSegmentedRaw(cbuf, offset, len);
                    return;
                }
                // yes, flushing brings enough space
                _flushBuffer();
            }
        }
        len += offset; // now marks the end

        // Note: here we know there is enough room, hence no output boundary checks
        main_loop:
        while (offset < len) {
            inner_loop:
            while (true) {
                int ch = (int) cbuf[offset];
                if (ch > 0x7F) {
                    break inner_loop;
                }
                _outputBuffer[_outputTail++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }
            char ch = cbuf[offset++];
            if (ch < 0x800) { // 2-byte?
                _outputBuffer[_outputTail++] = (byte) (0xc0 | (ch >> 6));
                _outputBuffer[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                _outputRawMultiByteChar(ch, cbuf, offset, len);
            }
        }
    }

    @Override
    public void writeRaw(char ch)
        throws IOException, JsonGenerationException
    {
        if ((_outputTail + 3) >= _outputEnd) {
            _flushBuffer();
        }
        final byte[] bbuf = _outputBuffer;
        if (ch <= 0x7F) {
            bbuf[_outputTail++] = (byte) ch;
        } else  if (ch < 0x800) { // 2-byte?
            bbuf[_outputTail++] = (byte) (0xc0 | (ch >> 6));
            bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
        } else {
            _outputRawMultiByteChar(ch, null, 0, 0);
        }
    }

    /**
     * Helper method called when it is possible that output of raw section
     * to output may cross buffer boundary
     */
    private final void _writeSegmentedRaw(char[] cbuf, int offset, int len)
        throws IOException, JsonGenerationException
    {
        final int end = _outputEnd;
        final byte[] bbuf = _outputBuffer;
        
        main_loop:
        while (offset < len) {
            inner_loop:
            while (true) {
                int ch = (int) cbuf[offset];
                if (ch >= 0x80) {
                    break inner_loop;
                }
                // !!! TODO: fast(er) writes (roll input, output checks in one)
                if (_outputTail >= end) {
                    _flushBuffer();
                }
                bbuf[_outputTail++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }
            if ((_outputTail + 3) >= _outputEnd) {
                _flushBuffer();
            }
            char ch = cbuf[offset++];
            if (ch < 0x800) { // 2-byte?
                bbuf[_outputTail++] = (byte) (0xc0 | (ch >> 6));
                bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                _outputRawMultiByteChar(ch, cbuf, offset, len);
            }
        }
    }
    
    /*
    /**********************************************************
    /* Output method implementations, base64-encoded binary
    /**********************************************************
     */

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write binary value");
        // Starting quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _writeBinary(b64variant, data, offset, offset+len);
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }
    
    /*
    /**********************************************************
    /* Output method implementations, primitive
    /**********************************************************
     */

    @Override
    public void writeNumber(int i)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write number");
        // up to 10 digits and possible minus sign
        if ((_outputTail + 11) >= _outputEnd) {
            _flushBuffer();
        }
        if (_cfgNumbersAsStrings) {
            _writeQuotedInt(i);
            return;
        }
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
    }

    private final void _writeQuotedInt(int i) throws IOException {
        if ((_outputTail + 13) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }    

    @Override
    public void writeNumber(long l)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write number");
        if (_cfgNumbersAsStrings) {
            _writeQuotedLong(l);
            return;
        }
        if ((_outputTail + 21) >= _outputEnd) {
            // up to 20 digits, minus sign
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
    }

    private final void _writeQuotedLong(long l) throws IOException {
        if ((_outputTail + 23) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }

    @Override
    public void writeNumber(BigInteger value)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write number");
        if (value == null) {
            _writeNull();
        } else if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(value);
        } else {
            writeRaw(value.toString());
        }
    }

    
    @Override
    public void writeNumber(double d)
        throws IOException, JsonGenerationException
    {
        if (_cfgNumbersAsStrings ||
            // [JACKSON-139]
            (((Double.isNaN(d) || Double.isInfinite(d))
                && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS)))) {
            writeString(String.valueOf(d));
            return;
        }
        // What is the max length for doubles? 40 chars?
        _verifyValueWrite("write number");
        writeRaw(String.valueOf(d));
    }

    @Override
    public void writeNumber(float f)
        throws IOException, JsonGenerationException
    {
        if (_cfgNumbersAsStrings ||
            // [JACKSON-139]
            (((Float.isNaN(f) || Float.isInfinite(f))
                && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS)))) {
            writeString(String.valueOf(f));
            return;
        }
        // What is the max length for floats?
        _verifyValueWrite("write number");
        writeRaw(String.valueOf(f));
    }

    @Override
    public void writeNumber(BigDecimal value)
        throws IOException, JsonGenerationException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite("write number");
        if (value == null) {
            _writeNull();
        } else if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(value);
        } else {
            writeRaw(value.toString());
        }
    }

    @Override
    public void writeNumber(String encodedValue)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write number");
        if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(encodedValue);            
        } else {
            writeRaw(encodedValue);
        }
    }

    private final void _writeQuotedRaw(Object value) throws IOException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
        writeRaw(value.toString());
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = BYTE_QUOTE;
    }
    
    @Override
    public void writeBoolean(boolean state)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write boolean value");
        if ((_outputTail + 5) >= _outputEnd) {
            _flushBuffer();
        }
        byte[] keyword = state ? TRUE_BYTES : FALSE_BYTES;
        int len = keyword.length;
        System.arraycopy(keyword, 0, _outputBuffer, _outputTail, len);
        _outputTail += len;
    }

    @Override
    public void writeNull()
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write null value");
        _writeNull();
    }

    /*
    /**********************************************************
    /* Implementations for other methods
    /**********************************************************
     */

    @Override
    protected final void _verifyValueWrite(String typeMsg)
        throws IOException, JsonGenerationException
    {
        int status = _writeContext.writeValue();
        if (status == JsonWriteContext.STATUS_EXPECT_NAME) {
            _reportError("Can not "+typeMsg+", expecting field name");
        }
        if (_cfgPrettyPrinter == null) {
            byte b;
            switch (status) {
            case JsonWriteContext.STATUS_OK_AFTER_COMMA:
                b = BYTE_COMMA;
                break;
            case JsonWriteContext.STATUS_OK_AFTER_COLON:
                b = BYTE_COLON;
                break;
            case JsonWriteContext.STATUS_OK_AFTER_SPACE:
                b = BYTE_SPACE;
                break;
            case JsonWriteContext.STATUS_OK_AS_IS:
            default:
                return;
            }
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail] = b;
            ++_outputTail;
            return;
        }
        // Otherwise, pretty printer knows what to do...
        _verifyPrettyValueWrite(typeMsg, status);
    }

    protected final void _verifyPrettyValueWrite(String typeMsg, int status)
        throws IOException, JsonGenerationException
    {
        // If we have a pretty printer, it knows what to do:
        switch (status) {
        case JsonWriteContext.STATUS_OK_AFTER_COMMA: // array
            _cfgPrettyPrinter.writeArrayValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AFTER_COLON:
            _cfgPrettyPrinter.writeObjectFieldValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AFTER_SPACE:
            _cfgPrettyPrinter.writeRootValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AS_IS:
            // First entry, but of which context?
            if (_writeContext.inArray()) {
                _cfgPrettyPrinter.beforeArrayValues(this);
            } else if (_writeContext.inObject()) {
                _cfgPrettyPrinter.beforeObjectEntries(this);
            }
            break;
        default:
            _cantHappen();
            break;
        }
    }

    /*
    /**********************************************************
    /* Low-level output handling
    /**********************************************************
     */

    @Override
    public final void flush()
        throws IOException
    {
        _flushBuffer();
        if (_outputStream != null) {
            if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                _outputStream.flush();
            }
        }
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();

        /* 05-Dec-2008, tatu: To add [JACKSON-27], need to close open
         *   scopes.
         */
        // First: let's see that we still have buffers...
        if (_outputBuffer != null
            && isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT)) {
            while (true) {
                JsonStreamContext ctxt = getOutputContext();
                if (ctxt.inArray()) {
                    writeEndArray();
                } else if (ctxt.inObject()) {
                    writeEndObject();
                } else {
                    break;
                }
            }
        }
        _flushBuffer();

        /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
         *   on the underlying Reader, unless we "own" it, or auto-closing
         *   feature is enabled.
         *   One downside: when using UTF8Writer, underlying buffer(s)
         *   may not be properly recycled if we don't close the writer.
         */
        if (_outputStream != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                _outputStream.close();
            } else  if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                // If we can't close it, we should at least flush
                _outputStream.flush();
            }
        }
        // Internal buffer(s) generator has can now be released as well
        _releaseBuffers();
    }

    @Override
    protected void _releaseBuffers()
    {
        byte[] buf = _outputBuffer;
        if (buf != null && _bufferRecyclable) {
            _outputBuffer = null;
            _ioContext.releaseWriteEncodingBuffer(buf);
        }
        char[] cbuf = _charBuffer;
        if (cbuf != null) {
            _charBuffer = null;
            _ioContext.releaseConcatBuffer(cbuf);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, raw bytes
    /**********************************************************
     */

    private final void _writeBytes(byte[] bytes) throws IOException
    {
        final int len = bytes.length;
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
            // still not enough?
            if (len > MAX_BYTES_TO_BUFFER) {
                _outputStream.write(bytes, 0, len);
                return;
            }
        }
        System.arraycopy(bytes, 0, _outputBuffer, _outputTail, len);
        _outputTail += len;
    }

    private final void _writeBytes(byte[] bytes, int offset, int len) throws IOException
    {
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
            // still not enough?
            if (len > MAX_BYTES_TO_BUFFER) {
                _outputStream.write(bytes, offset, len);
                return;
            }
        }
        System.arraycopy(bytes, offset, _outputBuffer, _outputTail, len);
        _outputTail += len;
    }

    /*
    /**********************************************************
    /* Internal methods, mid-level writing, String segments
    /**********************************************************
     */
    
    /**
     * Method called when String to write is long enough not to fit
     * completely in temporary copy buffer. If so, we will actually
     * copy it in small enough chunks so it can be directly fed
     * to single-segment writes (instead of maximum slices that
     * would fit in copy buffer)
     */
    private final void _writeStringSegments(String text)
        throws IOException, JsonGenerationException
    {
        int left = text.length();
        int offset = 0;
        final char[] cbuf = _charBuffer;

        while (left > 0) {
            int len = Math.min(_outputMaxContiguous, left);
            text.getChars(offset, offset+len, cbuf, 0);
            if ((_outputTail + len) > _outputEnd) { // caller must ensure enough space
                _flushBuffer();
            }
            _writeStringSegment(cbuf, 0, len);
            offset += len;
            left -= len;
        }
    }

    /**
     * Method called when character sequence to write is long enough that
     * its maximum encoded and escaped form is not guaranteed to fit in
     * the output buffer. If so, we will need to choose smaller output
     * chunks to write at a time.
     */
    private final void _writeStringSegments(char[] cbuf, int offset, int totalLen)
        throws IOException, JsonGenerationException
    {
        do {
            int len = Math.min(_outputMaxContiguous, totalLen);
            if ((_outputTail + len) > _outputEnd) { // caller must ensure enough space
                _flushBuffer();
            }
            _writeStringSegment(cbuf, offset, len);
            offset += len;
            totalLen -= len;
        } while (totalLen > 0);
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segments
    /**********************************************************
     */

    /**
     * This method called when the string content is already in
     * a char buffer, and its maximum total encoded and escaped length
     * can not exceed size of the output buffer.
     * Caller must ensure that there is enough space in output buffer,
     * assuming case of all non-escaped ASCII characters, as well as
     * potentially enough space for other cases (but not necessarily flushed)
     */
    private final void _writeStringSegment(char[] cbuf, int offset, int len)
        throws IOException, JsonGenerationException
    {
        // note: caller MUST ensure (via flushing) there's room for ASCII only
        
        // Fast+tight loop for ASCII-only, no-escaping-needed output
        len += offset; // becomes end marker, then

        int outputPtr = _outputTail;
        final byte[] outputBuffer = _outputBuffer;
        final int[] escCodes = _outputEscapes;

        while (offset < len) {
            int ch = cbuf[offset];
            // note: here we know that (ch > 0x7F) will cover case of escaping non-ASCII too:
            if (ch > 0x7F || escCodes[ch] != 0) {
                break;
            }
            outputBuffer[outputPtr++] = (byte) ch;
            ++offset;
        }
        _outputTail = outputPtr;
        if (offset < len) {
            // [JACKSON-106]
            if (_characterEscapes != null) {
                _writeCustomStringSegment2(cbuf, offset, len);
            // [JACKSON-102]
            } else if (_maximumNonEscapedChar == 0) {
                _writeStringSegment2(cbuf, offset, len);
            } else {
                _writeStringSegmentASCII2(cbuf, offset, len);
            }

        }
    }

    /**
     * Secondary method called when content contains characters to escape,
     * and/or multi-byte UTF-8 characters.
     */
    private final void _writeStringSegment2(final char[] cbuf, int offset, final int end)
        throws IOException, JsonGenerationException
    {
        // Ok: caller guarantees buffer can have room; but that may require flushing:
        if ((_outputTail +  6 * (end - offset)) > _outputEnd) {
            _flushBuffer();
        }

        int outputPtr = _outputTail;

        final byte[] outputBuffer = _outputBuffer;
        final int[] escCodes = _outputEscapes;
        
        while (offset < end) {
            int ch = cbuf[offset++];
            if (ch <= 0x7F) {
                 if (escCodes[ch] == 0) {
                     outputBuffer[outputPtr++] = (byte) ch;
                     continue;
                 }
                 int escape = escCodes[ch];
                 if (escape > 0) { // 2-char escape, fine
                     outputBuffer[outputPtr++] = BYTE_BACKSLASH;
                     outputBuffer[outputPtr++] = (byte) escape;
                 } else {
                     // ctrl-char, 6-byte escape...
                     outputPtr = _writeGenericEscape(ch, outputPtr);
                }
                continue;
            }
            if (ch <= 0x7FF) { // fine, just needs 2 byte output
                outputBuffer[outputPtr++] = (byte) (0xc0 | (ch >> 6));
                outputBuffer[outputPtr++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                outputPtr = _outputMultiByteChar(ch, outputPtr);
            }
        }
        _outputTail = outputPtr;
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with additional escaping (ASCII or such)
    /* (since 1.8; see [JACKSON-102])
    /**********************************************************
     */

    /**
     * Same as <code>_writeStringSegment2(char[], ...)</code., but with
     * additional escaping for high-range code points
     * 
     * @since 1.8
     */
    private final void _writeStringSegmentASCII2(final char[] cbuf, int offset, final int end)
        throws IOException, JsonGenerationException
    {
        // Ok: caller guarantees buffer can have room; but that may require flushing:
        if ((_outputTail +  6 * (end - offset)) > _outputEnd) {
            _flushBuffer();
        }
    
        int outputPtr = _outputTail;
    
        final byte[] outputBuffer = _outputBuffer;
        final int[] escCodes = _outputEscapes;
        final int maxUnescaped = _maximumNonEscapedChar;
        
        while (offset < end) {
            int ch = cbuf[offset++];
            if (ch <= 0x7F) {
                 if (escCodes[ch] == 0) {
                     outputBuffer[outputPtr++] = (byte) ch;
                     continue;
                 }
                 int escape = escCodes[ch];
                 if (escape > 0) { // 2-char escape, fine
                     outputBuffer[outputPtr++] = BYTE_BACKSLASH;
                     outputBuffer[outputPtr++] = (byte) escape;
                 } else {
                     // ctrl-char, 6-byte escape...
                     outputPtr = _writeGenericEscape(ch, outputPtr);
                 }
                 continue;
            }
            if (ch > maxUnescaped) { // [JACKSON-102] Allow forced escaping if non-ASCII (etc) chars:
                outputPtr = _writeGenericEscape(ch, outputPtr);
                continue;
            }
            if (ch <= 0x7FF) { // fine, just needs 2 byte output
                outputBuffer[outputPtr++] = (byte) (0xc0 | (ch >> 6));
                outputBuffer[outputPtr++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                outputPtr = _outputMultiByteChar(ch, outputPtr);
            }
        }
        _outputTail = outputPtr;
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with fully custom escaping (and possibly escaping of non-ASCII
    /**********************************************************
     */

    /**
     * Same as <code>_writeStringSegmentASCII2(char[], ...)</code., but with
     * additional checking for completely custom escapes
     * 
     * @since 1.8
     */
    private final void _writeCustomStringSegment2(final char[] cbuf, int offset, final int end)
        throws IOException, JsonGenerationException
    {
        // Ok: caller guarantees buffer can have room; but that may require flushing:
        if ((_outputTail +  6 * (end - offset)) > _outputEnd) {
            _flushBuffer();
        }
        int outputPtr = _outputTail;
    
        final byte[] outputBuffer = _outputBuffer;
        final int[] escCodes = _outputEscapes;
        // may or may not have this limit
        final int maxUnescaped = (_maximumNonEscapedChar <= 0) ? 0xFFFF : _maximumNonEscapedChar;
        final CharacterEscapes customEscapes = _characterEscapes; // non-null
        
        while (offset < end) {
            int ch = cbuf[offset++];
            if (ch <= 0x7F) {
                 if (escCodes[ch] == 0) {
                     outputBuffer[outputPtr++] = (byte) ch;
                     continue;
                 }
                 int escape = escCodes[ch];
                 if (escape > 0) { // 2-char escape, fine
                     outputBuffer[outputPtr++] = BYTE_BACKSLASH;
                     outputBuffer[outputPtr++] = (byte) escape;
                 } else if (escape == CharacterEscapes.ESCAPE_CUSTOM) {
                     SerializableString esc = customEscapes.getEscapeSequence(ch);
                     if (esc == null) {
                         throw new JsonGenerationException("Invalid custom escape definitions; custom escape not found for character code 0x"
                                 +Integer.toHexString(ch)+", although was supposed to have one");
                     }
                     outputPtr = _writeCustomEscape(outputBuffer, outputPtr, esc, end-offset);
                 } else {
                     // ctrl-char, 6-byte escape...
                     outputPtr = _writeGenericEscape(ch, outputPtr);
                 }
                 continue;
            }
            if (ch > maxUnescaped) { // [JACKSON-102] Allow forced escaping if non-ASCII (etc) chars:
                outputPtr = _writeGenericEscape(ch, outputPtr);
                continue;
            }
            SerializableString esc = customEscapes.getEscapeSequence(ch);
            if (esc != null) {
                outputPtr = _writeCustomEscape(outputBuffer, outputPtr, esc, end-offset);
                continue;
            }
            if (ch <= 0x7FF) { // fine, just needs 2 byte output
                outputBuffer[outputPtr++] = (byte) (0xc0 | (ch >> 6));
                outputBuffer[outputPtr++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                outputPtr = _outputMultiByteChar(ch, outputPtr);
            }
        }
        _outputTail = outputPtr;
    }

    private int _writeCustomEscape(byte[] outputBuffer, int outputPtr, SerializableString esc, int remainingChars)
        throws IOException, JsonGenerationException
    {
        byte[] raw = esc.asUnquotedUTF8(); // must be escaped at this point, shouldn't double-quote
        int len = raw.length;
        if (len > 6) { // may violate constraints we have, do offline
            return _handleLongCustomEscape(outputBuffer, outputPtr, _outputEnd, raw, remainingChars);
        }
        // otherwise will fit without issues, so:
        System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
        return (outputPtr + len);
    }
    
    private int _handleLongCustomEscape(byte[] outputBuffer, int outputPtr, int outputEnd, byte[] raw,
            int remainingChars)
        throws IOException, JsonGenerationException
    {
        int len = raw.length;
        if ((outputPtr + len) > outputEnd) {
            _outputTail = outputPtr;
            _flushBuffer();
            outputPtr = _outputTail;
            if (len > outputBuffer.length) { // very unlikely, but possible...
                _outputStream.write(raw, 0, len);
                return outputPtr;
            }
            System.arraycopy(raw, 0, outputBuffer, outputPtr, len);
            outputPtr += len;
        }
        // but is the invariant still obeyed? If not, flush once more
        if ((outputPtr +  6 * remainingChars) > outputEnd) {
            _flushBuffer();
            return _outputTail;
        }
        return outputPtr;
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, "raw UTF-8" segments
    /**********************************************************
     */
    
    /**
     * Method called when UTF-8 encoded (but NOT yet escaped!) content is not guaranteed
     * to fit in the output buffer after escaping; as such, we just need to
     * chunk writes.
     */
    private final void _writeUTF8Segments(byte[] utf8, int offset, int totalLen)
        throws IOException, JsonGenerationException
    {
        do {
            int len = Math.min(_outputMaxContiguous, totalLen);
            _writeUTF8Segment(utf8, offset, len);
            offset += len;
            totalLen -= len;
        } while (totalLen > 0);
    }
    
    private final void _writeUTF8Segment(byte[] utf8, final int offset, final int len)
        throws IOException, JsonGenerationException
    {
        // fast loop to see if escaping is needed; don't copy, just look
        final int[] escCodes = _outputEscapes;

        for (int ptr = offset, end = offset + len; ptr < end; ) {
            // 28-Feb-2011, tatu: escape codes just cover 7-bit range, so:
            int ch = utf8[ptr++];
            if ((ch >= 0) && escCodes[ch] != 0) {
                _writeUTF8Segment2(utf8, offset, len);
                return;
            }
        }
        
        // yes, fine, just copy the sucker
        if ((_outputTail + len) > _outputEnd) { // enough room or need to flush?
            _flushBuffer(); // but yes once we flush (caller guarantees length restriction)
        }
        System.arraycopy(utf8, offset, _outputBuffer, _outputTail, len);
        _outputTail += len;
    }

    private final void _writeUTF8Segment2(final byte[] utf8, int offset, int len)
        throws IOException, JsonGenerationException
    {
        int outputPtr = _outputTail;

        // Ok: caller guarantees buffer can have room; but that may require flushing:
        if ((outputPtr + (len * 6)) > _outputEnd) {
            _flushBuffer();
            outputPtr = _outputTail;
        }
        
        final byte[] outputBuffer = _outputBuffer;
        final int[] escCodes = _outputEscapes;
        len += offset; // so 'len' becomes 'end'
        
        while (offset < len) {
            byte b = utf8[offset++];
            int ch = b;
            if (ch < 0 || escCodes[ch] == 0) {
                outputBuffer[outputPtr++] = b;
                continue;
            }
            int escape = escCodes[ch];
            if (escape > 0) { // 2-char escape, fine
                outputBuffer[outputPtr++] = BYTE_BACKSLASH;
                outputBuffer[outputPtr++] = (byte) escape;
            } else {
                // ctrl-char, 6-byte escape...
                outputPtr = _writeGenericEscape(ch, outputPtr);
            }
        }
        _outputTail = outputPtr;
    }
    
    /*
    /**********************************************************
    /* Internal methods, low-level writing, base64 encoded
    /**********************************************************
     */
    
    protected void _writeBinary(Base64Variant b64variant, byte[] input, int inputPtr, final int inputEnd)
        throws IOException, JsonGenerationException
    {
        // Encoding is by chunks of 3 input, 4 output chars, so:
        int safeInputEnd = inputEnd - 3;
        // Let's also reserve room for possible (and quoted) lf char each round
        int safeOutputEnd = _outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        while (inputPtr <= safeInputEnd) {
            if (_outputTail > safeOutputEnd) { // need to flush
                _flushBuffer();
            }
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
            _outputTail = b64variant.encodeBase64Chunk(b24, _outputBuffer, _outputTail);
            if (--chunksBeforeLF <= 0) {
                // note: must quote in JSON value
                _outputBuffer[_outputTail++] = '\\';
                _outputBuffer[_outputTail++] = 'n';
                chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        int inputLeft = inputEnd - inputPtr; // 0, 1 or 2
        if (inputLeft > 0) { // yes, but do we have room for output?
            if (_outputTail > safeOutputEnd) { // don't really need 6 bytes but...
                _flushBuffer();
            }
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            _outputTail = b64variant.encodeBase64Partial(b24, inputLeft, _outputBuffer, _outputTail);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, character escapes/encoding
    /**********************************************************
     */
    
    /**
     * Method called to output a character that is beyond range of
     * 1- and 2-byte UTF-8 encodings, when outputting "raw" 
     * text (meaning it is not to be escaped or quoted)
     */
    private final int _outputRawMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException
    {
        // Let's handle surrogates gracefully (as 4 byte output):
        if (ch >= SURR1_FIRST) {
            if (ch <= SURR2_LAST) { // yes, outside of BMP
                // Do we have second part?
                if (inputOffset >= inputLen) { // nope... have to note down
                    _reportError("Split surrogate on writeRaw() input (last character)");
                }
                _outputSurrogates(ch, cbuf[inputOffset]);
                return (inputOffset+1);
            }
        }
        final byte[] bbuf = _outputBuffer;
        bbuf[_outputTail++] = (byte) (0xe0 | (ch >> 12));
        bbuf[_outputTail++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
        bbuf[_outputTail++] = (byte) (0x80 | (ch & 0x3f));
        return inputOffset;
    }

    protected final void _outputSurrogates(int surr1, int surr2)
        throws IOException
    {
        int c = _decodeSurrogate(surr1, surr2);
        if ((_outputTail + 4) > _outputEnd) {
            _flushBuffer();
        }
        final byte[] bbuf = _outputBuffer;
        bbuf[_outputTail++] = (byte) (0xf0 | (c >> 18));
        bbuf[_outputTail++] = (byte) (0x80 | ((c >> 12) & 0x3f));
        bbuf[_outputTail++] = (byte) (0x80 | ((c >> 6) & 0x3f));
        bbuf[_outputTail++] = (byte) (0x80 | (c & 0x3f));
    }
    
    /**
     * 
     * @param ch
     * @param outputPtr Position within output buffer to append multi-byte in
     * 
     * @return New output position after appending
     * 
     * @throws IOException
     */
    private final int _outputMultiByteChar(int ch, int outputPtr)
        throws IOException
    {
        byte[] bbuf = _outputBuffer;
        if (ch >= SURR1_FIRST && ch <= SURR2_LAST) { // yes, outside of BMP; add an escape
            bbuf[outputPtr++] = BYTE_BACKSLASH;
            bbuf[outputPtr++] = BYTE_u;
            
            bbuf[outputPtr++] = HEX_CHARS[(ch >> 12) & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[(ch >> 8) & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[(ch >> 4) & 0xF];
            bbuf[outputPtr++] = HEX_CHARS[ch & 0xF];
        } else {
            bbuf[outputPtr++] = (byte) (0xe0 | (ch >> 12));
            bbuf[outputPtr++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
            bbuf[outputPtr++] = (byte) (0x80 | (ch & 0x3f));
        }
        return outputPtr;
    }

    protected final int _decodeSurrogate(int surr1, int surr2) throws IOException
    {
        // First is known to be valid, but how about the other?
        if (surr2 < SURR2_FIRST || surr2 > SURR2_LAST) {
            String msg = "Incomplete surrogate pair: first char 0x"+Integer.toHexString(surr1)+", second 0x"+Integer.toHexString(surr2);
            _reportError(msg);
        }
        int c = 0x10000 + ((surr1 - SURR1_FIRST) << 10) + (surr2 - SURR2_FIRST);
        return c;
    }
    
    private final void _writeNull() throws IOException
    {
        if ((_outputTail + 4) >= _outputEnd) {
            _flushBuffer();
        }
        System.arraycopy(NULL_BYTES, 0, _outputBuffer, _outputTail, 4);
        _outputTail += 4;
    }
        
    /**
     * Method called to write a generic Unicode escape for given character.
     * 
     * @param charToEscape Character to escape using escape sequence (\\uXXXX)
     */
    private int _writeGenericEscape(int charToEscape, int outputPtr)
        throws IOException
    {
        final byte[] bbuf = _outputBuffer;
        bbuf[outputPtr++] = BYTE_BACKSLASH;
        bbuf[outputPtr++] = BYTE_u;
        if (charToEscape > 0xFF) {
            int hi = (charToEscape >> 8) & 0xFF;
            bbuf[outputPtr++] = HEX_CHARS[hi >> 4];
            bbuf[outputPtr++] = HEX_CHARS[hi & 0xF];
            charToEscape &= 0xFF;
        } else {
            bbuf[outputPtr++] = BYTE_0;
            bbuf[outputPtr++] = BYTE_0;
        }
        // We know it's a control char, so only the last 2 chars are non-0
        bbuf[outputPtr++] = HEX_CHARS[charToEscape >> 4];
        bbuf[outputPtr++] = HEX_CHARS[charToEscape & 0xF];
        return outputPtr;
    }

    protected final void _flushBuffer() throws IOException
    {
        int len = _outputTail;
        if (len > 0) {
            _outputTail = 0;
            _outputStream.write(_outputBuffer, 0, len);
        }
    }
}
