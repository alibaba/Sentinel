package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.io.*;

/**
 * Simple {@link InputStream} implementation that is used to "unwind" some
 * data previously read from an input stream; so that as long as some of
 * that data remains, it's returned; but as long as it's read, we'll
 * just use data from the underlying original stream. 
 * This is similar to {@link java.io.PushbackInputStream}, but here there's
 * only one implicit pushback, when instance is constructed.
 */
public final class MergedStream
    extends InputStream
{
    final protected IOContext _context;

    final InputStream _in;

    byte[] _buffer;

    int _ptr;

    final int _end;

    public MergedStream(IOContext context,
            InputStream in, byte[] buf, int start, int end)
    {
        _context = context;
        _in = in;
        _buffer = buf;
        _ptr = start;
        _end = end;
    }

    @Override
    public int available() throws IOException
    {
        if (_buffer != null) {
            return _end - _ptr;
        }
        return _in.available();
    }

    @Override
    public void close() throws IOException
    {
        freeMergedBuffer();
        _in.close();
    }

    @Override
    public void mark(int readlimit)
    {
        if (_buffer == null) {
            _in.mark(readlimit);
        }
    }
    
    @Override
    public boolean markSupported()
    {
        // Only supports marks past the initial rewindable section...
        return (_buffer == null) && _in.markSupported();
    }
    
    @Override
    public int read() throws IOException
    {
        if (_buffer != null) {
            int c = _buffer[_ptr++] & 0xFF;
            if (_ptr >= _end) {
                freeMergedBuffer();
            }
            return c;
        }
        return _in.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    @Override
    public int 	read(byte[] b, int off, int len) throws IOException
    {
        if (_buffer != null) {
            int avail = _end - _ptr;
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(_buffer, _ptr, b, off, len);
            _ptr += len;
            if (_ptr >= _end) {
                freeMergedBuffer();
            }
            return len;
        }
        return _in.read(b, off, len);
    }

    @Override
    public void reset() throws IOException
    {
        if (_buffer == null) {
            _in.reset();
        }
    }

    @Override
    public long skip(long n) throws IOException
    {
        long count = 0L;

        if (_buffer != null) {
            int amount = _end - _ptr;

            if (amount > n) { // all in pushed back segment?
                _ptr += (int) n;
                return n;
            }
            freeMergedBuffer();
            count += amount;
            n -= amount;
        }

        if (n > 0) {
            count += _in.skip(n);
        }
        return count;
    }

    private void freeMergedBuffer()
    {
        byte[] buf = _buffer;
        if (buf != null) {
            _buffer = null;
            if (_context != null) {
                _context.releaseReadIOBuffer(buf);
            }
        }
    }
}
