package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.IOContext;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.*;

/**
 * This is a concrete implementation of {@link JsonParser}, which is
 * based on a {@link java.io.InputStream} as the input source.
 */
public final class Utf8StreamParser
    extends JsonParserBase
{
    final static byte BYTE_LF = (byte) '\n';

    private final static int[] sInputCodesUtf8 = CharTypes.getInputCodeUtf8();

    /**
     * Latin1 encoding is not supported, but we do use 8-bit subset for
     * pre-processing task, to simplify first pass, keep it fast.
     */
    private final static int[] sInputCodesLatin1 = CharTypes.getInputCodeLatin1();
    
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Codec used for data binding when (if) requested; typically full
     * <code>ObjectMapper</code>, but that abstract is not part of core
     * package.
     */
    protected ObjectCodec _objectCodec;

    /**
     * Symbol table that contains field names encountered so far
     */
    final protected BytesToNameCanonicalizer _symbols;
    
    /*
    /**********************************************************
    /* Parsing state
    /**********************************************************
     */
    
    /**
     * Temporary buffer used for name parsing.
     */
    protected int[] _quadBuffer = new int[16];

    /**
     * Flag that indicates that the current token has not yet
     * been fully processed, and needs to be finished for
     * some access (or skipped to obtain the next token)
     */
    protected boolean _tokenIncomplete = false;

    /**
     * Temporary storage for partially parsed name bytes.
     */
    private int _quad1;
    
    /*
    /**********************************************************
    /* Input buffering (from former 'StreamBasedParserBase')
    /**********************************************************
     */
    
    protected InputStream _inputStream;

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
     */

    /**
     * Current buffer from which data is read; generally data is read into
     * buffer from input source, but in some cases pre-loaded buffer
     * is handed to the parser.
     */
    protected byte[] _inputBuffer;

    /**
     * Flag that indicates whether the input buffer is recycable (and
     * needs to be returned to recycler once we are done) or not.
     *<p>
     * If it is not, it also means that parser can NOT modify underlying
     * buffer.
     */
    protected boolean _bufferRecyclable;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public Utf8StreamParser(IOContext ctxt, int features, InputStream in,
            ObjectCodec codec, BytesToNameCanonicalizer sym,
            byte[] inputBuffer, int start, int end,
            boolean bufferRecyclable)
    {
        super(ctxt, features);
        _inputStream = in;
        _objectCodec = codec;
        _symbols = sym;
        _inputBuffer = inputBuffer;
        _inputPtr = start;
        _inputEnd = end;
        _bufferRecyclable = bufferRecyclable;
        // 12-Mar-2010, tatus: Sanity check, related to [JACKSON-259]:
        if (!JsonParser.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(features)) {
            // should never construct non-canonical UTF-8/byte parser (instead, use Reader)
            _throwInternal();
        }
    }

    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    /*
    /**********************************************************
    /* Former StreamBasedParserBase methods
    /**********************************************************
     */

    @Override
    public int releaseBuffered(OutputStream out) throws IOException
    {
        int count = _inputEnd - _inputPtr;
        if (count < 1) {
            return 0;
        }
        // let's just advance ptr to end
        int origPtr = _inputPtr;
        out.write(_inputBuffer, origPtr, count);
        return count;
    }

    @Override
    public Object getInputSource() {
        return _inputStream;
    }
    
    /*
    /**********************************************************
    /* Low-level reading, other
    /**********************************************************
     */

    @Override
    protected final boolean loadMore()
        throws IOException
    {
        _currInputProcessed += _inputEnd;
        _currInputRowStart -= _inputEnd;
        
        if (_inputStream != null) {
            int count = _inputStream.read(_inputBuffer, 0, _inputBuffer.length);
            if (count > 0) {
                _inputPtr = 0;
                _inputEnd = count;
                return true;
            }
            // End of input
            _closeInput();
            // Should never return 0, so let's fail
            if (count == 0) {
                throw new IOException("InputStream.read() returned 0 characters when trying to read "+_inputBuffer.length+" bytes");
            }
        }
        return false;
    }

    /**
     * Helper method that will try to load at least specified number bytes in
     * input buffer, possible moving existing data around if necessary
     * 
     * @since 1.6
     */
    protected final boolean _loadToHaveAtLeast(int minAvailable)
        throws IOException
    {
        // No input stream, no leading (either we are closed, or have non-stream input source)
        if (_inputStream == null) {
            return false;
        }
        // Need to move remaining data in front?
        int amount = _inputEnd - _inputPtr;
        if (amount > 0 && _inputPtr > 0) {
            _currInputProcessed += _inputPtr;
            _currInputRowStart -= _inputPtr;
            System.arraycopy(_inputBuffer, _inputPtr, _inputBuffer, 0, amount);
            _inputEnd = amount;
        } else {
            _inputEnd = 0;
        }
        _inputPtr = 0;
        while (_inputEnd < minAvailable) {
            int count = _inputStream.read(_inputBuffer, _inputEnd, _inputBuffer.length - _inputEnd);
            if (count < 1) {
                // End of input
                _closeInput();
                // Should never return 0, so let's fail
                if (count == 0) {
                    throw new IOException("InputStream.read() returned 0 characters when trying to read "+amount+" bytes");
                }
                return false;
            }
            _inputEnd += count;
        }
        return true;
    }
    
    @Override
    protected void _closeInput() throws IOException
    {
        /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
         *   on the underlying InputStream, unless we "own" it, or auto-closing
         *   feature is enabled.
         */
        if (_inputStream != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                _inputStream.close();
            }
            _inputStream = null;
        }
    }

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     */
    @Override
    protected void _releaseBuffers() throws IOException
    {
        super._releaseBuffers();
        if (_bufferRecyclable) {
            byte[] buf = _inputBuffer;
            if (buf != null) {
                _inputBuffer = null;
                _ioContext.releaseReadIOBuffer(buf);
            }
        }
    }

    /*
    /**********************************************************
    /* Public API, data access
    /**********************************************************
     */

    @Override
    public String getText()
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
    /* Public API, traversal, basic
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
        if (!_parsingContext.inObject()) {
            return _nextTokenNotInObject(i);
        }
        // So first parse the field name itself:
        Name n = _parseFieldName(i);
        _parsingContext.setCurrentName(n.getName());
        _currToken = JsonToken.FIELD_NAME;
        i = _skipWS();
        if (i != INT_COLON) {
            _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
        }
        i = _skipWS();

        // Ok: we must have a value... what is it? Strings are very common, check first:
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return _currToken;
        }        
        JsonToken t;

        switch (i) {
        case INT_LBRACKET:
            t = JsonToken.START_ARRAY;
            break;
        case INT_LCURLY:
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
        }
        _nextToken = t;
        return _currToken;
    }

    private final JsonToken _nextTokenNotInObject(int i)
        throws IOException, JsonParseException
    {
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            return (_currToken = JsonToken.VALUE_STRING);
        }
        switch (i) {
        case INT_LBRACKET:
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            return (_currToken = JsonToken.START_ARRAY);
        case INT_LCURLY:
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            return (_currToken = JsonToken.START_OBJECT);
        case INT_RBRACKET:
        case INT_RCURLY:
            // Error: neither is valid at this point; valid closers have
            // been handled earlier
            _reportUnexpectedChar(i, "expected a value");
        case INT_t:
            _matchToken("true", 1);
            return (_currToken = JsonToken.VALUE_TRUE);
        case INT_f:
            _matchToken("false", 1);
            return (_currToken = JsonToken.VALUE_FALSE);
        case INT_n:
            _matchToken("null", 1);
            return (_currToken = JsonToken.VALUE_NULL);
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
            return (_currToken = parseNumberText(i));
        }
        return (_currToken = _handleUnexpectedValue(i));
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

    @Override
    public void close() throws IOException
    {
        super.close();
        // Merge found symbols, if any:
        _symbols.release();
    }
    
    /*
    /**********************************************************
    /* Public API, traversal, nextXxxValue/nextFieldName
    /**********************************************************
     */
    
    @Override
    public boolean nextFieldName(SerializableString str)
        throws IOException, JsonParseException
    {
        // // // Note: most of code below is copied from nextToken()
        
        _numTypesValid = NR_UNKNOWN;
        if (_currToken == JsonToken.FIELD_NAME) { // can't have name right after name
            _nextAfterName();
            return false;
        }
        if (_tokenIncomplete) {
            _skipString();
        }
        int i = _skipWSOrEnd();
        if (i < 0) { // end-of-input
            close();
            _currToken = null;
            return false;
        }
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
            _currToken = JsonToken.END_ARRAY;
            return false;
        }
        if (i == INT_RCURLY) {
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.getParent();
            _currToken = JsonToken.END_OBJECT;
            return false;
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            if (i != INT_COMMA) {
                _reportUnexpectedChar(i, "was expecting comma to separate "+_parsingContext.getTypeDesc()+" entries");
            }
            i = _skipWS();
        }

        if (!_parsingContext.inObject()) {
            _nextTokenNotInObject(i);
            return false;
        }
        
        // // // This part differs, name parsing
        if (i == INT_QUOTE) {
            // when doing literal match, must consider escaping:
            byte[] nameBytes = str.asQuotedUTF8();
            final int len = nameBytes.length;
            if ((_inputPtr + len) < _inputEnd) { // maybe...
                // first check length match by
                final int end = _inputPtr+len;
                if (_inputBuffer[end] == INT_QUOTE) {
                    int offset = 0;
                    final int ptr = _inputPtr;
                    while (true) {
                        if (offset == len) { // yes, match!
                            _inputPtr = end+1; // skip current value first
                            // First part is simple; setting of name
                            _parsingContext.setCurrentName(str.getValue());
                            _currToken = JsonToken.FIELD_NAME;
                            // But then we also must handle following value etc
                            _isNextTokenNameYes();
                            return true;
                        }
                        if (nameBytes[offset] != _inputBuffer[ptr+offset]) {
                            break;
                        }
                        ++offset;
                    }
                }
            }
        }
        _isNextTokenNameNo(i);
        return false;
    }

    private final void _isNextTokenNameYes()
        throws IOException, JsonParseException
    {
        // very first thing: common case, colon, value, no white space
        int i;
        if (_inputPtr < _inputEnd && _inputBuffer[_inputPtr] == INT_COLON) { // fast case first
            ++_inputPtr;
            i = _inputBuffer[_inputPtr++];
            if (i == INT_QUOTE) {
                _tokenIncomplete = true;
                _nextToken = JsonToken.VALUE_STRING;
                return;
            }
            if (i == INT_LCURLY) {
                _nextToken = JsonToken.START_OBJECT;
                return;
            }
            if (i == INT_LBRACKET) {
                _nextToken = JsonToken.START_ARRAY;
                return;
            }
            i &= 0xFF;
            if (i <= INT_SPACE || i == INT_SLASH) {
                --_inputPtr;
                i = _skipWS();
            }
        } else {
            i = _skipColon();
        }
        switch (i) {
        case INT_QUOTE:
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return;
        case INT_LBRACKET:
            _nextToken = JsonToken.START_ARRAY;
            return;
        case INT_LCURLY:
            _nextToken = JsonToken.START_OBJECT;
            return;
        case INT_RBRACKET:
        case INT_RCURLY:
            _reportUnexpectedChar(i, "expected a value");
        case INT_t:
            _matchToken("true", 1);
            _nextToken = JsonToken.VALUE_TRUE;
            return;
        case INT_f:
            _matchToken("false", 1);
            _nextToken = JsonToken.VALUE_FALSE;
            return;
        case INT_n:
            _matchToken("null", 1);
            _nextToken = JsonToken.VALUE_NULL;
            return;
        case INT_MINUS:
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
            _nextToken = parseNumberText(i);
            return;
        }
        _nextToken = _handleUnexpectedValue(i);
    }
    
    private final void _isNextTokenNameNo(int i)
            throws IOException, JsonParseException
    {
        // // // and this is back to standard nextToken()
            
        Name n = _parseFieldName(i);
        _parsingContext.setCurrentName(n.getName());
        _currToken = JsonToken.FIELD_NAME;
        i = _skipWS();
        if (i != INT_COLON) {
            _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
        }
        i = _skipWS();

        // Ok: we must have a value... what is it? Strings are very common, check first:
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return;
        }        
        JsonToken t;

        switch (i) {
        case INT_LBRACKET:
            t = JsonToken.START_ARRAY;
            break;
        case INT_LCURLY:
            t = JsonToken.START_OBJECT;
            break;
        case INT_RBRACKET:
        case INT_RCURLY:
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
        }
        _nextToken = t;
    }

    @Override
    public String nextTextValue()
        throws IOException, JsonParseException
    {
        // two distinct cases; either got name and we know next type, or 'other'
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

    @Override
    public int nextIntValue(int defaultValue)
        throws IOException, JsonParseException
    {
        // two distinct cases; either got name and we know next type, or 'other'
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
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

    @Override
    public long nextLongValue(long defaultValue)
        throws IOException, JsonParseException
    {
        // two distinct cases; either got name and we know next type, or 'other'
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

    @Override
    public Boolean nextBooleanValue()
        throws IOException, JsonParseException
    {
        // two distinct cases; either got name and we know next type, or 'other'
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
    
    /*
    /**********************************************************
    /* Internal methods, number parsing
    /* (note: in 1.6 and prior, part of "Utf8NumericParser"
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
    protected final JsonToken parseNumberText(int c)
        throws IOException, JsonParseException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        boolean negative = (c == INT_MINUS);

        // Need to prepend sign?
        if (negative) {
            outBuf[outPtr++] = '-';
            // Must have something after sign too
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Note: must be followed by a digit
            if (c < INT_0 || c > INT_9) {
                return _handleInvalidNumberStart(c, true);
            }
        }

        // One special case: if first char is 0, must not be followed by a digit
        if (c == INT_0) {
            c = _verifyNoLeadingZeroes();
        }
        
        // Ok: we can first just add digit we saw first:
        outBuf[outPtr++] = (char) c;
        int intLen = 1;

        // And then figure out how far we can read without further checks:
        int end = _inputPtr + outBuf.length;
        if (end > _inputEnd) {
            end = _inputEnd;
        }

        // With this, we have a nice and tight loop:
        while (true) {
            if (_inputPtr >= end) {
                // Long enough to be split across boundary, so:
                return _parserNumber2(outBuf, outPtr, negative, intLen);
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                break;
            }
            ++intLen;
            outBuf[outPtr++] = (char) c;
        }
        if (c == '.' || c == 'e' || c == 'E') {
            return _parseFloatText(outBuf, outPtr, c, negative, intLen);
        }

        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetInt(negative, intLen);
    }
    
    /**
     * Method called to handle parsing when input is split across buffer boundary
     * (or output is longer than segment used to store it)
     */
    private final JsonToken _parserNumber2(char[] outBuf, int outPtr, boolean negative,
            int intPartLength)
        throws IOException, JsonParseException
    {
        // Ok, parse the rest
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuffer.setCurrentLength(outPtr);
                return resetInt(negative, intPartLength);
            }
            int c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (c > INT_9 || c < INT_0) {
                if (c == '.' || c == 'e' || c == 'E') {
                    return _parseFloatText(outBuf, outPtr, c, negative, intPartLength);
                }
                break;
            }
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            ++intPartLength;
        }
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetInt(negative, intPartLength);
        
    }
    
    /**
     * Method called when we have seen one zero, and want to ensure
     * it is not followed by another
     */
    private final int _verifyNoLeadingZeroes()
        throws IOException, JsonParseException
    {
        // Ok to have plain "0"
        if (_inputPtr >= _inputEnd && !loadMore()) {
            return INT_0;
        }
        int ch = _inputBuffer[_inputPtr] & 0xFF;
        // if not followed by a number (probably '.'); return zero as is, to be included
        if (ch < INT_0 || ch > INT_9) {
            return INT_0;
        }
        // [JACKSON-358]: we may want to allow them, after all...
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        // if so, just need to skip either all zeroes (if followed by number); or all but one (if non-number)
        ++_inputPtr; // Leading zero to be skipped
        if (ch == INT_0) {
            while (_inputPtr < _inputEnd || loadMore()) {
                ch = _inputBuffer[_inputPtr] & 0xFF;
                if (ch < INT_0 || ch > INT_9) { // followed by non-number; retain one zero
                    return INT_0;
                }
                ++_inputPtr; // skip previous zeroes
                if (ch != INT_0) { // followed by other number; return 
                    break;
                }
            }
        }
        return ch;
    }
    
    private final JsonToken _parseFloatText(char[] outBuf, int outPtr, int c,
            boolean negative, int integerPartLength)
        throws IOException, JsonParseException
    {
        int fractLen = 0;
        boolean eof = false;

        // And then see if we get other parts
        if (c == '.') { // yes, fraction
            outBuf[outPtr++] = (char) c;

            fract_loop:
            while (true) {
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break fract_loop;
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
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
            outBuf[outPtr++] = (char) c;
            // Not optional, can require that we get one more char
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                // Likewise, non optional:
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            }

            exp_loop:
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                if (_inputPtr >= _inputEnd && !loadMore()) {
                    eof = true;
                    break exp_loop;
                }
                c = (int) _inputBuffer[_inputPtr++] & 0xFF;
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
        return resetFloat(negative, integerPartLength, fractLen, expLen);
    }
    
    /*
    /**********************************************************
    /* Internal methods, secondary parsing
    /**********************************************************
     */
    
    protected final Name _parseFieldName(int i)
        throws IOException, JsonParseException
    {
        if (i != INT_QUOTE) {
            return _handleUnusualFieldName(i);
        }
        // First: can we optimize out bounds checks?
        if ((_inputPtr + 9) > _inputEnd) { // Need 8 chars, plus one trailing (quote)
            return slowParseFieldName();
        }

        // If so, can also unroll loops nicely
        /* 25-Nov-2008, tatu: This may seem weird, but here we do
         *   NOT want to worry about UTF-8 decoding. Rather, we'll
         *   assume that part is ok (if not it will get caught
         *   later on), and just handle quotes and backslashes here.
         */
        final byte[] input = _inputBuffer;
        final int[] codes = sInputCodesLatin1;

        int q = input[_inputPtr++] & 0xFF;

        if (codes[q] == 0) {
            i = input[_inputPtr++] & 0xFF;
            if (codes[i] == 0) {
                q = (q << 8) | i;
                i = input[_inputPtr++] & 0xFF;
                if (codes[i] == 0) {
                    q = (q << 8) | i;
                    i = input[_inputPtr++] & 0xFF;
                    if (codes[i] == 0) {
                        q = (q << 8) | i;
                        i = input[_inputPtr++] & 0xFF;
                        if (codes[i] == 0) {
                            _quad1 = q;
                            return parseMediumFieldName(i, codes);
                        }
                        if (i == INT_QUOTE) { // one byte/char case or broken
                            return findName(q, 4);
                        }
                        return parseFieldName(q, i, 4);
                    }
                    if (i == INT_QUOTE) { // one byte/char case or broken
                        return findName(q, 3);
                    }
                    return parseFieldName(q, i, 3);
                }                
                if (i == INT_QUOTE) { // one byte/char case or broken
                    return findName(q, 2);
                }
                return parseFieldName(q, i, 2);
            }
            if (i == INT_QUOTE) { // one byte/char case or broken
                return findName(q, 1);
            }
            return parseFieldName(q, i, 1);
        }     
        if (q == INT_QUOTE) { // special case, ""
            return BytesToNameCanonicalizer.getEmptyName();
        }
        return parseFieldName(0, q, 0); // quoting or invalid char
    }

    protected final Name parseMediumFieldName(int q2, final int[] codes)
        throws IOException, JsonParseException
    {
        // Ok, got 5 name bytes so far
        int i = _inputBuffer[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 5 bytes
                return findName(_quad1, q2, 1);
            }
            return parseFieldName(_quad1, q2, i, 1); // quoting or invalid char
        }
        q2 = (q2 << 8) | i;
        i = _inputBuffer[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 6 bytes
                return findName(_quad1, q2, 2);
            }
            return parseFieldName(_quad1, q2, i, 2);
        }
        q2 = (q2 << 8) | i;
        i = _inputBuffer[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 7 bytes
                return findName(_quad1, q2, 3);
            }
            return parseFieldName(_quad1, q2, i, 3);
        }
        q2 = (q2 << 8) | i;
        i = _inputBuffer[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 8 bytes
                return findName(_quad1, q2, 4);
            }
            return parseFieldName(_quad1, q2, i, 4);
        }
        _quadBuffer[0] = _quad1;
        _quadBuffer[1] = q2;
        return parseLongFieldName(i);
    }

    protected Name parseLongFieldName(int q)
        throws IOException, JsonParseException
    {
        // As explained above, will ignore UTF-8 encoding at this point
        final int[] codes = sInputCodesLatin1;
        int qlen = 2;

        while (true) {
            /* Let's offline if we hit buffer boundary (otherwise would
             * need to [try to] align input, which is bit complicated
             * and may not always be possible)
             */
            if ((_inputEnd - _inputPtr) < 4) {
                return parseEscapedFieldName(_quadBuffer, qlen, 0, q, 0);
            }
            // Otherwise can skip boundary checks for 4 bytes in loop

            int i = _inputBuffer[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 1);
                }
                return parseEscapedFieldName(_quadBuffer, qlen, q, i, 1);
            }

            q = (q << 8) | i;
            i = _inputBuffer[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 2);
                }
                return parseEscapedFieldName(_quadBuffer, qlen, q, i, 2);
            }

            q = (q << 8) | i;
            i = _inputBuffer[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 3);
                }
                return parseEscapedFieldName(_quadBuffer, qlen, q, i, 3);
            }

            q = (q << 8) | i;
            i = _inputBuffer[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 4);
                }
                return parseEscapedFieldName(_quadBuffer, qlen, q, i, 4);
            }

            // Nope, no end in sight. Need to grow quad array etc
            if (qlen >= _quadBuffer.length) {
                _quadBuffer = growArrayBy(_quadBuffer, qlen);
            }
            _quadBuffer[qlen++] = q;
            q = i;
        }
    }

    /**
     * Method called when not even first 8 bytes are guaranteed
     * to come consequtively. Happens rarely, so this is offlined;
     * plus we'll also do full checks for escaping etc.
     */
    protected Name slowParseFieldName()
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                _reportInvalidEOF(": was expecting closing '\"' for name");
            }
        }
        int i = _inputBuffer[_inputPtr++] & 0xFF;
        if (i == INT_QUOTE) { // special case, ""
            return BytesToNameCanonicalizer.getEmptyName();
        }
        return parseEscapedFieldName(_quadBuffer, 0, 0, i, 0);
    }

    private final Name parseFieldName(int q1, int ch, int lastQuadBytes)
        throws IOException, JsonParseException
    {
        return parseEscapedFieldName(_quadBuffer, 0, q1, ch, lastQuadBytes);
    }

    private final Name parseFieldName(int q1, int q2, int ch, int lastQuadBytes)
        throws IOException, JsonParseException
    {
        _quadBuffer[0] = q1;
        return parseEscapedFieldName(_quadBuffer, 1, q2, ch, lastQuadBytes);
    }

    /**
     * Slower parsing method which is generally branched to when
     * an escape sequence is detected (or alternatively for long
     * names, or ones crossing input buffer boundary). In any case,
     * needs to be able to handle more exceptional cases, gets
     * slower, and hance is offlined to a separate method.
     */
    protected Name parseEscapedFieldName(int[] quads, int qlen, int currQuad, int ch,
                                         int currQuadBytes)
        throws IOException, JsonParseException
    {
        /* 25-Nov-2008, tatu: This may seem weird, but here we do
         *   NOT want to worry about UTF-8 decoding. Rather, we'll
         *   assume that part is ok (if not it will get caught
         *   later on), and just handle quotes and backslashes here.
         */
        final int[] codes = sInputCodesLatin1;

        while (true) {
            if (codes[ch] != 0) {
                if (ch == INT_QUOTE) { // we are done
                    break;
                }
                // Unquoted white space?
                if (ch != INT_BACKSLASH) {
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(ch, "name");
                } else {
                    // Nope, escape sequence
                    ch = _decodeEscaped();
                }
                /* Oh crap. May need to UTF-8 (re-)encode it, if it's
                 * beyond 7-bit ascii. Gets pretty messy.
                 * If this happens often, may want to use different name
                 * canonicalization to avoid these hits.
                 */
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = growArrayBy(quads, quads.length);
                        }
                        quads[qlen++] = currQuad;
                        currQuad = 0;
                        currQuadBytes = 0;
                    }
                    if (ch < 0x800) { // 2-byte
                        currQuad = (currQuad << 8) | (0xc0 | (ch >> 6));
                        ++currQuadBytes;
                        // Second byte gets output below:
                    } else { // 3 bytes; no need to worry about surrogates here
                        currQuad = (currQuad << 8) | (0xe0 | (ch >> 12));
                        ++currQuadBytes;
                        // need room for middle byte?
                        if (currQuadBytes >= 4) {
                            if (qlen >= quads.length) {
                                _quadBuffer = quads = growArrayBy(quads, quads.length);
                            }
                            quads[qlen++] = currQuad;
                            currQuad = 0;
                            currQuadBytes = 0;
                        }
                        currQuad = (currQuad << 8) | (0x80 | ((ch >> 6) & 0x3f));
                        ++currQuadBytes;
                    }
                    // And same last byte in both cases, gets output below:
                    ch = 0x80 | (ch & 0x3f);
                }
            }
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(" in field name");
                }
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = growArrayBy(quads, quads.length);
            }
            quads[qlen++] = currQuad;
        }
        Name name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /**
     * Method called when we see non-white space character other
     * than double quote, when expecting a field name.
     * In standard mode will just throw an expection; but
     * in non-standard modes may be able to parse name.
     */
    protected final Name _handleUnusualFieldName(int ch)
        throws IOException, JsonParseException
    {
        // [JACKSON-173]: allow single quotes
        if (ch == INT_APOSTROPHE && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseApostropheFieldName();
        }
        // [JACKSON-69]: allow unquoted names if feature enabled:
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar(ch, "was expecting double-quote to start field name");
        }
        /* Also: note that although we use a different table here,
         * it does NOT handle UTF-8 decoding. It'll just pass those
         * high-bit codes as acceptable for later decoding.
         */
        final int[] codes = CharTypes.getInputCodeUtf8JsNames();
        // Also: must start with a valid character...
        if (codes[ch] != 0) {
            _reportUnexpectedChar(ch, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }

        /* Ok, now; instead of ultra-optimizing parsing here (as with
         * regular JSON names), let's just use the generic "slow"
         * variant. Can measure its impact later on if need be
         */
        int[] quads = _quadBuffer;
        int qlen = 0;
        int currQuad = 0;
        int currQuadBytes = 0;

        while (true) {
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(" in field name");
                }
            }
            ch = _inputBuffer[_inputPtr] & 0xFF;
            if (codes[ch] != 0) {
                break;
            }
            ++_inputPtr;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = growArrayBy(quads, quads.length);
            }
            quads[qlen++] = currQuad;
        }
        Name name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /* Parsing to support [JACKSON-173]. Plenty of duplicated code;
     * main reason being to try to avoid slowing down fast path
     * for valid JSON -- more alternatives, more code, generally
     * bit slower execution.
     */
    protected final Name _parseApostropheFieldName()
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                _reportInvalidEOF(": was expecting closing '\'' for name");
            }
        }
        int ch = _inputBuffer[_inputPtr++] & 0xFF;
        if (ch == INT_APOSTROPHE) { // special case, ''
            return BytesToNameCanonicalizer.getEmptyName();
        }
        int[] quads = _quadBuffer;
        int qlen = 0;
        int currQuad = 0;
        int currQuadBytes = 0;

        // Copied from parseEscapedFieldName, with minor mods:

        final int[] codes = sInputCodesLatin1;

        while (true) {
            if (ch == INT_APOSTROPHE) {
                break;
            }
            // additional check to skip handling of double-quotes
            if (ch != INT_QUOTE && codes[ch] != 0) {
                if (ch != INT_BACKSLASH) {
                    // Unquoted white space?
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(ch, "name");
                } else {
                    // Nope, escape sequence
                    ch = _decodeEscaped();
                }
                /* Oh crap. May need to UTF-8 (re-)encode it, if it's
                 * beyond 7-bit ascii. Gets pretty messy.
                 * If this happens often, may want to use different name
                 * canonicalization to avoid these hits.
                 */
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = growArrayBy(quads, quads.length);
                        }
                        quads[qlen++] = currQuad;
                        currQuad = 0;
                        currQuadBytes = 0;
                    }
                    if (ch < 0x800) { // 2-byte
                        currQuad = (currQuad << 8) | (0xc0 | (ch >> 6));
                        ++currQuadBytes;
                        // Second byte gets output below:
                    } else { // 3 bytes; no need to worry about surrogates here
                        currQuad = (currQuad << 8) | (0xe0 | (ch >> 12));
                        ++currQuadBytes;
                        // need room for middle byte?
                        if (currQuadBytes >= 4) {
                            if (qlen >= quads.length) {
                                _quadBuffer = quads = growArrayBy(quads, quads.length);
                            }
                            quads[qlen++] = currQuad;
                            currQuad = 0;
                            currQuadBytes = 0;
                        }
                        currQuad = (currQuad << 8) | (0x80 | ((ch >> 6) & 0x3f));
                        ++currQuadBytes;
                    }
                    // And same last byte in both cases, gets output below:
                    ch = 0x80 | (ch & 0x3f);
                }
            }
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(" in field name");
                }
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = growArrayBy(quads, quads.length);
            }
            quads[qlen++] = currQuad;
        }
        Name name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /*
    /**********************************************************
    /* Internal methods, symbol (name) handling
    /**********************************************************
     */

    private final Name findName(int q1, int lastQuadBytes)
        throws JsonParseException
    {
        // Usually we'll find it from the canonical symbol table already
        Name name = _symbols.findName(q1);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        return addName(_quadBuffer, 1, lastQuadBytes);
    }

    private final Name findName(int q1, int q2, int lastQuadBytes)
        throws JsonParseException
    {
        // Usually we'll find it from the canonical symbol table already
        Name name = _symbols.findName(q1, q2);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        _quadBuffer[1] = q2;
        return addName(_quadBuffer, 2, lastQuadBytes);
    }

    private final Name findName(int[] quads, int qlen, int lastQuad, int lastQuadBytes)
        throws JsonParseException
    {
        if (qlen >= quads.length) {
            _quadBuffer = quads = growArrayBy(quads, quads.length);
        }
        quads[qlen++] = lastQuad;
        Name name = _symbols.findName(quads, qlen);
        if (name == null) {
            return addName(quads, qlen, lastQuadBytes);
        }
        return name;
    }

    /**
     * This is the main workhorse method used when we take a symbol
     * table miss. It needs to demultiplex individual bytes, decode
     * multi-byte chars (if any), and then construct Name instance
     * and add it to the symbol table.
     */
    private final Name addName(int[] quads, int qlen, int lastQuadBytes)
        throws JsonParseException
    {
        /* Ok: must decode UTF-8 chars. No other validation is
         * needed, since unescaping has been done earlier as necessary
         * (as well as error reporting for unescaped control chars)
         */
        // 4 bytes per quad, except last one maybe less
        int byteLen = (qlen << 2) - 4 + lastQuadBytes;

        /* And last one is not correctly aligned (leading zero bytes instead
         * need to shift a bit, instead of trailing). Only need to shift it
         * for UTF-8 decoding; need revert for storage (since key will not
         * be aligned, to optimize lookup speed)
         */
        int lastQuad;

        if (lastQuadBytes < 4) {
            lastQuad = quads[qlen-1];
            // 8/16/24 bit left shift
            quads[qlen-1] = (lastQuad << ((4 - lastQuadBytes) << 3));
        } else {
            lastQuad = 0;
        }

        // Need some working space, TextBuffer works well:
        char[] cbuf = _textBuffer.emptyAndGetCurrentSegment();
        int cix = 0;

        for (int ix = 0; ix < byteLen; ) {
            int ch = quads[ix >> 2]; // current quad, need to shift+mask
            int byteIx = (ix & 3);
            ch = (ch >> ((3 - byteIx) << 3)) & 0xFF;
            ++ix;

            if (ch > 127) { // multi-byte
                int needed;
                if ((ch & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                    ch &= 0x1F;
                    needed = 1;
                } else if ((ch & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                    ch &= 0x0F;
                    needed = 2;
                } else if ((ch & 0xF8) == 0xF0) { // 4 bytes; double-char with surrogates and all...
                    ch &= 0x07;
                    needed = 3;
                } else { // 5- and 6-byte chars not valid xml chars
                    _reportInvalidInitial(ch);
                    needed = ch = 1; // never really gets this far
                }
                if ((ix + needed) > byteLen) {
                    _reportInvalidEOF(" in field name");
                }
                
                // Ok, always need at least one more:
                int ch2 = quads[ix >> 2]; // current quad, need to shift+mask
                byteIx = (ix & 3);
                ch2 = (ch2 >> ((3 - byteIx) << 3));
                ++ix;
                
                if ((ch2 & 0xC0) != 0x080) {
                    _reportInvalidOther(ch2);
                }
                ch = (ch << 6) | (ch2 & 0x3F);
                if (needed > 1) {
                    ch2 = quads[ix >> 2];
                    byteIx = (ix & 3);
                    ch2 = (ch2 >> ((3 - byteIx) << 3));
                    ++ix;
                    
                    if ((ch2 & 0xC0) != 0x080) {
                        _reportInvalidOther(ch2);
                    }
                    ch = (ch << 6) | (ch2 & 0x3F);
                    if (needed > 2) { // 4 bytes? (need surrogates on output)
                        ch2 = quads[ix >> 2];
                        byteIx = (ix & 3);
                        ch2 = (ch2 >> ((3 - byteIx) << 3));
                        ++ix;
                        if ((ch2 & 0xC0) != 0x080) {
                            _reportInvalidOther(ch2 & 0xFF);
                        }
                        ch = (ch << 6) | (ch2 & 0x3F);
                    }
                }
                if (needed > 2) { // surrogate pair? once again, let's output one here, one later on
                    ch -= 0x10000; // to normalize it starting with 0x0
                    if (cix >= cbuf.length) {
                        cbuf = _textBuffer.expandCurrentSegment();
                    }
                    cbuf[cix++] = (char) (0xD800 + (ch >> 10));
                    ch = 0xDC00 | (ch & 0x03FF);
                }
            }
            if (cix >= cbuf.length) {
                cbuf = _textBuffer.expandCurrentSegment();
            }
            cbuf[cix++] = (char) ch;
        }

        // Ok. Now we have the character array, and can construct the String
        String baseName = new String(cbuf, 0, cix);
        // And finally, un-align if necessary
        if (lastQuadBytes < 4) {
            quads[qlen-1] = lastQuad;
        }
        return _symbols.addName(baseName, quads, qlen);
    }

    /*
    /**********************************************************
    /* Internal methods, String value parsing
    /**********************************************************
     */

    @Override
    protected void _finishString()
        throws IOException, JsonParseException
    {
        // First, single tight loop for ASCII content, not split across input buffer boundary:        
        int ptr = _inputPtr;
        if (ptr >= _inputEnd) {
            loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        final int[] codes = sInputCodesUtf8;

        final int max = Math.min(_inputEnd, (ptr + outBuf.length));
        final byte[] inputBuffer = _inputBuffer;
        while (ptr < max) {
            int c = (int) inputBuffer[ptr] & 0xFF;
            if (codes[c] != 0) {
                if (c == INT_QUOTE) {
                    _inputPtr = ptr+1;
                    _textBuffer.setCurrentLength(outPtr);
                    return;
                }
                break;
            }
            ++ptr;
            outBuf[outPtr++] = (char) c;
        }
        _inputPtr = ptr;
        _finishString2(outBuf, outPtr);
    }

    private final void _finishString2(char[] outBuf, int outPtr)
        throws IOException, JsonParseException
    {
        int c;

        // Here we do want to do full decoding, hence:
        final int[] codes = sInputCodesUtf8;
        final byte[] inputBuffer = _inputBuffer;

        main_loop:
        while (true) {
            // Then the tight ASCII non-funny-char loop:
            ascii_loop:
            while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                final int max = Math.min(_inputEnd, (ptr + (outBuf.length - outPtr)));
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (codes[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break main_loop;
            }

            switch (codes[c]) {
            case 1: // backslash
                c = _decodeEscaped();
                break;
            case 2: // 2-byte UTF
                c = _decodeUtf8_2(c);
                break;
            case 3: // 3-byte UTF
                if ((_inputEnd - _inputPtr) >= 2) {
                    c = _decodeUtf8_3fast(c);
                } else {
                    c = _decodeUtf8_3(c);
                }
                break;
            case 4: // 4-byte UTF
                c = _decodeUtf8_4(c);
                // Let's add first part right away:
                outBuf[outPtr++] = (char) (0xD800 | (c >> 10));
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                c = 0xDC00 | (c & 0x3FF);
                // And let the other char output down below
                break;
            default:
                if (c < INT_SPACE) {
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(c, "string value");
                } else {
                    // Is this good enough error message?
                    _reportInvalidChar(c);
                }
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = (char) c;
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

        // Need to be fully UTF-8 aware here:
        final int[] codes = sInputCodesUtf8;
        final byte[] inputBuffer = _inputBuffer;

        main_loop:
        while (true) {
            int c;

            ascii_loop:
            while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (codes[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break main_loop;
            }
            
            switch (codes[c]) {
            case 1: // backslash
                _decodeEscaped();
                break;
            case 2: // 2-byte UTF
                _skipUtf8_2(c);
                break;
            case 3: // 3-byte UTF
                _skipUtf8_3(c);
                break;
            case 4: // 4-byte UTF
                _skipUtf8_4(c);
                break;
            default:
                if (c < INT_SPACE) {
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(c, "string value");
                } else {
                    // Is this good enough error message?
                    _reportInvalidChar(c);
                }
            }
        }
    }

    /**
     * Method for handling cases where first non-space character
     * of an expected value token is not legal for standard JSON content.
     *
     * @since 1.3
     */
    protected JsonToken _handleUnexpectedValue(int c)
        throws IOException, JsonParseException
    {
        // Most likely an error, unless we are to allow single-quote-strings
        switch (c) {
        case '\'':
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
            return _handleInvalidNumberStart(_inputBuffer[_inputPtr++] & 0xFF, false);
        }

        _reportUnexpectedChar(c, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
        return null;
    }
    
    protected JsonToken _handleApostropheValue()
        throws IOException, JsonParseException
    {
        int c = 0;
        // Otherwise almost verbatim copy of _finishString()
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();

        // Here we do want to do full decoding, hence:
        final int[] codes = sInputCodesUtf8;
        final byte[] inputBuffer = _inputBuffer;

        main_loop:
        while (true) {
            // Then the tight ascii non-funny-char loop:
            ascii_loop:
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outBuf.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) inputBuffer[_inputPtr++] & 0xFF;
                    if (c == INT_APOSTROPHE || codes[c] != 0) {
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (char) c;
                }
            }

            // Ok: end marker, escape or multi-byte?
            if (c == INT_APOSTROPHE) {
                break main_loop;
            }

            switch (codes[c]) {
            case 1: // backslash
                if (c != INT_QUOTE) { // marked as special, isn't here
                    c = _decodeEscaped();
                }
                break;
            case 2: // 2-byte UTF
                c = _decodeUtf8_2(c);
                break;
            case 3: // 3-byte UTF
                if ((_inputEnd - _inputPtr) >= 2) {
                    c = _decodeUtf8_3fast(c);
                } else {
                    c = _decodeUtf8_3(c);
                }
                break;
            case 4: // 4-byte UTF
                c = _decodeUtf8_4(c);
                // Let's add first part right away:
                outBuf[outPtr++] = (char) (0xD800 | (c >> 10));
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                c = 0xDC00 | (c & 0x3FF);
                // And let the other char output down below
                break;
            default:
                if (c < INT_SPACE) {
                    _throwUnquotedSpace(c, "string value");
                }
                // Is this good enough error message?
                _reportInvalidChar(c);
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = (char) c;
        }
        _textBuffer.setCurrentLength(outPtr);

        return JsonToken.VALUE_STRING;
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

    protected final void _matchToken(String matchStr, int i)
        throws IOException, JsonParseException
    {
        final int len = matchStr.length();
    
        do {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    _reportInvalidEOF(" in a value");
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
        int ch = _inputBuffer[_inputPtr] & 0xFF;
        if (ch < '0' || ch == ']' || ch == '}') { // expected/allowed chars
            return;
        }
        // but actually only alphanums are problematic
        char c = (char) _decodeCharForError(ch);
        if (Character.isJavaIdentifierPart(c)) {
            ++_inputPtr;
            _reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
        }
    }
    
    protected void _reportInvalidToken(String matchedPart, String msg)
        throws IOException, JsonParseException
    {
        StringBuilder sb = new StringBuilder(matchedPart);
        /* Let's just try to find what appears to be the token, using
         * regular Java identifier character rules. It's just a heuristic,
         * nothing fancy here (nor fast).
         */
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                break;
            }
            int i = (int) _inputBuffer[_inputPtr++];
            char c = (char) _decodeCharForError(i);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
            sb.append(c);
        }
        _reportError("Unrecognized token '"+sb.toString()+"': was expecting "+msg);
    }
    
    /*
    /**********************************************************
    /* Internal methods, ws skipping, escape/unescape
    /**********************************************************
     */

    private final int _skipWS()
        throws IOException, JsonParseException
    {
        while (_inputPtr < _inputEnd || loadMore()) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
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
            int i = _inputBuffer[_inputPtr++] & 0xFF;
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
        // We ran out of input...
        _handleEOF();
        return -1;
    }

    /**
     * Helper method for matching and skipping a colon character,
     * optionally surrounded by white space
     * 
     * @since 1.9
     */
    private final int _skipColon()
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        // first fast case: we just got a colon without white space:
        int i = _inputBuffer[_inputPtr++];
        if (i == INT_COLON) {
            if (_inputPtr < _inputEnd) {
                i = _inputBuffer[_inputPtr] & 0xFF;
                if (i > INT_SPACE && i != INT_SLASH) {
                    ++_inputPtr;
                    return i;
                }
            }
        } else {
            // need to skip potential leading space
            i &= 0xFF;

            space_loop:
            while (true) {
                switch (i) {
                case INT_SPACE:
                case INT_TAB:
                case INT_CR:
                    _skipCR();
                    break;
                case INT_LF:
                    _skipLF();
                    break;
                case INT_SLASH:
                    _skipComment();
                    break;
                default:
                    if (i < INT_SPACE) {
                        _throwInvalidSpace(i);
                    }
                    break space_loop;
                }
            }
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            i = _inputBuffer[_inputPtr++] & 0xFF;
            if (i != INT_COLON) {
                _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
            }
        }

            // either way, found colon, skip through trailing WS
        while (_inputPtr < _inputEnd || loadMore()) {
            i = _inputBuffer[_inputPtr++] & 0xFF;
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
        int c = _inputBuffer[_inputPtr++] & 0xFF;
        if (c == INT_SLASH) {
            _skipCppComment();
        } else if (c == INT_ASTERISK) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipCComment()
        throws IOException, JsonParseException
    {
        // Need to be UTF-8 aware here to decode content (for skipping)
        final int[] codes = CharTypes.getInputCodeComment();

        // Ok: need the matching '*/'
        while ((_inputPtr < _inputEnd) || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++] & 0xFF;
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                case INT_ASTERISK:
                    if (_inputBuffer[_inputPtr] == INT_SLASH) {
                        ++_inputPtr;
                        return;
                    }
                    break;
                case INT_LF:
                    _skipLF();
                    break;
                case INT_CR:
                    _skipCR();
                    break;
                case 2: // 2-byte UTF
                    _skipUtf8_2(i);
                    break;
                case 3: // 3-byte UTF
                    _skipUtf8_3(i);
                    break;
                case 4: // 4-byte UTF
                    _skipUtf8_4(i);
                    break;
                default: // e.g. -1
                    // Is this good enough error message?
                    _reportInvalidChar(i);
                }
            }
        }
        _reportInvalidEOF(" in a comment");
    }

    private final void _skipCppComment()
        throws IOException, JsonParseException
    {
        // Ok: need to find EOF or linefeed
        final int[] codes = CharTypes.getInputCodeComment();
        while ((_inputPtr < _inputEnd) || loadMore()) {
            int i = (int) _inputBuffer[_inputPtr++] & 0xFF;
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                case INT_LF:
                    _skipLF();
                    return;
                case INT_CR:
                    _skipCR();
                    return;
                case INT_ASTERISK: // nop for these comments
                    break;
                case 2: // 2-byte UTF
                    _skipUtf8_2(i);
                    break;
                case 3: // 3-byte UTF
                    _skipUtf8_3(i);
                    break;
                case 4: // 4-byte UTF
                    _skipUtf8_4(i);
                    break;
                default: // e.g. -1
                    // Is this good enough error message?
                    _reportInvalidChar(i);
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
        int c = (int) _inputBuffer[_inputPtr++];

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
            return (char) c;

        case INT_u: // and finally hex-escaped
            break;

        default:
            return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(c));
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

    protected int _decodeCharForError(int firstByte)
        throws IOException, JsonParseException
    {
        int c = (int) firstByte;
        if (c < 0) { // if >= 0, is ascii and fine as is
            int needed;
            
            // Ok; if we end here, we got multi-byte combination
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                c &= 0x1F;
                needed = 1;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                c &= 0x0F;
                needed = 2;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                c &= 0x07;
                needed = 3;
            } else {
                _reportInvalidInitial(c & 0xFF);
                needed = 1; // never gets here
            }

            int d = nextByte();
            if ((d & 0xC0) != 0x080) {
                _reportInvalidOther(d & 0xFF);
            }
            c = (c << 6) | (d & 0x3F);
            
            if (needed > 1) { // needed == 1 means 2 bytes total
                d = nextByte(); // 3rd byte
                if ((d & 0xC0) != 0x080) {
                    _reportInvalidOther(d & 0xFF);
                }
                c = (c << 6) | (d & 0x3F);
                if (needed > 2) { // 4 bytes? (need surrogates)
                    d = nextByte();
                    if ((d & 0xC0) != 0x080) {
                        _reportInvalidOther(d & 0xFF);
                    }
                    c = (c << 6) | (d & 0x3F);
                }
            }
        }
        return c;
    }

    /*
    /**********************************************************
    /* Internal methods,UTF8 decoding
    /**********************************************************
     */

    private final int _decodeUtf8_2(int c)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        return ((c & 0x1F) << 6) | (d & 0x3F);
    }

    private final int _decodeUtf8_3(int c1)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c1 &= 0x0F;
        int d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        return c;
    }

    private final int _decodeUtf8_3fast(int c1)
        throws IOException, JsonParseException
    {
        c1 &= 0x0F;
        int d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        return c;
    }

    /**
     * @return Character value <b>minus 0x10000</c>; this so that caller
     *    can readily expand it to actual surrogates
     */
    private final int _decodeUtf8_4(int c)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = ((c & 0x07) << 6) | (d & 0x3F);

        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }

        /* note: won't change it to negative here, since caller
         * already knows it'll need a surrogate
         */
        return ((c << 6) | (d & 0x3F)) - 0x10000;
    }

    private final void _skipUtf8_2(int c)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c = (int) _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
    }

    /* Alas, can't heavily optimize skipping, since we still have to
     * do validity checks...
     */
    private final void _skipUtf8_3(int c)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        //c &= 0x0F;
        c = (int) _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c = (int) _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
    }

    private final void _skipUtf8_4(int c)
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = (int) _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, input loading
    /**********************************************************
     */

    /**
     * We actually need to check the character value here
     * (to see if we have \n following \r).
     */
    protected final void _skipCR() throws IOException
    {
        if (_inputPtr < _inputEnd || loadMore()) {
            if (_inputBuffer[_inputPtr] == BYTE_LF) {
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

    private int nextByte()
        throws IOException, JsonParseException
    {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        return _inputBuffer[_inputPtr++] & 0xFF;
    }

    /*
    /**********************************************************
    /* Internal methods, error reporting
    /**********************************************************
     */

    protected void _reportInvalidChar(int c)
        throws JsonParseException
        {
            // Either invalid WS or illegal UTF-8 start char
            if (c < INT_SPACE) {
                _throwInvalidSpace(c);
            }
            _reportInvalidInitial(c);
        }

    protected void _reportInvalidInitial(int mask)
        throws JsonParseException
    {
        _reportError("Invalid UTF-8 start byte 0x"+Integer.toHexString(mask));
    }

    protected void _reportInvalidOther(int mask)
        throws JsonParseException
    {
        _reportError("Invalid UTF-8 middle byte 0x"+Integer.toHexString(mask));
    }

    protected void _reportInvalidOther(int mask, int ptr)
        throws JsonParseException
    {
        _inputPtr = ptr;
        _reportInvalidOther(mask);
    }

    public static int[] growArrayBy(int[] arr, int more)
    {
        if (arr == null) {
            return new int[more];
        }
        int[] old = arr;
        int len = arr.length;
        arr = new int[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
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
            int ch;
            do {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                ch = (int) _inputBuffer[_inputPtr++] & 0xFF;
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) { // reached the end, fair and square?
                if (ch == INT_QUOTE) {
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
            ch = _inputBuffer[_inputPtr++] & 0xFF;
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;
            
            // third base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
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
                    // Ok, must get padding
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    ch = _inputBuffer[_inputPtr++] & 0xFF;
                    if (!b64variant.usesPaddingChar(ch)) {
                        throw reportInvalidBase64Char(b64variant, ch, 3, "expected padding character '"+b64variant.getPaddingChar()+"'");
                    }
                    // Got 12 bits, only need 8, need to shift
                    decodedData >>= 4;
                    builder.append(decodedData);
                    continue;
                }
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
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
                    /* With padding we only get 2 bytes; but we have
                     * to shift it a bit so it is identical to triplet
                     * case with partial output.
                     * 3 chars gives 3x6 == 18 bits, of which 2 are
                     * dummies, need to discard:
                     */
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    continue;
                }
            }
            // otherwise, our triplet is now complete
            decodedData = (decodedData << 6) | bits;
            builder.appendThreeBytes(decodedData);
        }
    }
}
