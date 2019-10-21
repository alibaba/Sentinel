package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.util.BufferRecycler;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TextBuffer;

/**
 * Efficient alternative to {@link StringWriter}, based on using segmented
 * internal buffer. Initial input buffer is also recyclable.
 *<p>
 * This class is most useful when serializing JSON content as a String:
 * if so, instance of this class can be given as the writer to
 * <code>JsonGenerator</code>.
 *
 * @since 1.3
 */
public final class SegmentedStringWriter
    extends Writer
{
    final protected TextBuffer _buffer;

    public SegmentedStringWriter(BufferRecycler br)
    {
        super();
        _buffer = new TextBuffer(br);
    }

    /*
    /**********************************************************
    /* java.io.Writer implementation
    /**********************************************************
     */

    @Override
    public Writer append(char c)
    {
        write(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq)
    {
	String str = csq.toString();
	_buffer.append(str, 0, str.length());
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end)
    {
	String str = csq.subSequence(start, end).toString();
	_buffer.append(str, 0, str.length());
        return this;
    }

    @Override public void close() { } // NOP

    @Override public void flush() { } // NOP

    @Override
    public void write(char[] cbuf) {
        _buffer.append(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        _buffer.append(cbuf, off, len);
    }

    @Override
    public void write(int c) {
        _buffer.append((char) c);
    }

    @Override
    public void write(String str) { _buffer.append(str, 0, str.length()); }

    @Override
    public void write(String str, int off, int len) {
        _buffer.append(str, 0, str.length());
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Main access method that will construct a String that contains
     * all the contents, release all internal buffers we may have,
     * and return result String.
     * Note that the method is not idempotent -- if called second time,
     * will just return an empty String.
     */
    public String getAndClear()
    {
        String result = _buffer.contentsAsString();
        _buffer.releaseBuffers();
        return result;
    }
}
