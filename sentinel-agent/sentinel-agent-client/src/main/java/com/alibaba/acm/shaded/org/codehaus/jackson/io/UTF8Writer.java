package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.io.*;


public final class UTF8Writer
    extends Writer
{
    final static int SURR1_FIRST = 0xD800;
    final static int SURR1_LAST = 0xDBFF;
    final static int SURR2_FIRST = 0xDC00;
    final static int SURR2_LAST = 0xDFFF;

    final protected IOContext _context;

    OutputStream _out;

    byte[] _outBuffer;

    final int _outBufferEnd;

    int _outPtr;

    /**
     * When outputting chars from BMP, surrogate pairs need to be coalesced.
     * To do this, both pairs must be known first; and since it is possible
     * pairs may be split, we need temporary storage for the first half
     */
    int _surrogate = 0;

    public UTF8Writer(IOContext ctxt, OutputStream out)
    {
        _context = ctxt;
        _out = out;

        _outBuffer = ctxt.allocWriteEncodingBuffer();
        /* Max. expansion for a single char (in unmodified UTF-8) is
         * 4 bytes (or 3 depending on how you view it -- 4 when recombining
         * surrogate pairs)
         */
        _outBufferEnd = _outBuffer.length - 4;
        _outPtr = 0;
    }

    @Override
    public Writer append(char c)
        throws IOException
    {
        write(c);
        return this;
    }

    @Override
    public void close()
        throws IOException
    {
        if (_out != null) {
            if (_outPtr > 0) {
                _out.write(_outBuffer, 0, _outPtr);
                _outPtr = 0;
            }
            OutputStream out = _out;
            _out = null;

            byte[] buf = _outBuffer;
            if (buf != null) {
                _outBuffer = null;
                _context.releaseWriteEncodingBuffer(buf);
            }

            out.close();

            /* Let's 'flush' orphan surrogate, no matter what; but only
             * after cleanly closing everything else.
             */
            int code = _surrogate;
            _surrogate = 0;
            if (code > 0) {
                throwIllegal(code);
            }
        }
    }

    @Override
    public void flush()
        throws IOException
    {
        if (_out != null) {
            if (_outPtr > 0) {
                _out.write(_outBuffer, 0, _outPtr);
                _outPtr = 0;
            }
            _out.flush();
        }
    }

    @Override
    public void write(char[] cbuf)
        throws IOException
    {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(char[] cbuf, int off, int len)
        throws IOException
    {
        if (len < 2) {
            if (len == 1) {
                write(cbuf[off]);
            }
            return;
        }

        // First: do we have a leftover surrogate to deal with?
        if (_surrogate > 0) {
            char second = cbuf[off++];
            --len;
            write(convertSurrogate(second));
            // will have at least one more char
        }

        int outPtr = _outPtr;
        byte[] outBuf = _outBuffer;
        int outBufLast = _outBufferEnd; // has 4 'spare' bytes

        // All right; can just loop it nice and easy now:
        len += off; // len will now be the end of input buffer

        output_loop:
        for (; off < len; ) {
            /* First, let's ensure we can output at least 4 bytes
             * (longest UTF-8 encoded codepoint):
             */
            if (outPtr >= outBufLast) {
                _out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }

            int c = cbuf[off++];
            // And then see if we have an Ascii char:
            if (c < 0x80) { // If so, can do a tight inner loop:
                outBuf[outPtr++] = (byte)c;
                // Let's calc how many ascii chars we can copy at most:
                int maxInCount = (len - off);
                int maxOutCount = (outBufLast - outPtr);

                if (maxInCount > maxOutCount) {
                    maxInCount = maxOutCount;
                }
                maxInCount += off;
                ascii_loop:
                while (true) {
                    if (off >= maxInCount) { // done with max. ascii seq
                        continue output_loop;
                    }
                    c = cbuf[off++];
                    if (c >= 0x80) {
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (byte) c;
                }
            }

            // Nope, multi-byte:
            if (c < 0x800) { // 2-byte
                outBuf[outPtr++] = (byte) (0xc0 | (c >> 6));
                outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
            } else { // 3 or 4 bytes
                // Surrogates?
                if (c < SURR1_FIRST || c > SURR2_LAST) {
                    outBuf[outPtr++] = (byte) (0xe0 | (c >> 12));
                    outBuf[outPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
                    continue;
                }
                // Yup, a surrogate:
                if (c > SURR1_LAST) { // must be from first range
                    _outPtr = outPtr;
                    throwIllegal(c);
                }
                _surrogate = c;
                // and if so, followed by another from next range
                if (off >= len) { // unless we hit the end?
                    break;
                }
                c = convertSurrogate(cbuf[off++]);
                if (c > 0x10FFFF) { // illegal in JSON as well as in XML
                    _outPtr = outPtr;
                    throwIllegal(c);
                }
                outBuf[outPtr++] = (byte) (0xf0 | (c >> 18));
                outBuf[outPtr++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                outBuf[outPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
            }
        }
        _outPtr = outPtr;
    }
    
    @Override
    public void write(int c) throws IOException
    {
        // First; do we have a left over surrogate?
        if (_surrogate > 0) {
            c = convertSurrogate(c);
            // If not, do we start with a surrogate?
        } else if (c >= SURR1_FIRST && c <= SURR2_LAST) {
            // Illegal to get second part without first:
            if (c > SURR1_LAST) {
                throwIllegal(c);
            }
            // First part just needs to be held for now
            _surrogate = c;
            return;
        }

        if (_outPtr >= _outBufferEnd) { // let's require enough room, first
            _out.write(_outBuffer, 0, _outPtr);
            _outPtr = 0;
        }

        if (c < 0x80) { // ascii
            _outBuffer[_outPtr++] = (byte) c;
        } else {
            int ptr = _outPtr;
            if (c < 0x800) { // 2-byte
                _outBuffer[ptr++] = (byte) (0xc0 | (c >> 6));
                _outBuffer[ptr++] = (byte) (0x80 | (c & 0x3f));
            } else if (c <= 0xFFFF) { // 3 bytes
                _outBuffer[ptr++] = (byte) (0xe0 | (c >> 12));
                _outBuffer[ptr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                _outBuffer[ptr++] = (byte) (0x80 | (c & 0x3f));
            } else { // 4 bytes
                if (c > 0x10FFFF) { // illegal
                    throwIllegal(c);
                }
                _outBuffer[ptr++] = (byte) (0xf0 | (c >> 18));
                _outBuffer[ptr++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                _outBuffer[ptr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                _outBuffer[ptr++] = (byte) (0x80 | (c & 0x3f));
            }
            _outPtr = ptr;
        }
    }

    @Override
    public void write(String str) throws IOException
    {
        write(str, 0, str.length());
    }

    @Override
    public void write(String str, int off, int len)  throws IOException
    {
        if (len < 2) {
            if (len == 1) {
                write(str.charAt(off));
            }
            return;
        }

        // First: do we have a leftover surrogate to deal with?
        if (_surrogate > 0) {
            char second = str.charAt(off++);
            --len;
            write(convertSurrogate(second));
            // will have at least one more char (case of 1 char was checked earlier on)
        }

        int outPtr = _outPtr;
        byte[] outBuf = _outBuffer;
        int outBufLast = _outBufferEnd; // has 4 'spare' bytes

        // All right; can just loop it nice and easy now:
        len += off; // len will now be the end of input buffer

        output_loop:
        for (; off < len; ) {
            /* First, let's ensure we can output at least 4 bytes
             * (longest UTF-8 encoded codepoint):
             */
            if (outPtr >= outBufLast) {
                _out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }

            int c = str.charAt(off++);
            // And then see if we have an Ascii char:
            if (c < 0x80) { // If so, can do a tight inner loop:
                outBuf[outPtr++] = (byte)c;
                // Let's calc how many ascii chars we can copy at most:
                int maxInCount = (len - off);
                int maxOutCount = (outBufLast - outPtr);

                if (maxInCount > maxOutCount) {
                    maxInCount = maxOutCount;
                }
                maxInCount += off;
                ascii_loop:
                while (true) {
                    if (off >= maxInCount) { // done with max. ascii seq
                        continue output_loop;
                    }
                    c = str.charAt(off++);
                    if (c >= 0x80) {
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (byte) c;
                }
            }

            // Nope, multi-byte:
            if (c < 0x800) { // 2-byte
                outBuf[outPtr++] = (byte) (0xc0 | (c >> 6));
                outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
            } else { // 3 or 4 bytes
                // Surrogates?
                if (c < SURR1_FIRST || c > SURR2_LAST) {
                    outBuf[outPtr++] = (byte) (0xe0 | (c >> 12));
                    outBuf[outPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
                    continue;
                }
                // Yup, a surrogate:
                if (c > SURR1_LAST) { // must be from first range
                    _outPtr = outPtr;
                    throwIllegal(c);
                }
                _surrogate = c;
                // and if so, followed by another from next range
                if (off >= len) { // unless we hit the end?
                    break;
                }
                c = convertSurrogate(str.charAt(off++));
                if (c > 0x10FFFF) { // illegal, as per RFC 4627
                    _outPtr = outPtr;
                    throwIllegal(c);
                }
                outBuf[outPtr++] = (byte) (0xf0 | (c >> 18));
                outBuf[outPtr++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                outBuf[outPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                outBuf[outPtr++] = (byte) (0x80 | (c & 0x3f));
            }
        }
        _outPtr = outPtr;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Method called to calculate UTF codepoint, from a surrogate pair.
     */
    private int convertSurrogate(int secondPart)
        throws IOException
    {
        int firstPart = _surrogate;
        _surrogate = 0;

        // Ok, then, is the second part valid?
        if (secondPart < SURR2_FIRST || secondPart > SURR2_LAST) {
            throw new IOException("Broken surrogate pair: first char 0x"+Integer.toHexString(firstPart)+", second 0x"+Integer.toHexString(secondPart)+"; illegal combination");
        }
        return 0x10000 + ((firstPart - SURR1_FIRST) << 10) + (secondPart - SURR2_FIRST);
    }

    private void throwIllegal(int code)
        throws IOException
    {
        if (code > 0x10FFFF) { // over max?
            throw new IOException("Illegal character point (0x"+Integer.toHexString(code)+") to output; max is 0x10FFFF as per RFC 4627");
        }
        if (code >= SURR1_FIRST) {
            if (code <= SURR1_LAST) { // Unmatched first part (closing without second part?)
                throw new IOException("Unmatched first part of surrogate pair (0x"+Integer.toHexString(code)+")");
            }
            throw new IOException("Unmatched second part of surrogate pair (0x"+Integer.toHexString(code)+")");
        }

        // should we ever get this?
        throw new IOException("Illegal character point (0x"+Integer.toHexString(code)+") to output");
    }
}
