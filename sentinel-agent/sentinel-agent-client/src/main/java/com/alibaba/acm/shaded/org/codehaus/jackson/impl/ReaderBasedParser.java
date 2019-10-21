package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.IOContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.CharsToNameCanonicalizer;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.*;

/**
 * This is a concrete implementation of {@link JsonParser}, which is
 * based on a {@link java.io.Reader} to handle low-level character
 * conversion tasks.
 */
public final class ReaderBasedParser
    extends JsonParserBase
{
    /*
    /**********************************************************
    /* Input configuration
    /**********************************************************
     */

    /**
     * Reader that can be used for reading more content, if one
     * buffer from input source, but in some cases pre-loaded buffer
     * is handed to the parser.
     */
    protected Reader _reader;

    /**
     * Current buffer from which data is read; generally data is read into
     * buffer from input source.
     */
    protected char[] _inputBuffer;

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected ObjectCodec _objectCodec;

    final protected CharsToNameCanonicalizer _symbols;

    /*
    /**********************************************************
    /* Parsing state
    /**********************************************************
     */
    
    /**
     * Flag that indicates that the current token has not yet
     * been fully processed, and needs to be finished for
     * some access (or skipped to obtain the next token)
     */
    protected boolean _tokenIncomplete = false;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public ReaderBasedParser(IOContext ctxt, int features, Reader r,
                             ObjectCodec codec, CharsToNameCanonicalizer st)
    {
        super(ctxt, features);
        _reader = r;
        _inputBuffer = ctxt.allocTokenBuffer();
        _objectCodec = codec;
        _symbols = st;
    }

    /*
    /**********************************************************
    /* Base method defs, overrides
    /**********************************************************
     */
    
    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    @Override
    public int releaseBuffered(Writer w) throws IOException
    {
        int count = _inputEnd - _inputPtr;
        if (count < 1) {
            return 0;
        }
        // let's just advance ptr to end
        int origPtr = _inputPtr;
        w.write(_inputBuffer, origPtr, count);
        return count;
    }

    @Override
    public Object getInputSource() {
        return _reader;
    }

    @Override
    protected final boolean loadMore() throws IOException
    {
        _currInputProcessed += _inputEnd;
        _currInputRowStart -= _inputEnd;

        if (_reader != null) {
            int count = _reader.read(_inputBuffer, 0, _inputBuffer.length);
            if (count > 0) {
                _inputPtr = 0;
                _inputEnd = count;
                return true;
            }
            // End of input
            _closeInput();
            // Should never return 0, so let's fail
            if (count == 0) {
                throw new IOException("Reader returned 0 characters when trying to read "+_inputEnd);
            }
        }
        return false;
    }

    protected char getNextChar(String eofMsg)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                _reportInvalidEOF(eofMsg);
            }
        }
        return _inputBuffer[_inputPtr++];
    }

    @Override
    protected void _closeInput() throws IOException
    {
        /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
         *   on the underlying Reader, unless we "own" it, or auto-closing
         *   feature is enabled.
         *   One downside is that when using our optimized
         *   Reader (granted, we only do that for UTF-32...) this
         *   means that buffer recycling won't work correctly.
         */
        if (_reader != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                _reader.close();
            }
            _reader = null;
        }
    }

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     */
    @Override
    protected void _releaseBuffers()
        throws IOException
    {
        super._releaseBuffers();
        char[] buf = _inputBuffer;
        if (buf != null) {
            _inputBuffer = null;
            _ioContext.releaseTokenBuffer(buf);
        }
    }
    
    /*
    /**********************************************************
    /* Public API, data access
    /**********************************************************
     */
    
    /**
     * Method for accessing textual representation of the current event;
     * if no current event (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any event.
     */
    @Override
    public final String getText()
        throws IOException, JsonParseException
    {
        JsonToken t = _currToken;
        if (t == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return _getText2(t);
    }
    
    protected final String _getText2(JsonToken t)
    {
        if (t == null) {
            return null;
        }
        switch (t) {
        case FIELD_NAME:
            return _parsingContext.getCurrentName();

        case VALUE_STRING:
            // fall through
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
            return _textBuffer.contentsAsString();
        }
        return t.asString();
    }

    @Override
    public char[] getTextCharacters()
        throws IOException, JsonParseException
    {
        if (_currToken != null) { // null only before/after document
            switch (_currToken) {
                
            case FIELD_NAME:
                if (!_nameCopied) {
                    String name = _parsingContext.getCurrentName();
                    int nameLen = name.length();
                    if (_nameCopyBuffer == null) {
                        _nameCopyBuffer = _ioContext.allocNameCopyBuffer(nameLen);
                    } else if (_nameCopyBuffer.length < nameLen) {
                        _nameCopyBuffer = new char[nameLen];
                    }
                    name.getChars(0, nameLen, _nameCopyBuffer, 0);
                    _nameCopied = true;
                }
                return _nameCopyBuffer;
    
            case VALUE_STRING:
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString(); // only strings can be incomplete
                }
                // fall through
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return _textBuffer.getTextBuffer();
                
            default:
                return _currToken.asCharArray();
            }
        }
        return null;
    }

    @Override
    public int getTextLength()
        throws IOException, JsonParseException
    {
        if (_currToken != null) { // null only before/after document
            switch (_currToken) {
                
            case FIELD_NAME:
                return _parsingContext.getCurrentName().length();
            case VALUE_STRING:
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString(); // only strings can be incomplete
                }
                // fall through
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return _textBuffer.size();
                
            default:
                return _currToken.asCharArray().length;
            }
        }
        return 0;
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException
    {
        // Most have offset of 0, only some may have other values:
        if (_currToken != null) {
            switch (_currToken) {
            case FIELD_NAME:
                return 0;
            case VALUE_STRING:
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString(); // only strings can be incomplete
                }
                // fall through
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return _textBuffer.getTextOffset();
            }
        }
        return 0;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        if (_currToken != JsonToken.VALUE_STRING &&
                (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT || _binaryValue == null)) {
            _reportError("Current token ("+_currToken+") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        /* To ensure that we won't see inconsistent data, better clear up
         * state...
         */
        if (_tokenIncomplete) {
            try {
                _binaryValue = _decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw _constructError("Failed to decode VALUE_STRING as base64 ("+b64variant+"): "+iae.getMessage());
            }
            /* let's clear incomplete only now; allows for accessing other
             * textual content in error cases
             */
            _tokenIncomplete = false;
        } else { // may actually require conversion...
            if (_binaryValue == null) {
                ByteArrayBuilder builder = _getByteArrayBuilder();
                _decodeBase64(getText(), builder, b64variant);
                _binaryValue = builder.toByteArray();
            }
        }
        return _binaryValue;
    }
    
    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    /**
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     */
    @Override
    public JsonToken nextToken()
        throws IOException, JsonParseException
    {
        _numTypesValid = NR_UNKNOWN;

        /* First: field names are special -- we will always tokenize
         * (part of) value along with field name to simplify
         * state handling. If so, can and need to use secondary token:
         */
        if (_currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        if (_tokenIncomplete) {
            _skipString(); // only strings can be partial
        }
        int i = _skipWSOrEnd();
        if (i < 0) { // end-of-input
            /* 19-Feb-2009, tatu: Should actually close/release things
             *    like input source, symbol table and recyclable buffers now.
             */
            close();
            return (_currToken = null);
        }

        /* First, need to ensure we know the starting location of token
         * after skipping leading white space
         */
        _tokenInputTotal = _currInputProcessed + _inputPtr - 1;
        _tokenInputRow = _currInputRow;
        _tokenInputCol = _inputPtr - _currInputRowStart - 1;

        // finally: clear any data retained so far
        _binaryValue = null;

        // Closing scope?
        if (i == INT_RBRACKET) {
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.getParent();
            return (_currToken = JsonToken.END_ARRAY);
        }
        if (i == INT_RCURLY) {
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.getParent();
            return (_currToken = JsonToken.END_OBJECT);
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            if (i != INT_COMMA) {
                _reportUnexpectedChar(i, "was expecting comma to separate "+_parsingContext.getTypeDesc()+" entries");
            }
            i = _skipWS();
        }

        /* And should we now have a name? Always true for
         * Object contexts, since the intermediate 'expect-value'
         * state is never retained.
         */
        boolean inObject = _parsingContext.inObject();
        if (inObject) {
           // First, field name itself:
            String name = _parseFieldName(i);
            _parsingContext.setCurrentName(name);
            _currToken = JsonToken.FIELD_NAME;
            i = _skipWS();
            if (i != INT_COLON) {
                _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
            }
            i = _skipWS();
        }

        // Ok: we must have a value... what is it?

        JsonToken t;

        switch (i) {
        case INT_QUOTE:
            _tokenIncomplete = true;
            t = JsonToken.VALUE_STRING;
            break;
        case INT_LBRACKET:
            if (!inObject) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            }
            t = JsonToken.START_ARRAY;
            break;
        case INT_LCURLY:
            if (!inObject) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            t = JsonToken.START_OBJECT;
            break;
        case INT_RBRACKET:
        case INT_RCURLY:
            // Error: neither is valid at this point; valid closers have
            // been handled earlier
            _reportUnexpectedChar(i, "expected a value");
        case INT_t:
            _matchToken("true", 1);
            t = JsonToken.VALUE_TRUE;
            break;
        case INT_f:
            _matchToken("false", 1);
            t = JsonToken.VALUE_FALSE;
            break;
        case INT_n:
            _matchToken("null", 1);
            t = JsonToken.VALUE_NULL;
            break;

        case INT_MINUS:
            /* Should we have separate handling for plus? Although
             * it is not allowed per se, it may be erroneously used,
             * and could be indicate by a more specific error message.
             */
        case INT_0:
        case INT_1:
        case INT_2:
        case INT_3:
        case INT_4:
        case INT_5:
        case INT_6:
        case INT_7:
        case INT_8:
        case INT_9:
            t = parseNumberText(i);
            break;
        default:
            t = _handleUnexpectedValue(i);
            break;
        }

        if (inObject) {
            _nextToken = t;
            return _currToken;
        }
        _currToken = t;
        return t;
    }

    private final JsonToken _nextAfterName()
    {
        _nameCopied = false; // need to invalidate if it was copied
        JsonToken t = _nextToken;
        _nextToken = null;
        // Also: may need to start new context?
        if (t == JsonToken.START_ARRAY) {
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
        } else if (t == JsonToken.START_OBJECT) {
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
        }
        return (_currToken = t);
    }

    /*
    @Override
    public boolean nextFieldName(SerializableString str)
         throws IOException, JsonParseException
     */

    // note: identical to one in Utf8StreamParser
    @Override
    public String nextTextValue()
        throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_STRING) {
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString();
                }
                return _textBuffer.contentsAsString();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return null;
        }
        // !!! TODO: optimize this case as well
        return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
    }

    // note: identical to one in Utf8StreamParser
    @Override
    public int nextIntValue(int defaultValue)
        throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.FIELD_NAME) {
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return getIntValue();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return defaultValue;
        }
        // !!! TODO: optimize this case as well
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
    }

    // note: identical to one in Utf8StreamParser
    @Override
    public long nextLongValue(long defaultValue)
        throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return getLongValue();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return defaultValue;
        }
        // !!! TODO: optimize this case as well
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
    }

    // note: identical to one in Utf8StreamParser
    @Override
    public Boolean nextBooleanValue()
        throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return null;
        }
        switch (nextToken()) {
        case VALUE_TRUE:
            return Boolean.TRUE;
        case VALUE_FALSE:
            return Boolean.FALSE;
        }
        return null;
    }
    
    @Override
    public void close() throws IOException
    {
        super.close();
        _symbols.release();
    }

    /*
    /**********************************************************
    /* Internal methods, number parsing
    /* (note: in 1.8 and prior, part of "ReaderBasedNumericParser"
    /**********************************************************
     */

    /**
     * Initial parsing method for number values. It needs to be able
     * to parse enough input to be able to determine whether the
     * value is to be considered a simple integer value, or a more
     * generic decimal value: latter of which needs to be expressed
     * as a floating point number. The basic rule is that if the number
     * has no fractional or exponential part, it is an integer; otherwise
     * a floating point number.
     *<p>
     * Because much of input has to be processed in any case, no partial
     * parsing is done: all input text will be stored for further
     * processing. However, actual numeric value conversion will be
     * deferred, since it is usually the most complicated and costliest
     * part of processing.
     */
    protected final JsonToken parseNumberText(int ch)
        throws IOException, JsonParseException
    {
        /* Although we will always be complete with respect to textual
         * representation (that is, all characters will be parsed),
         * actual conversion to a number is deferred. Thus, need to
         * note that no representations are valid yet
         */
        boolean negative = (ch == INT_MINUS);
        int ptr = _inputPtr;
        int startPtr = ptr-1; // to include sign/digit already read
        final int inputLen = _inputEnd;

        dummy_loop:
        do { // dummy loop, to be able to break out
            if (negative) { // need to read the next digit
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = _inputBuffer[ptr++];
                // First check: must have a digit to follow minus sign
                if (ch > INT_9 || ch < INT_0) {
                    _inputPtr = ptr;
                    return _handleInvalidNumberStart(ch, true);
                }
                /* (note: has been checked for non-negative already, in
                 * the dispatching code that determined it should be
                 * a numeric value)
                 */
            }
            // One special case, leading zero(es):
            if (ch == INT_0) {
                break dummy_loop;
            }
            
            /* First, let's see if the whole number is contained within
             * the input buffer unsplit. This should be the common case;
             * and to simplify processing, we will just reparse contents
             * in the alternative case (number split on buffer boundary)
             */
            
            int intLen = 1; // already got one
            
            // First let's get the obligatory integer part:
            
            int_loop:
            while (true) {
                if (ptr >= _inputEnd) {
                    break dummy_loop;
                }
                ch = (int) _inputBuffer[ptr++];
                if (ch < INT_0 || ch > INT_9) {
                    break int_loop;
                }
                ++intLen;
            }

            int fractLen = 0;
            
            // And then see if we get other parts
            if (ch == INT_DECIMAL_POINT) { // yes, fraction
                fract_loop:
                while (true) {
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                    if (ch < INT_0 || ch > INT_9) {
                        break fract_loop;
                    }
                    ++fractLen;
                }
                // must be followed by sequence of ints, one minimum
                if (fractLen == 0) {
                    reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
                }
            }

            int expLen = 0;
            if (ch == INT_e || ch == INT_E) { // and/or exponent
                if (ptr >= inputLen) {
                    break dummy_loop;
                }
                // Sign indicator?
                ch = (int) _inputBuffer[ptr++];
                if (ch == INT_MINUS || ch == INT_PLUS) { // yup, skip for now
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                while (ch <= INT_9 && ch >= INT_0) {
                    ++expLen;
                    if (ptr >= inputLen) {
                        break dummy_loop;
                    }
                    ch = (int) _inputBuffer[ptr++];
                }
                // must be followed by sequence of ints, one minimum
                if (expLen == 0) {
                    reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
                }
            }

            // Got it all: let's add to text buffer for parsing, access
            --ptr; // need to push back following separator
            _inputPtr = ptr;
            int len = ptr-startPtr;
            _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
            return reset(negative, intLen, fractLen, expLen);
        } while (false);

        _inputPtr = negative ? (startPtr+1) : startPtr;
        return parseNumberText2(negative);
    }

    /**
     * Method called to parse a number, when the primary parse
     * method has failed to parse it, due to it being split on
     * buffer boundary. As a result code is very similar, except
     * that it has to explicitly copy contents to the text buffer
     * instead of just sharing the main input buffer.
     */
    private final JsonToken parseNumberText2(boolean negative)
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';
        }

        // This is the place to do leading-zero check(s) too:
        int intLen = 0;
        char c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : getNextChar("No digit following minus sign");
        if (c == '0') {
            c = _verifyNoLeadingZeroes();
        }
        boolean eof = false;

        // Ok, first the obligatory integer part:
        int_loop:
        while (c >= '0' && c <= '9') {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            if (_inputPtr >= _inputEnd && !loadMore()) {
                // EOF is legal for main level int values
                c = CHAR_NULL;
                eof = true;
                break int_loop;
            }
            c = _inputBuffer[_inputPtr++];
        }
        // Also, integer part is not optional
        if (intLen == 0) {
            reportInvalidNumber("Missing integer part (next char "+_getCharDesc(c)+")");
        }

        int fractLen = 0;
        // And then see if we get other parts
        if (c == '.') { // yes, fraction
            outBuf[outPtr++] = c;

            fract_loop:
            while (true) {
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break fract_loop;
                }
                c = _inputBuffer[_inputPtr++];
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == 'e' || c == 'E') { // exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            // Not optional, can require that we get one more char
            c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                : getNextChar("expected a digit for number exponent");
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                // Likewise, non optional:
                c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++]
                    : getNextChar("expected a digit for number exponent");
            }

            exp_loop:
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break exp_loop;
                }
                c = _inputBuffer[_inputPtr++];
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        if (!eof) {
            --_inputPtr;
        }
        _textBuffer.setCurrentLength(outPtr);
        // And there we have it!
        return reset(negative, intLen, fractLen, expLen);
    }

    /**
     * Method called when we have seen one zero, and want to ensure
     * it is not followed by another
     */
    private final char _verifyNoLeadingZeroes()
        throws IOException, JsonParseException
    {
        // Ok to have plain "0"
        if (_inputPtr >= _inputEnd && !loadMore()) {
            return '0';
        }
        char ch = _inputBuffer[_inputPtr];
        // if not followed by a number (probably '.'); return zero as is, to be included
        if (ch < '0' || ch > '9') {
            return '0';
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        // if so, just need to skip either all zeroes (if followed by number); or all but one (if non-number)
        ++_inputPtr; // Leading zero to be skipped
        if (ch == INT_0) {
            while (_inputPtr < _inputEnd || loadMore()) {
                ch = _inputBuffer[_inputPtr];
                if (ch < '0' || ch > '9') { // followed by non-number; retain one zero
                    return '0';
                }
                ++_inputPtr; // skip previous zero
                if (ch != '0') { // followed by other number; return 
                    break;
                }
            }
        }
        return ch;
    }

    /**
     * Method called if expected numeric value (due to leading sign) does not
     * look like a number
     */
    protected JsonToken _handleInvalidNumberStart(int ch, boolean negative)
        throws IOException, JsonParseException
    {
        if (ch == 'I') {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOFInValue();
                }
            }
            ch = _inputBuffer[_inputPtr++];
            if (ch == 'N') {
                String match = negative ? "-INF" :"+INF";
                _matchToken(match, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            } else if (ch == 'n') {
                String match = negative ? "-Infinity" :"+Infinity";
                _matchToken(match, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
        }
        reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    /*
    /**********************************************************
    /* Internal methods, secondary parsing
    /**********************************************************
     */

    protected final String _parseFieldName(int i)
        throws IOException, JsonParseException
    {
        if (i != INT_QUOTE) {
            return _handleUnusualFieldName(i);
        }
        /* First: let's try to see if we have a simple name: one that does
         * not cross input buffer boundary, and does not contain escape
         * sequences.
         */
        int ptr = _inputPtr;
        int hash = 0;
        final int inputLen = _inputEnd;

        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;

            do {
                int ch = _inputBuffer[ptr];
                if (ch < maxCode && codes[ch] != 0) {
                    if (ch == '"') {
                        int start = _inputPtr;
                        _inputPtr = ptr+1; // to skip the quote
                        return _symbols.findSymbol(_inputBuffer, start, ptr - start, hash);
                    }
                    break;
                }
                hash = (hash * 31) + ch;
                ++ptr;
            } while (ptr < inputLen);
        }

        int start = _inputPtr;
        _inputPtr = ptr;
        return _parseFieldName2(start, hash, INT_QUOTE);
    }

    private String _parseFieldName2(int startPtr, int hash, int endChar)
        throws IOException, JsonParseException
    {
        _textBuffer.resetWithShared(_inputBuffer, startPtr, (_inputPtr - startPtr));

        /* Output pointers; calls will also ensure that the buffer is
         * not shared and has room for at least one more char.
         */
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(": was expecting closing '"+((char) endChar)+"' for name");
                }
            }
            char c = _inputBuffer[_inputPtr++];
            int i = (int) c;
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    /* Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    c = _decodeEscaped();
                } else if (i <= endChar) {
                    if (i == endChar) {
                        break;
                    }
                    if (i < INT_SPACE) {
                        _throwUnquotedSpace(i, "name");
                    }
                }
            }
            hash = (hash * 31) + i;
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;

            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        {
            TextBuffer tb = _textBuffer;
            char[] buf = tb.getTextBuffer();
            int start = tb.getTextOffset();
            int len = tb.size();

            return _symbols.findSymbol(buf, start, len, hash);
        }
    }

    /**
     * Method called when we see non-white space character other
     * than double quote, when expecting a field name.
     * In standard mode will just throw an expection; but
     * in non-standard modes may be able to parse name.
     *
     * @since 1.2
     */
    protected final String _handleUnusualFieldName(int i)
        throws IOException, JsonParseException
    {
        // [JACKSON-173]: allow single quotes
        if (i == INT_APOSTROPHE && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseApostropheFieldName();
        }
        // [JACKSON-69]: allow unquoted names if feature enabled:
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar(i, "was expecting double-quote to start field name");
        }
        final int[] codes = CharTypes.getInputCodeLatin1JsNames();
        final int maxCode = codes.length;

        // Also: first char must be a valid name char, but NOT be number
        boolean firstOk;

        if (i < maxCode) { // identifier, and not a number
            firstOk = (codes[i] == 0) && (i < INT_0 || i > INT_9);
        } else {
            firstOk = Character.isJavaIdentifierPart((char) i);
        }
        if (!firstOk) {
            _reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        int ptr = _inputPtr;
        int hash = 0;
        final int inputLen = _inputEnd;

        if (ptr < inputLen) {
            do {
                int ch = _inputBuffer[ptr];
                if (ch < maxCode) {
                    if (codes[ch] != 0) {
                        int start = _inputPtr-1; // -1 to bring back first char
                        _inputPtr = ptr;
                        return _symbols.findSymbol(_inputBuffer, start, ptr - start, hash);
                    }
                } else if (!Character.isJavaIdentifierPart((char) ch)) {
                    int start = _inputPtr-1; // -1 to bring back first char
                    _inputPtr = ptr;
                    return _symbols.findSymbol(_inputBuffer, start, ptr - start, hash);
                }
                hash = (hash * 31) + ch;
                ++ptr;
            } while (ptr < inputLen);
        }
        int start = _inputPtr-1;
        _inputPtr = ptr;
        return _parseUnusualFieldName2(start, hash, codes);
    }

    protected final String _parseApostropheFieldName()
        throws IOException, JsonParseException
    {
        // Note: mostly copy of_parseFieldName
        int ptr = _inputPtr;
        int hash = 0;
        final int inputLen = _inputEnd;

        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;

            do {
                int ch = _inputBuffer[ptr];
                if (ch == '\'') {
                    int start = _inputPtr;
                    _inputPtr = ptr+1; // to skip the quote
                    return _symbols.findSymbol(_inputBuffer, start, ptr - start, hash);
                }
                if (ch < maxCode && codes[ch] != 0) {
                    break;
                }
                hash = (hash * 31) + ch;
                ++ptr;
            } while (ptr < inputLen);
        }

        int start = _inputPtr;
        _inputPtr = ptr;

        return _parseFieldName2(start, hash, INT_APOSTROPHE);
    }

    /**
     * Method for handling cases where first non-space character
     * of an expected value token is not legal for standard JSON content.
     *
     * @since 1.3
     */
    protected final JsonToken _handleUnexpectedValue(int i)
        throws IOException, JsonParseException
    {
        // Most likely an error, unless we are to allow single-quote-strings
        switch (i) {
        case '\'':
            /* [JACKSON-173]: allow single quotes. Unlike with regular
             * Strings, we'll eagerly parse contents; this so that there's
             * no need to store information on quote char used.
             *
             * Also, no separation to fast/slow parsing; we'll just do
             * one regular (~= slowish) parsing, to keep code simple
             */
            if (isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
                return _handleApostropheValue();
            }
            break;
        case 'N':
            _matchToken("NaN", 1);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN("NaN", Double.NaN);
            }
            _reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            break;
        case '+': // note: '-' is taken as number
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOFInValue();
                }
            }
            return _handleInvalidNumberStart(_inputBuffer[_inputPtr++], false);
        }
        _reportUnexpectedChar(i, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
        return null;
    }
    
    /**
     * @since 1.8
     */
    protected final JsonToken _handleApostropheValue()
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value");
                }
            }
            char c = _inputBuffer[_inputPtr++];
            int i = (int) c;
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    /* Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    c = _decodeEscaped();
                } else if (i <= INT_APOSTROPHE) {
                    if (i == INT_APOSTROPHE) {
                        break;
                    }
                    if (i < INT_SPACE) {
                        _throwUnquotedSpace(i, "string value");
                    }
                }
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;
        }
        _textBuffer.setCurrentLength(outPtr);
        return JsonToken.VALUE_STRING;
    }
    
    /**
     * @since 1.2
     */
    private String _parseUnusualFieldName2(int startPtr, int hash, int[] codes)
        throws IOException, JsonParseException
    {
        _textBuffer.resetWithShared(_inputBuffer, startPtr, (_inputPtr - startPtr));
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();
        final int maxCode = codes.length;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) { // acceptable for now (will error out later)
                    break;
                }
            }
            char c = _inputBuffer[_inputPtr];
            int i = (int) c;
            if (i <= maxCode) {
                if (codes[i] != 0) {
                    break;
                }
            } else if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
            hash = (hash * 31) + i;
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;

            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        {
            TextBuffer tb = _textBuffer;
            char[] buf = tb.getTextBuffer();
            int start = tb.getTextOffset();
            int len = tb.size();

            return _symbols.findSymbol(buf, start, len, hash);
        }
    }
  
    @Override
    protected void _finishString()
        throws IOException, JsonParseException
    {
        /* First: let's try to see if we have simple String value: one
         * that does not cross input buffer boundary, and does not
         * contain escape sequences.
         */
        int ptr = _inputPtr;
        final int inputLen = _inputEnd;

        if (ptr < inputLen) {
            final int[] codes = CharTypes.getInputCodeLatin1();
            final int maxCode = codes.length;

            do {
                int ch = _inputBuffer[ptr];
                if (ch < maxCode && codes[ch] != 0) {
                    if (ch == '"') {
                        _textBuffer.resetWithShared(_inputBuffer, _inputPtr, (ptr-_inputPtr));
                        _inputPtr = ptr+1;
                        // Yes, we got it all
                        return;
                    }
                    break;
                }
                ++ptr;
            } while (ptr < inputLen);
        }

        /* Either ran out of input, or bumped into an escape
         * sequence...
         */
        _textBuffer.resetWithCopy(_inputBuffer, _inputPtr, (ptr-_inputPtr));
        _inputPtr = ptr;
        _finishString2();
    }

    protected void _finishString2()
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value");
                }
            }
            char c = _inputBuffer[_inputPtr++];
            int i = (int) c;
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    /* Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    c = _decodeEscaped();
                } else if (i <= INT_QUOTE) {
                    if (i == INT_QUOTE) {
                        break;
                    }
                    if (i < INT_SPACE) {
                        _throwUnquotedSpace(i, "string value");
                    }
                }
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;
        }
        _textBuffer.setCurrentLength(outPtr);
    }

    /**
     * Method called to skim through rest of unparsed String value,
     * if it is not needed. This can be done bit faster if contents
     * need not be stored for future access.
     */
    protected void _skipString()
        throws IOException, JsonParseException
    {
        _tokenIncomplete = false;

        int inputPtr = _inputPtr;
        int inputLen = _inputEnd;
        char[] inputBuffer = _inputBuffer;

        while (true) {
            if (inputPtr >= inputLen) {
                _inputPtr = inputPtr;
                if (!loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value");
                }
                inputPtr = _inputPtr;
                inputLen = _inputEnd;
            }
            char c = inputBuffer[inputPtr++];
            int i = (int) c;
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    /* Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    _inputPtr = inputPtr;
                    c = _decodeEscaped();
                    inputPtr = _inputPtr;
                    inputLen = _inputEnd;
                } else if (i <= INT_QUOTE) {
                    if (i == INT_QUOTE) {
                        _inputPtr = inputPtr;
                        break;
                    }
                    if (i < INT_SPACE) {
                        _inputPtr = inputPtr;
                        _throwUnquotedSpace(i, "string value");
                    }
                }
            }
        }
    }

    /*
    /**********************************************************
    /* Internal methods, other parsing
    /**********************************************************
     */
    
    /**
     * We actually need to check the character value here
     * (to see if we have \n following \r).
     */
    protected final void _skipCR() throws IOException
    {
        if (_inputPtr < _inputEnd || loadMore()) {
            if (_inputBuffer[_inputPtr] == '\n') {
                ++_inputPtr;
            }
        }
        ++_currInputRow;
        _currInputRowStart = _inputPtr;
    }

    protected final void _skipLF() throws IOException
    {
        ++_currInputRow;
        _currInputRowStart = _inputPtr;
    }

    private final int _skipWS()
        throws IOException, JsonParseException
    {
        while (_inputPtr < _inputEnd || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i != INT_SLASH) {
                    return i;
                }
                _skipComment();
            } else if (i != INT_SPACE) {
                if (i == INT_LF) {
                    _skipLF();
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        throw _constructError("Unexpected end-of-input within/between "+_parsingContext.getTypeDesc()+" entries");
    }

    private final int _skipWSOrEnd()
        throws IOException, JsonParseException
    {
        while ((_inputPtr < _inputEnd) || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                 if (i == INT_SLASH) {
                     _skipComment();
                     continue;
                }
                 return i;
            }
            if (i != INT_SPACE) {
                if (i == INT_LF) {
                    _skipLF();
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        // We ran out of input...
        _handleEOF();
        return -1;
    }

    private final void _skipComment()
        throws IOException, JsonParseException
    {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar('/', "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        // First: check which comment (if either) it is:
        if (_inputPtr >= _inputEnd && !loadMore()) {
            _reportInvalidEOF(" in a comment");
        }
        char c = _inputBuffer[_inputPtr++];
        if (c == '/') {
            _skipCppComment();
        } else if (c == '*') {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipCComment()
        throws IOException, JsonParseException
    {
        // Ok: need the matching '*/'
        main_loop:
        while ((_inputPtr < _inputEnd) || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++];
            if (i <= INT_ASTERISK) {
                if (i == INT_ASTERISK) { // end?
                    if ((_inputPtr >= _inputEnd) && !loadMore()) {
                        break main_loop;
                    }
                    if (_inputBuffer[_inputPtr] == INT_SLASH) {
                        ++_inputPtr;
                        return;
                    }
                    continue;
                }
                if (i < INT_SPACE) {
                    if (i == INT_LF) {
                        _skipLF();
                    } else if (i == INT_CR) {
                        _skipCR();
                    } else if (i != INT_TAB) {
                        _throwInvalidSpace(i);
                    }
                }
            }
        }
        _reportInvalidEOF(" in a comment");
    }

    private final void _skipCppComment()
        throws IOException, JsonParseException
    {
        // Ok: need to find EOF or linefeed
        while ((_inputPtr < _inputEnd) || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++];
            if (i < INT_SPACE) {
                if (i == INT_LF) {
                    _skipLF();
                    break;
                } else if (i == INT_CR) {
                    _skipCR();
                    break;
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
    }

    @Override
    protected final char _decodeEscaped()
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                _reportInvalidEOF(" in character escape sequence");
            }
        }
        char c = _inputBuffer[_inputPtr++];

        switch ((int) c) {
            // First, ones that are mapped
        case INT_b:
            return '\b';
        case INT_t:
            return '\t';
        case INT_n:
            return '\n';
        case INT_f:
            return '\f';
        case INT_r:
            return '\r';

            // And these are to be returned as they are
        case INT_QUOTE:
        case INT_SLASH:
        case INT_BACKSLASH:
            return c;

        case INT_u: // and finally hex-escaped
            break;

        default:
            return _handleUnrecognizedCharacterEscape(c);
        }

        // Ok, a hex escape. Need 4 characters
        int value = 0;
        for (int i = 0; i < 4; ++i) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(" in character escape sequence");
                }
            }
            int ch = (int) _inputBuffer[_inputPtr++];
            int digit = CharTypes.charToHex(ch);
            if (digit < 0) {
                _reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
            }
            value = (value << 4) | digit;
        }
        return (char) value;
    }
    
    /**
     * Helper method for checking whether input matches expected token
     * 
     * @since 1.8
     */
    protected final void _matchToken(String matchStr, int i)
        throws IOException, JsonParseException
    {
        final int len = matchStr.length();

        do {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOFInValue();
                }
            }
            if (_inputBuffer[_inputPtr] != matchStr.charAt(i)) {
                _reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
            }
            ++_inputPtr;
        } while (++i < len);

        // but let's also ensure we either get EOF, or non-alphanum char...
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                return;
            }
        }
        char c = _inputBuffer[_inputPtr];
        if (c < '0' || c == ']' || c == '}') { // expected/allowed chars
            return;
        }
        // if Java letter, it's a problem tho
        if (Character.isJavaIdentifierPart(c)) {
            ++_inputPtr;
            _reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
        }
        return;
    }

    /*
    /**********************************************************
    /* Binary access
    /**********************************************************
     */

    /**
     * Efficient handling for incremental parsing of base64-encoded
     * textual content.
     */
    protected byte[] _decodeBase64(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        ByteArrayBuilder builder = _getByteArrayBuilder();

        //main_loop:
        while (true) {
            // first, we'll skip preceding white space, if any
            char ch;
            do {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                ch = _inputBuffer[_inputPtr++];
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (ch == '"') { // reached the end, fair and square?
                    return builder.toByteArray();
                }
                bits = _decodeBase64Escape(b64variant, ch, 0);
                if (bits < 0) { // white space to skip
                    continue;
                }
            }
            int decodedData = bits;
            
            // then second base64 char; can't get padding yet, nor ws
            
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;
            
            // third base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // as per [JACKSON-631], could also just be 'missing'  padding
                    if (ch == '"' && !b64variant.usesPadding()) {
                        decodedData >>= 4;
                        builder.append(decodedData);
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 2);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // Ok, must get more padding chars, then
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    ch = _inputBuffer[_inputPtr++];
                    if (!b64variant.usesPaddingChar(ch)) {
                        throw reportInvalidBase64Char(b64variant, ch, 3, "expected padding character '"+b64variant.getPaddingChar()+"'");
                    }
                    // Got 12 bits, only need 8, need to shift
                    decodedData >>= 4;
                    builder.append(decodedData);
                    continue;
                }
                // otherwise we got escaped other char, to be processed below
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // as per [JACKSON-631], could also just be 'missing'  padding
                    if (ch == '"' && !b64variant.usesPadding()) {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 3);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // With padding we only get 2 bytes; but we have
                    // to shift it a bit so it is identical to triplet
                    // case with partial output.
                    // 3 chars gives 3x6 == 18 bits, of which 2 are
                    // dummies, need to discard:
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    continue;
                }
                // otherwise we got escaped other char, to be processed below
            }
            // otherwise, our triplet is now complete
            decodedData = (decodedData << 6) | bits;
            builder.appendThreeBytes(decodedData);
        }
    }

    /*
    /**********************************************************
    /* Error reporting
    /**********************************************************
     */

    protected void _reportInvalidToken(String matchedPart, String msg)
        throws IOException, JsonParseException
    {
        StringBuilder sb = new StringBuilder(matchedPart);
        /* Let's just try to find what appears to be the token, using
         * regular Java identifier character rules. It's just a heuristic,
         * nothing fancy here.
         */
        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    break;
                }
            }
            char c = _inputBuffer[_inputPtr];
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
            sb.append(c);
        }
        _reportError("Unrecognized token '"+sb.toString()+"': was expecting ");
    }
}
