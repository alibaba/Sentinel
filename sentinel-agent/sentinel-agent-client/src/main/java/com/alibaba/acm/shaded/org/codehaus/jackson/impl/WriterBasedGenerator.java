package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.CharTypes;

/**
 * {@link JsonGenerator} that outputs JSON content using a {@link java.io.Writer}
 * which handles character encoding.
 */
public final class WriterBasedGenerator
    extends JsonGeneratorBase
{
    final protected static int SHORT_WRITE = 32;

    final protected static char[] HEX_CHARS = CharTypes.copyHexChars();

    /**
     * This is the default set of escape codes, over 7-bit ASCII range
     * (first 128 character codes), used for single-byte UTF-8 characters.
     */
    protected final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
    
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    final protected IOContext _ioContext;

    final protected Writer _writer;
    
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

    /**
     * When custom escapes are used, this member variable can be used to
     * store escape to use
     * 
     * @since 1.8
     */
    protected SerializableString _currentEscape;

    /*
    /**********************************************************
    /* Output buffering
    /**********************************************************
     */

    /**
     * Intermediate buffer in which contents are buffered before
     * being written using {@link #_writer}.
     */
    protected char[] _outputBuffer;

    /**
     * Pointer to the first buffered character to output
     */
    protected int _outputHead = 0;

    /**
     * Pointer to the position right beyond the last character to output
     * (end marker; may point to position right beyond the end of the buffer)
     */
    protected int _outputTail = 0;

    /**
     * End marker of the output buffer; one past the last valid position
     * within the buffer.
     */
    protected int _outputEnd;

    /**
     * Short (14 char) temporary buffer allocated if needed, for constructing
     * escape sequences
     */
    protected char[] _entityBuffer;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public WriterBasedGenerator(IOContext ctxt, int features, ObjectCodec codec,
            Writer w)
    {
        super(features, codec);
        _ioContext = ctxt;
        _writer = w;
        _outputBuffer = ctxt.allocConcatBuffer();
        _outputEnd = _outputBuffer.length;

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
        return _writer;
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
    public final void writeFieldName(String name)  throws IOException, JsonGenerationException
    {
        int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
    }

    @Override
    public final void writeStringField(String fieldName, String value)
        throws IOException, JsonGenerationException
    {
        writeFieldName(fieldName);
        writeString(value);
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
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
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
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
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
            _outputBuffer[_outputTail++] = '[';
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
            _outputBuffer[_outputTail++] = ']';
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
            _outputBuffer[_outputTail++] = '{';
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
            _outputBuffer[_outputTail++] = '}';
        }
    }

    protected void _writeFieldName(String name, boolean commaBefore)
        throws IOException, JsonGenerationException
    {
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, commaBefore);
            return;
        }
        // for fast+std case, need to output up to 2 chars, comma, dquote
        if ((_outputTail + 1) >= _outputEnd) {
            _flushBuffer();
        }
        if (commaBefore) {
            _outputBuffer[_outputTail++] = ',';
        }

        /* To support [JACKSON-46], we'll do this:
         * (Question: should quoting of spaces (etc) still be enabled?)
         */
        if (!isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            _writeString(name);
            return;
        }

        // we know there's room for at least one more char
        _outputBuffer[_outputTail++] = '"';
        // The beef:
        _writeString(name);
        // and closing quotes; need room for one more char:
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
    }

    public void _writeFieldName(SerializableString name, boolean commaBefore)
        throws IOException, JsonGenerationException
    {
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, commaBefore);
            return;
        }
        // for fast+std case, need to output up to 2 chars, comma, dquote
        if ((_outputTail + 1) >= _outputEnd) {
            _flushBuffer();
        }
        if (commaBefore) {
            _outputBuffer[_outputTail++] = ',';
        }
        /* To support [JACKSON-46], we'll do this:
         * (Question: should quoting of spaces (etc) still be enabled?)
         */
        final char[] quoted = name.asQuotedChars();
        if (!isEnabled(Feature.QUOTE_FIELD_NAMES)) {
            writeRaw(quoted, 0, quoted.length);
            return;
        }
        // we know there's room for at least one more char
        _outputBuffer[_outputTail++] = '"';
        // The beef:
        final int qlen = quoted.length;
        if ((_outputTail + qlen + 1) >= _outputEnd) {
            writeRaw(quoted, 0, qlen);
            // and closing quotes; need room for one more char:
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '"';
        } else {
            System.arraycopy(quoted, 0, _outputBuffer, _outputTail, qlen);
            _outputTail += qlen;
            _outputBuffer[_outputTail++] = '"';
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
            _outputBuffer[_outputTail++] = '"';
            _writeString(name);
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '"';
        } else { // non-standard, omit quotes
            _writeString(name);
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
    
        final char[] quoted = name.asQuotedChars();
        if (isEnabled(Feature.QUOTE_FIELD_NAMES)) { // standard
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '"';
            writeRaw(quoted, 0, quoted.length);
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '"';
        } else { // non-standard, omit quotes
            writeRaw(quoted, 0, quoted.length);
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
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
        _writeString(text);
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
    }

    @Override
    public void writeString(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
        _writeString(text, offset, len);
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
    }

    @Override
    public final void writeString(SerializableString sstr)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write text value");
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
        // Note: copied from writeRaw:
        char[] text = sstr.asQuotedChars();
        final int len = text.length;
        // Only worth buffering if it's a short write?
        if (len < SHORT_WRITE) {
            int room = _outputEnd - _outputTail;
            if (len > room) {
                _flushBuffer();
            }
            System.arraycopy(text, 0, _outputBuffer, _outputTail, len);
            _outputTail += len;
        } else {
            // Otherwise, better just pass through:
            _flushBuffer();
            _writer.write(text, 0, len);
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException
    {
        // could add support for buffering if we really want it...
        _reportUnsupportedOperation();
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length)
        throws IOException, JsonGenerationException
    {
        // could add support for buffering if we really want it...
        _reportUnsupportedOperation();
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
        // Nothing to check, can just output as is
        int len = text.length();
        int room = _outputEnd - _outputTail;

        if (room == 0) {
            _flushBuffer();
            room = _outputEnd - _outputTail;
        }
        // But would it nicely fit in? If yes, it's easy
        if (room >= len) {
            text.getChars(0, len, _outputBuffer, _outputTail);
            _outputTail += len;
        } else {
            writeRawLong(text);
        }
    }

    @Override
    public void writeRaw(String text, int start, int len)
        throws IOException, JsonGenerationException
    {
        // Nothing to check, can just output as is
        int room = _outputEnd - _outputTail;

        if (room < len) {
            _flushBuffer();
            room = _outputEnd - _outputTail;
        }
        // But would it nicely fit in? If yes, it's easy
        if (room >= len) {
            text.getChars(start, start+len, _outputBuffer, _outputTail);
            _outputTail += len;
        } else {            	
            writeRawLong(text.substring(start, start+len));
        }
    }

    @Override
    public void writeRaw(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        // Only worth buffering if it's a short write?
        if (len < SHORT_WRITE) {
            int room = _outputEnd - _outputTail;
            if (len > room) {
                _flushBuffer();
            }
            System.arraycopy(text, offset, _outputBuffer, _outputTail, len);
            _outputTail += len;
            return;
        }
        // Otherwise, better just pass through:
        _flushBuffer();
        _writer.write(text, offset, len);
    }

    @Override
    public void writeRaw(char c)
        throws IOException, JsonGenerationException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = c;
    }

    private void writeRawLong(String text)
        throws IOException, JsonGenerationException
    {
        int room = _outputEnd - _outputTail;
        // If not, need to do it by looping
        text.getChars(0, room, _outputBuffer, _outputTail);
        _outputTail += room;
        _flushBuffer();
        int offset = room;
        int len = text.length() - room;

        while (len > _outputEnd) {
            int amount = _outputEnd;
            text.getChars(offset, offset+amount, _outputBuffer, 0);
            _outputHead = 0;
            _outputTail = amount;
            _flushBuffer();
            offset += amount;
            len -= amount;
        }
        // And last piece (at most length of buffer)
        text.getChars(offset, offset+len, _outputBuffer, 0);
        _outputHead = 0;
        _outputTail = len;
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
        _outputBuffer[_outputTail++] = '"';
        _writeBinary(b64variant, data, offset, offset+len);
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
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
        if (_cfgNumbersAsStrings) {
            _writeQuotedInt(i);
            return;
        }
        // up to 10 digits and possible minus sign
        if ((_outputTail + 11) >= _outputEnd) {
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
    }

    private final void _writeQuotedInt(int i) throws IOException {
        if ((_outputTail + 13) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = '"';
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
        _outputBuffer[_outputTail++] = '"';
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = '"';
    }

    // !!! 05-Aug-2008, tatus: Any ways to optimize these?

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
        _outputBuffer[_outputTail++] = '"';
        writeRaw(value.toString());
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '"';
    }
    
    @Override
    public void writeBoolean(boolean state)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite("write boolean value");
        if ((_outputTail + 5) >= _outputEnd) {
            _flushBuffer();
        }
        int ptr = _outputTail;
        char[] buf = _outputBuffer;
        if (state) {
            buf[ptr] = 't';
            buf[++ptr] = 'r';
            buf[++ptr] = 'u';
            buf[++ptr] = 'e';
        } else {
            buf[ptr] = 'f';
            buf[++ptr] = 'a';
            buf[++ptr] = 'l';
            buf[++ptr] = 's';
            buf[++ptr] = 'e';
        }
        _outputTail = ptr+1;
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
            char c;
            switch (status) {
            case JsonWriteContext.STATUS_OK_AFTER_COMMA:
                c = ',';
                break;
            case JsonWriteContext.STATUS_OK_AFTER_COLON:
                c = ':';
                break;
            case JsonWriteContext.STATUS_OK_AFTER_SPACE:
                c = ' ';
                break;
            case JsonWriteContext.STATUS_OK_AS_IS:
            default:
                return;
            }
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail] = c;
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
        if (_writer != null) {
            if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                _writer.flush();
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
        if (_writer != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                _writer.close();
            } else  if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                // If we can't close it, we should at least flush
                _writer.flush();
            }
        }
        // Internal buffer(s) generator has can now be released as well
        _releaseBuffers();
    }

    @Override
    protected void _releaseBuffers()
    {
        char[] buf = _outputBuffer;
        if (buf != null) {
            _outputBuffer = null;
            _ioContext.releaseConcatBuffer(buf);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing; text, default
    /**********************************************************
     */

    private void _writeString(String text)
        throws IOException, JsonGenerationException
    {
        /* One check first: if String won't fit in the buffer, let's
         * segment writes. No point in extending buffer to huge sizes
         * (like if someone wants to include multi-megabyte base64
         * encoded stuff or such)
         */
        final int len = text.length();
        if (len > _outputEnd) { // Let's reserve space for entity at begin/end
            _writeLongString(text);
            return;
        }

        // Ok: we know String will fit in buffer ok
        // But do we need to flush first?
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
        }
        text.getChars(0, len, _outputBuffer, _outputTail);

        if (_characterEscapes != null) {
            _writeStringCustom(len);
        } else if (_maximumNonEscapedChar != 0) {
            _writeStringASCII(len, _maximumNonEscapedChar);
        } else {
            _writeString2(len);
        }
    }

    private void _writeString2(final int len)
        throws IOException, JsonGenerationException
    {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;

        output_loop:
        while (_outputTail < end) {
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                char c = _outputBuffer[_outputTail];
                if (c < escLen && escCodes[c] != 0) {
                    break escape_loop;
                }
                if (++_outputTail >= end) {
                    break output_loop;
                }
            }

            // Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            /* In any case, tail will be the new start, so hopefully
             * we have room now.
             */
            char c = _outputBuffer[_outputTail++];
            _prependOrWriteCharacterEscape(c, escCodes[c]);
        }
    }

    /**
     * Method called to write "long strings", strings whose length exceeds
     * output buffer length.
     */
    private void _writeLongString(String text)
        throws IOException, JsonGenerationException
    {
        // First things first: let's flush the buffer to get some more room
        _flushBuffer();

        // Then we can write 
        final int textLen = text.length();
        int offset = 0;
        do {
            int max = _outputEnd;
            int segmentLen = ((offset + max) > textLen)
                ? (textLen - offset) : max;
            text.getChars(offset, offset+segmentLen, _outputBuffer, 0);
            if (_characterEscapes != null) {
                _writeSegmentCustom(segmentLen);
            } else if (_maximumNonEscapedChar != 0) {
                _writeSegmentASCII(segmentLen, _maximumNonEscapedChar);
            } else {
                _writeSegment(segmentLen);
            }
            offset += segmentLen;
        } while (offset < textLen);
    }

    /**
     * Method called to output textual context which has been copied
     * to the output buffer prior to call. If any escaping is needed,
     * it will also be handled by the method.
     *<p>
     * Note: when called, textual content to write is within output
     * buffer, right after buffered content (if any). That's why only
     * length of that text is passed, as buffer and offset are implied.
     */
    private final void _writeSegment(int end)
        throws IOException, JsonGenerationException
    {
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;
    
        int ptr = 0;
        int start = ptr;

        output_loop:
        while (ptr < end) {
            // Fast loop for chars not needing escaping
            char c;
            while (true) {
                c = _outputBuffer[ptr];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
                if (++ptr >= end) {
                    break;
                }
            }

            // Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break output_loop;
                }
            }
            ++ptr;
            // So; either try to prepend (most likely), or write directly:
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCodes[c]);
        }
    }
    
    /**
     * This method called when the string content is already in
     * a char buffer, and need not be copied for processing.
     */
    private final void _writeString(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        if (_characterEscapes != null) {
            _writeStringCustom(text, offset, len);
            return;
        }
        if (_maximumNonEscapedChar != 0) {
            _writeStringASCII(text, offset, len, _maximumNonEscapedChar);
            return;
        }
        
        /* Let's just find longest spans of non-escapable
         * content, and for each see if it makes sense
         * to copy them, or write through
         */
        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;
        while (offset < len) {
            int start = offset;

            while (true) {
                char c = text[offset];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
                if (++offset >= len) {
                    break;
                }
            }

            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            char c = text[offset++];
            _appendCharacterEscape(c, escCodes[c]);          
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with additional escaping (ASCII or such)
    /* (since 1.8; see [JACKSON-102])
    /**********************************************************
     */

    /* Same as "_writeString2()", except needs additional escaping
     * for subset of characters
     */
    private void _writeStringASCII(final int len, final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        int escCode = 0;
        
        output_loop:
        while (_outputTail < end) {
            char c;
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                c = _outputBuffer[_outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break escape_loop;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break escape_loop;
                }
                if (++_outputTail >= end) {
                    break output_loop;
                }
            }
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            ++_outputTail;
            _prependOrWriteCharacterEscape(c, escCode);
        }
    }

    private final void _writeSegmentASCII(int end, final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
    
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
    
        output_loop:
        while (ptr < end) {
            // Fast loop for chars not needing escaping
            char c;
            while (true) {
                c = _outputBuffer[ptr];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                }
                if (++ptr >= end) {
                    break;
                }
            }
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break output_loop;
                }
            }
            ++ptr;
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
        }
    }

    private final void _writeStringASCII(char[] text, int offset, int len,
            final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);

        int escCode = 0;
        
        while (offset < len) {
            int start = offset;
            char c;
            
            while (true) {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                }
                if (++offset >= len) {
                    break;
                }
            }

            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            ++offset;
            _appendCharacterEscape(c, escCode);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with custom escaping (possibly coupling with ASCII limits)
    /* (since 1.8; see [JACKSON-106])
    /**********************************************************
     */

    /* Same as "_writeString2()", except needs additional escaping
     * for subset of characters
     */
    private void _writeStringCustom(final int len)
        throws IOException, JsonGenerationException
    {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        int escCode = 0;
        final CharacterEscapes customEscapes = _characterEscapes;

        output_loop:
        while (_outputTail < end) {
            char c;
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                c = _outputBuffer[_outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break escape_loop;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break escape_loop;
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break escape_loop;
                    }
                }
                if (++_outputTail >= end) {
                    break output_loop;
                }
            }
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            ++_outputTail;
            _prependOrWriteCharacterEscape(c, escCode);
        }
    }

    private final void _writeSegmentCustom(int end)
        throws IOException, JsonGenerationException
    {
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        final CharacterEscapes customEscapes = _characterEscapes;
    
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
    
        output_loop:
        while (ptr < end) {
            // Fast loop for chars not needing escaping
            char c;
            while (true) {
                c = _outputBuffer[ptr];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break;
                    }
                }
                if (++ptr >= end) {
                    break;
                }
            }
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break output_loop;
                }
            }
            ++ptr;
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
        }
    }

    private final void _writeStringCustom(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        final CharacterEscapes customEscapes = _characterEscapes;

        int escCode = 0;
        
        while (offset < len) {
            int start = offset;
            char c;
            
            while (true) {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break;
                    }
                }
                if (++offset >= len) {
                    break;
                }
            }
    
            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            ++offset;
            _appendCharacterEscape(c, escCode);
        }
    }
    
    /*
    /**********************************************************
    /* Internal methods, low-level writing; binary
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
    /* Internal methods, low-level writing, other
    /**********************************************************
     */
    
    private final void _writeNull() throws IOException
    {
        if ((_outputTail + 4) >= _outputEnd) {
            _flushBuffer();
        }
        int ptr = _outputTail;
        char[] buf = _outputBuffer;
        buf[ptr] = 'n';
        buf[++ptr] = 'u';
        buf[++ptr] = 'l';
        buf[++ptr] = 'l';
        _outputTail = ptr+1;
    }
        
    /*
    /**********************************************************
    /* Internal methods, low-level writing, escapes
    /**********************************************************
     */

    /**
     * Method called to try to either prepend character escape at front of
     * given buffer; or if not possible, to write it out directly.
     * Uses head and tail pointers (and updates as necessary)
     */
    private final void _prependOrWriteCharacterEscape(char ch, int escCode)
        throws IOException, JsonGenerationException
    {
        if (escCode >= 0) { // \\N (2 char)
            if (_outputTail >= 2) { // fits, just prepend
                int ptr = _outputTail - 2;
                _outputHead = ptr;
                _outputBuffer[ptr++] = '\\';
                _outputBuffer[ptr] = (char) escCode;
                return;
            }
            // won't fit, write
            char[] buf = _entityBuffer;
            if (buf == null) {
                buf = _allocateEntityBuffer();
            }
            _outputHead = _outputTail;
            buf[1] = (char) escCode;
            _writer.write(buf, 0, 2);
            return;
        }
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
            if (_outputTail >= 6) { // fits, prepend to buffer
                char[] buf = _outputBuffer;
                int ptr = _outputTail - 6;
                _outputHead = ptr;
                buf[ptr] = '\\';
                buf[++ptr] = 'u';
                // We know it's a control char, so only the last 2 chars are non-0
                if (ch > 0xFF) { // beyond 8 bytes
                    int hi = (ch >> 8) & 0xFF;
                    buf[++ptr] = HEX_CHARS[hi >> 4];
                    buf[++ptr] = HEX_CHARS[hi & 0xF];
                    ch &= 0xFF;
                } else {
                    buf[++ptr] = '0';
                    buf[++ptr] = '0';
                }
                buf[++ptr] = HEX_CHARS[ch >> 4];
                buf[++ptr] = HEX_CHARS[ch & 0xF];
                return;
            }
            // won't fit, flush and write
            char[] buf = _entityBuffer;
            if (buf == null) {
                buf = _allocateEntityBuffer();
            }
            _outputHead = _outputTail;
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                int lo = ch & 0xFF;
                buf[10] = HEX_CHARS[hi >> 4];
                buf[11] = HEX_CHARS[hi & 0xF];
                buf[12] = HEX_CHARS[lo >> 4];
                buf[13] = HEX_CHARS[lo & 0xF];
                _writer.write(buf, 8, 6);
            } else { // We know it's a control char, so only the last 2 chars are non-0
                buf[6] = HEX_CHARS[ch >> 4];
                buf[7] = HEX_CHARS[ch & 0xF];
                _writer.write(buf, 2, 6);
            }
            return;
        }
        String escape;

        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if (_outputTail >= len) { // fits in, prepend
            int ptr = _outputTail - len;
            _outputHead = ptr;
            escape.getChars(0, len, _outputBuffer, ptr);
            return;
        }
        // won't fit, write separately
        _outputHead = _outputTail;
        _writer.write(escape);
    }

    /**
     * Method called to try to either prepend character escape at front of
     * given buffer; or if not possible, to write it out directly.
     * 
     * @return Pointer to start of prepended entity (if prepended); or 'ptr'
     *   if not.
     */
    private final int _prependOrWriteCharacterEscape(char[] buffer, int ptr, int end,
            char ch, int escCode)
        throws IOException, JsonGenerationException
    {
        if (escCode >= 0) { // \\N (2 char)
            if (ptr > 1 && ptr < end) { // fits, just prepend
                ptr -= 2;
                buffer[ptr] = '\\';
                buffer[ptr+1] = (char) escCode;
            } else { // won't fit, write
                char[] ent = _entityBuffer;
                if (ent == null) {
                    ent = _allocateEntityBuffer();
                }
                ent[1] = (char) escCode;
                _writer.write(ent, 0, 2);
            }
            return ptr;
        }
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
            if (ptr > 5 && ptr < end) { // fits, prepend to buffer
                ptr -= 6;
                buffer[ptr++] = '\\';
                buffer[ptr++] = 'u';
                // We know it's a control char, so only the last 2 chars are non-0
                if (ch > 0xFF) { // beyond 8 bytes
                    int hi = (ch >> 8) & 0xFF;
                    buffer[ptr++] = HEX_CHARS[hi >> 4];
                    buffer[ptr++] = HEX_CHARS[hi & 0xF];
                    ch &= 0xFF;
                } else {
                    buffer[ptr++] = '0';
                    buffer[ptr++] = '0';
                }
                buffer[ptr++] = HEX_CHARS[ch >> 4];
                buffer[ptr] = HEX_CHARS[ch & 0xF];
                ptr -= 5;
            } else {
                // won't fit, flush and write
                char[] ent = _entityBuffer;
                if (ent == null) {
                    ent = _allocateEntityBuffer();
                }
                _outputHead = _outputTail;
                if (ch > 0xFF) { // beyond 8 bytes
                    int hi = (ch >> 8) & 0xFF;
                    int lo = ch & 0xFF;
                    ent[10] = HEX_CHARS[hi >> 4];
                    ent[11] = HEX_CHARS[hi & 0xF];
                    ent[12] = HEX_CHARS[lo >> 4];
                    ent[13] = HEX_CHARS[lo & 0xF];
                    _writer.write(ent, 8, 6);
                } else { // We know it's a control char, so only the last 2 chars are non-0
                    ent[6] = HEX_CHARS[ch >> 4];
                    ent[7] = HEX_CHARS[ch & 0xF];
                    _writer.write(ent, 2, 6);
                }
            }
            return ptr;
        }
        String escape;
        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if (ptr >= len && ptr < end) { // fits in, prepend
            ptr -= len;
            escape.getChars(0, len, buffer, ptr);
        } else { // won't fit, write separately
            _writer.write(escape);
        }
        return ptr;
    }

    /**
     * Method called to append escape sequence for given character, at the
     * end of standard output buffer; or if not possible, write out directly.
     */
    private final void _appendCharacterEscape(char ch, int escCode)
        throws IOException, JsonGenerationException
    {
        if (escCode >= 0) { // \\N (2 char)
            if ((_outputTail + 2) > _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '\\';
            _outputBuffer[_outputTail++] = (char) escCode;
            return;
        }
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
            if ((_outputTail + 2) > _outputEnd) {
                _flushBuffer();
            }
            int ptr = _outputTail;
            char[] buf = _outputBuffer;
            buf[ptr++] = '\\';
            buf[ptr++] = 'u';
            // We know it's a control char, so only the last 2 chars are non-0
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                buf[ptr++] = HEX_CHARS[hi >> 4];
                buf[ptr++] = HEX_CHARS[hi & 0xF];
                ch &= 0xFF;
            } else {
                buf[ptr++] = '0';
                buf[ptr++] = '0';
            }
            buf[ptr++] = HEX_CHARS[ch >> 4];
            buf[ptr] = HEX_CHARS[ch & 0xF];
            _outputTail = ptr;
            return;
        }
        String escape;
        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
            if (len > _outputEnd) { // very very long escape; unlikely but theoretically possible
                _writer.write(escape);
                return;
            }
        }
        escape.getChars(0, len, _outputBuffer, _outputTail);
        _outputTail += len;
    }
    
    private char[] _allocateEntityBuffer()
    {
        char[] buf = new char[14];
        // first 2 chars, non-numeric escapes (like \n)
        buf[0] = '\\';
        // next 6; 8-bit escapes (control chars mostly)
        buf[2] = '\\';
        buf[3] = 'u';
        buf[4] = '0';
        buf[5] = '0';
        // last 6, beyond 8 bits
        buf[8] = '\\';
        buf[9] = 'u';
        _entityBuffer = buf;
        return buf;
    }
    
    protected final void _flushBuffer() throws IOException
    {
        int len = _outputTail - _outputHead;
        if (len > 0) {
            int offset = _outputHead;
            _outputTail = _outputHead = 0;
            _writer.write(_outputBuffer, offset, len);
        }
    }
}
