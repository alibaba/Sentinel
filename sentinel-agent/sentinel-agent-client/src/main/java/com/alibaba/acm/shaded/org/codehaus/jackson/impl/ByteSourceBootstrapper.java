package com.alibaba.acm.shaded.org.codehaus.jackson.impl;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.format.InputAccessor;
import com.alibaba.acm.shaded.org.codehaus.jackson.format.MatchStrength;
import com.alibaba.acm.shaded.org.codehaus.jackson.io.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.BytesToNameCanonicalizer;
import com.alibaba.acm.shaded.org.codehaus.jackson.sym.CharsToNameCanonicalizer;

/**
 * This class is used to determine the encoding of byte stream
 * that is to contain JSON content. Rules are fairly simple, and
 * defined in JSON specification (RFC-4627 or newer), except
 * for BOM handling, which is a property of underlying
 * streams.
 */
public final class ByteSourceBootstrapper
{
    final static byte UTF8_BOM_1 = (byte) 0xEF;
    final static byte UTF8_BOM_2 = (byte) 0xBB;
    final static byte UTF8_BOM_3 = (byte) 0xBF;
    
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected final IOContext _context;

    protected final InputStream _in;

    /*
    /**********************************************************
    /* Input buffering
    /**********************************************************
     */

    protected final byte[] _inputBuffer;

    private int _inputPtr;

    private int _inputEnd;

    /**
     * Flag that indicates whether buffer above is to be recycled
     * after being used or not.
     */
    private final boolean _bufferRecyclable;

    /*
    /**********************************************************
    /* Input location
    /**********************************************************
     */

    /**
     * Current number of input units (bytes or chars) that were processed in
     * previous blocks,
     * before contents of current input buffer.
     *<p>
     * Note: includes possible BOMs, if those were part of the input.
     */
    protected int _inputProcessed;

    /*
    /**********************************************************
    /* Data gathered
    /**********************************************************
     */

    protected boolean _bigEndian = true;

    protected int _bytesPerChar = 0; // 0 means "dunno yet"

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public ByteSourceBootstrapper(IOContext ctxt, InputStream in)
    {
        _context = ctxt;
        _in = in;
        _inputBuffer = ctxt.allocReadIOBuffer();
        _inputEnd = _inputPtr = 0;
        _inputProcessed = 0;
        _bufferRecyclable = true;
    }

    public ByteSourceBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart, int inputLen)
    {
        _context = ctxt;
        _in = null;
        _inputBuffer = inputBuffer;
        _inputPtr = inputStart;
        _inputEnd = (inputStart + inputLen);
        // Need to offset this for correct location info
        _inputProcessed = -inputStart;
        _bufferRecyclable = false;
    }

    /*
    /**********************************************************
    /*  Encoding detection during bootstrapping
    /**********************************************************
     */
    
    /**
     * Method that should be called after constructing an instace.
     * It will figure out encoding that content uses, to allow
     * for instantiating a proper scanner object.
     */
    public JsonEncoding detectEncoding()
        throws IOException, JsonParseException
    {
        boolean foundEncoding = false;

        // First things first: BOM handling
        /* Note: we can require 4 bytes to be read, since no
         * combination of BOM + valid JSON content can have
         * shorter length (shortest valid JSON content is single
         * digit char, but BOMs are chosen such that combination
         * is always at least 4 chars long)
         */
        if (ensureLoaded(4)) {
            int quad =  (_inputBuffer[_inputPtr] << 24)
                | ((_inputBuffer[_inputPtr+1] & 0xFF) << 16)
                | ((_inputBuffer[_inputPtr+2] & 0xFF) << 8)
                | (_inputBuffer[_inputPtr+3] & 0xFF);
            
            if (handleBOM(quad)) {
                foundEncoding = true;
            } else {
                /* If no BOM, need to auto-detect based on first char;
                 * this works since it must be 7-bit ascii (wrt. unicode
                 * compatible encodings, only ones JSON can be transferred
                 * over)
                 */
                // UTF-32?
                if (checkUTF32(quad)) {
                    foundEncoding = true;
                } else if (checkUTF16(quad >>> 16)) {
                    foundEncoding = true;
                }
            }
        } else if (ensureLoaded(2)) {
            int i16 = ((_inputBuffer[_inputPtr] & 0xFF) << 8)
                | (_inputBuffer[_inputPtr+1] & 0xFF);
            if (checkUTF16(i16)) {
                foundEncoding = true;
            }
        }

        JsonEncoding enc;

        /* Not found yet? As per specs, this means it must be UTF-8. */
        if (!foundEncoding) {
            enc = JsonEncoding.UTF8;
        } else {
            switch (_bytesPerChar) {
            case 1:
                enc = JsonEncoding.UTF8;
                break;
            case 2:
                enc = _bigEndian ? JsonEncoding.UTF16_BE : JsonEncoding.UTF16_LE;
                break;
            case 4:
                enc = _bigEndian ? JsonEncoding.UTF32_BE : JsonEncoding.UTF32_LE;
                break;
            default:
                throw new RuntimeException("Internal error"); // should never get here
            }
        }
        _context.setEncoding(enc);
        return enc;
    }

    /*
    /**********************************************************
    /* Constructing a Reader
    /**********************************************************
     */
    
    public Reader constructReader()
        throws IOException
    {
        JsonEncoding enc = _context.getEncoding();
        switch (enc) { 
        case UTF32_BE:
        case UTF32_LE:
            return new UTF32Reader(_context, _in, _inputBuffer, _inputPtr, _inputEnd,
                                   _context.getEncoding().isBigEndian());

        case UTF16_BE:
        case UTF16_LE:
        case UTF8: // only in non-common case where we don't want to do direct mapping
            {
                // First: do we have a Stream? If not, need to create one:
                InputStream in = _in;

                if (in == null) {
                    in = new ByteArrayInputStream(_inputBuffer, _inputPtr, _inputEnd);
                } else {
                    /* Also, if we have any read but unused input (usually true),
                     * need to merge that input in:
                     */
                    if (_inputPtr < _inputEnd) {
                        in = new MergedStream(_context, in, _inputBuffer, _inputPtr, _inputEnd);
                    }
                }
                return new InputStreamReader(in, enc.getJavaName());
            }
        }
        throw new RuntimeException("Internal error"); // should never get here
    }

    public JsonParser constructParser(int features, ObjectCodec codec, BytesToNameCanonicalizer rootByteSymbols, CharsToNameCanonicalizer rootCharSymbols)
        throws IOException, JsonParseException
    {
        JsonEncoding enc = detectEncoding();

        // As per [JACKSON-259], may want to fully disable canonicalization:
        boolean canonicalize = JsonParser.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(features);
        boolean intern = JsonParser.Feature.INTERN_FIELD_NAMES.enabledIn(features);
        if (enc == JsonEncoding.UTF8) {
            /* and without canonicalization, byte-based approach is not performance; just use std UTF-8 reader
             * (which is ok for larger input; not so hot for smaller; but this is not a common case)
             */
            if (canonicalize) {
                BytesToNameCanonicalizer can = rootByteSymbols.makeChild(canonicalize, intern);
                return new Utf8StreamParser(_context, features, _in, codec, can, _inputBuffer, _inputPtr, _inputEnd, _bufferRecyclable);
            }
        }
        return new ReaderBasedParser(_context, features, constructReader(), codec, rootCharSymbols.makeChild(canonicalize, intern));
    }

    /*
    /**********************************************************
    /*  Encoding detection for data format auto-detection
    /**********************************************************
     */

    /**
     * Current implementation is not as thorough as other functionality
     * ({@link com.alibaba.acm.shaded.org.codehaus.jackson.impl.ByteSourceBootstrapper}); 
     * supports UTF-8, for example. But it should work, for now, and can
     * be improved as necessary.
     * 
     * @since 1.8
     */
    public static MatchStrength hasJSONFormat(InputAccessor acc) throws IOException
    {
        // Ideally we should see "[" or "{"; but if not, we'll accept double-quote (String)
        // in future could also consider accepting non-standard matches?
        
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte b = acc.nextByte();
        // Very first thing, a UTF-8 BOM?
        if (b == UTF8_BOM_1) { // yes, looks like UTF-8 BOM
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_2) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_3) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
        }
        // Then possible leading space
        int ch = skipSpace(acc, b);
        if (ch < 0) {
            return MatchStrength.INCONCLUSIVE;
        }
        // First, let's see if it looks like a structured type:
        if (ch == '{') { // JSON object?
            // Ideally we need to find either double-quote or closing bracket
            ch = skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (ch == '"' || ch == '}') {
                return MatchStrength.SOLID_MATCH;
            }
            // ... should we allow non-standard? Let's not yet... can add if need be
            return MatchStrength.NO_MATCH;
        }
        MatchStrength strength;
        
        if (ch == '[') {
            ch = skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            // closing brackets is easy; but for now, let's also accept opening...
            if (ch == ']' || ch == '[') {
                return MatchStrength.SOLID_MATCH;
            }
            return MatchStrength.SOLID_MATCH;
        } else {
            // plain old value is not very convincing...
            strength = MatchStrength.WEAK_MATCH;
        }

        if (ch == '"') { // string value
            return strength;
        }
        if (ch <= '9' && ch >= '0') { // number
            return strength;
        }
        if (ch == '-') { // negative number
            ch = skipSpace(acc);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            return (ch <= '9' && ch >= '0') ? strength : MatchStrength.NO_MATCH;
        }
        // or one of literals
        if (ch == 'n') { // null
            return tryMatch(acc, "ull", strength);
        }
        if (ch == 't') { // true
            return tryMatch(acc, "rue", strength);
        }
        if (ch == 'f') { // false
            return tryMatch(acc, "alse", strength);
        }
        return MatchStrength.NO_MATCH;
    }

    private final static MatchStrength tryMatch(InputAccessor acc, String matchStr, MatchStrength fullMatchStrength)
        throws IOException
    {
        for (int i = 0, len = matchStr.length(); i < len; ++i) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != matchStr.charAt(i)) {
                return MatchStrength.NO_MATCH;
            }
        }
        return fullMatchStrength;
    }
    
    private final static int skipSpace(InputAccessor acc) throws IOException
    {
        if (!acc.hasMoreBytes()) {
            return -1;
        }
        return skipSpace(acc, acc.nextByte());
    }
    
    private final static int skipSpace(InputAccessor acc, byte b) throws IOException
    {
        while (true) {
            int ch = (int) b & 0xFF;
            if (!(ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t')) {
                return ch;
            }
            if (!acc.hasMoreBytes()) {
                return -1;
            }
            b = acc.nextByte();
            ch = (int) b & 0xFF;
        }
    }
    
    /*
    /**********************************************************
    /* Internal methods, parsing
    /**********************************************************
     */

    /**
     * @return True if a BOM was succesfully found, and encoding
     *   thereby recognized.
     */
    private boolean handleBOM(int quad)
        throws IOException
    {
        /* Handling of (usually) optional BOM (required for
         * multi-byte formats); first 32-bit charsets:
         */
        switch (quad) {
        case 0x0000FEFF:
            _bigEndian = true;
            _inputPtr += 4;
            _bytesPerChar = 4;
            return true;
        case 0xFFFE0000: // UCS-4, LE?
            _inputPtr += 4;
            _bytesPerChar = 4;
            _bigEndian = false;
            return true;
        case 0x0000FFFE: // UCS-4, in-order...
            reportWeirdUCS4("2143"); // throws exception
        case 0xFEFF0000: // UCS-4, in-order...
            reportWeirdUCS4("3412"); // throws exception
        }
        // Ok, if not, how about 16-bit encoding BOMs?
        int msw = quad >>> 16;
        if (msw == 0xFEFF) { // UTF-16, BE
            _inputPtr += 2;
            _bytesPerChar = 2;
            _bigEndian = true;
            return true;
        }
        if (msw == 0xFFFE) { // UTF-16, LE
            _inputPtr += 2;
            _bytesPerChar = 2;
            _bigEndian = false;
            return true;
        }
        // And if not, then UTF-8 BOM?
        if ((quad >>> 8) == 0xEFBBBF) { // UTF-8
            _inputPtr += 3;
            _bytesPerChar = 1;
            _bigEndian = true; // doesn't really matter
            return true;
        }
        return false;
    }

    private boolean checkUTF32(int quad)
        throws IOException
    {
        /* Handling of (usually) optional BOM (required for
         * multi-byte formats); first 32-bit charsets:
         */
        if ((quad >> 8) == 0) { // 0x000000?? -> UTF32-BE
            _bigEndian = true;
        } else if ((quad & 0x00FFFFFF) == 0) { // 0x??000000 -> UTF32-LE
            _bigEndian = false;
        } else if ((quad & ~0x00FF0000) == 0) { // 0x00??0000 -> UTF32-in-order
            reportWeirdUCS4("3412");
        } else if ((quad & ~0x0000FF00) == 0) { // 0x0000??00 -> UTF32-in-order
            reportWeirdUCS4("2143");
        } else {
            // Can not be valid UTF-32 encoded JSON...
            return false;
        }
        // Not BOM (just regular content), nothing to skip past:
        //_inputPtr += 4;
        _bytesPerChar = 4;
        return true;
    }

    private boolean checkUTF16(int i16)
    {
        if ((i16 & 0xFF00) == 0) { // UTF-16BE
            _bigEndian = true;
        } else if ((i16 & 0x00FF) == 0) { // UTF-16LE
            _bigEndian = false;
        } else { // nope, not  UTF-16
            return false;
        }
        // Not BOM (just regular content), nothing to skip past:
        //_inputPtr += 2;
        _bytesPerChar = 2;
        return true;
    }

    /*
    /**********************************************************
    /* Internal methods, problem reporting
    /**********************************************************
     */

    private void reportWeirdUCS4(String type)
        throws IOException
    {
        throw new CharConversionException("Unsupported UCS-4 endianness ("+type+") detected");
    }

    /*
    /**********************************************************
    /* Internal methods, raw input access
    /**********************************************************
     */

    protected boolean ensureLoaded(int minimum)
        throws IOException
    {
        /* Let's assume here buffer has enough room -- this will always
         * be true for the limited used this method gets
         */
        int gotten = (_inputEnd - _inputPtr);
        while (gotten < minimum) {
            int count;

            if (_in == null) { // block source
                count = -1;
            } else {
                count = _in.read(_inputBuffer, _inputEnd, _inputBuffer.length - _inputEnd);
            }
            if (count < 1) {
                return false;
            }
            _inputEnd += count;
            gotten += count;
        }
        return true;
    }
}

