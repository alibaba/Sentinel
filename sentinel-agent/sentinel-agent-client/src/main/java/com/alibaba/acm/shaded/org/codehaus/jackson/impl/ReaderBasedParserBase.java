package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.IOContext;

/**
 * This is a simple low-level input reader base class, used by
 * JSON parser.
 * The reason for sub-classing (over composition)
 * is due to need for direct access to character buffers
 * and positions.
 *
 * @author Tatu Saloranta
 * 
 * @deprecated Since 1.9 sub-classes should just include code
 *   from here as is.
 */
@Deprecated
public abstract class ReaderBasedParserBase
    extends JsonParserBase
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Reader that can be used for reading more content, if one
     * buffer from input source, but in some cases pre-loaded buffer
     * is handed to the parser.
     */
    protected Reader _reader;

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
     */

    /**
     * Current buffer from which data is read; generally data is read into
     * buffer from input source.
     */
    protected char[] _inputBuffer;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected ReaderBasedParserBase(IOContext ctxt, int features, Reader r)
    {
        super(ctxt, features);
        _reader = r;
        _inputBuffer = ctxt.allocTokenBuffer();
    }

    /*
    /**********************************************************
    /* Overrides
    /**********************************************************
     */

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
    
    /*
    /**********************************************************
    /* Low-level reading, other
    /**********************************************************
     */

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
    /* Helper methods for subclasses
    /**********************************************************
     */

    /**
     * Helper method for checking whether input matches expected token
     * 
     * @since 1.8
     */
    protected final boolean _matchToken(String matchStr, int i)
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
                return true;
            }
        }
        char c = _inputBuffer[_inputPtr];
        // if Java letter, it's a problem tho
        if (Character.isJavaIdentifierPart(c)) {
            ++_inputPtr;
            _reportInvalidToken(matchStr.substring(0, i), "'null', 'true', 'false' or NaN");
        }
        return true;
    }

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
