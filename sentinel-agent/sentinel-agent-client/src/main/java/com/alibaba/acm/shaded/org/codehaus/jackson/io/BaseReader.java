
package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.io.*;


/**
 * Simple basic class for optimized readers in this package; implements
 * "cookie-cutter" methods that are used by all actual implementations.
 */
abstract class BaseReader
    extends Reader
{
    /**
     * JSON actually limits available Unicode range in the high end
     * to the same as xml (to basically limit UTF-8 max byte sequence
     * length to 4)
     */
    final protected static int LAST_VALID_UNICODE_CHAR = 0x10FFFF;

    final protected static char NULL_CHAR = (char) 0;
    final protected static char NULL_BYTE = (byte) 0;

    final protected IOContext _context;

    protected InputStream _in;

    protected byte[] _buffer;

    protected int _ptr;
    protected int _length;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected BaseReader(IOContext context,
                         InputStream in, byte[] buf, int ptr, int len)
    {
        _context = context;
        _in = in;
        _buffer = buf;
        _ptr = ptr;
        _length = len;
    }

    /*
    /**********************************************************
    /* Reader API
    /**********************************************************
     */

    @Override
    public void close() throws IOException
    {
        InputStream in = _in;

        if (in != null) {
            _in = null;
            freeBuffers();
            in.close();
        }
    }

    protected char[] _tmpBuf = null;

    /**
     * Although this method is implemented by the base class, AND it should
     * never be called by main code, let's still implement it bit more
     * efficiently just in case
     */
    @Override
    public int read() throws IOException
    {
        if (_tmpBuf == null) {
            _tmpBuf = new char[1];
        }
        if (read(_tmpBuf, 0, 1) < 1) {
            return -1;
        }
        return _tmpBuf[0];
    }

    /*
    /**********************************************************
    /* Internal/package methods:
    /**********************************************************
     */

    /**
     * This method should be called along with (or instead of) normal
     * close. After calling this method, no further reads should be tried.
     * Method will try to recycle read buffers (if any).
     */
    public final void freeBuffers()
    {
        byte[] buf = _buffer;
        if (buf != null) {
            _buffer = null;
            _context.releaseReadIOBuffer(buf);
        }
    }

    protected void reportBounds(char[] cbuf, int start, int len)
        throws IOException
    {
        throw new ArrayIndexOutOfBoundsException("read(buf,"+start+","+len+"), cbuf["+cbuf.length+"]");
    }

    protected void reportStrangeStream()
        throws IOException
    {
        throw new IOException("Strange I/O stream, returned 0 bytes on read");
    }
}
