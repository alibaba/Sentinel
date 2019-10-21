package com.alibaba.acm.shaded.org.codehaus.jackson.util;

/**
 * This is a small utility class, whose main functionality is to allow
 * simple reuse of raw byte/char buffers. It is usually used through
 * <code>ThreadLocal</code> member of the owning class pointing to
 * instance of this class through a <code>SoftReference</code>. The
 * end result is a low-overhead GC-cleanable recycling: hopefully
 * ideal for use by stream readers.
 */
public class BufferRecycler
{
    public final static int DEFAULT_WRITE_CONCAT_BUFFER_LEN = 2000;
    
    public enum ByteBufferType {
        READ_IO_BUFFER(4000)
        /**
         * Buffer used for temporarily storing encoded content; used
         * for example by UTF-8 encoding writer
         */
        ,WRITE_ENCODING_BUFFER(4000)

        /**
         * Buffer used for temporarily concatenating output; used for
         * example when requesting output as byte array.
         */
        ,WRITE_CONCAT_BUFFER(2000)
        ;
            
        private final int size;

        ByteBufferType(int size) { this.size = size; }
    }

    public enum CharBufferType {
        TOKEN_BUFFER(2000) // Tokenizable input
            ,CONCAT_BUFFER(2000) // concatenated output
            ,TEXT_BUFFER(200) // Text content from input
            ,NAME_COPY_BUFFER(200) // Temporary buffer for getting name characters
            ;
        
        private final int size;

        CharBufferType(int size) { this.size = size; }
    }

    final protected byte[][] _byteBuffers = new byte[ByteBufferType.values().length][];
    final protected char[][] _charBuffers = new char[CharBufferType.values().length][];

    public BufferRecycler() { }

    public final byte[] allocByteBuffer(ByteBufferType type)
    {
        int ix = type.ordinal();
        byte[] buffer = _byteBuffers[ix];
        if (buffer == null) {
            buffer = balloc(type.size);
        } else {
            _byteBuffers[ix] = null;
        }
        return buffer;
    }

    public final void releaseByteBuffer(ByteBufferType type, byte[] buffer)
    {
        _byteBuffers[type.ordinal()] = buffer;
    }

    public final char[] allocCharBuffer(CharBufferType type)
    {
        return allocCharBuffer(type, 0);
    }

    public final char[] allocCharBuffer(CharBufferType type, int minSize)
    {
        if (type.size > minSize) {
            minSize = type.size;
        }
        int ix = type.ordinal();
        char[] buffer = _charBuffers[ix];
        if (buffer == null || buffer.length < minSize) {
            buffer = calloc(minSize);
        } else {
            _charBuffers[ix] = null;
        }
        return buffer;
    }

    public final void releaseCharBuffer(CharBufferType type, char[] buffer)
    {
        _charBuffers[type.ordinal()] = buffer;
    }

    /*
    /**********************************************************
    /* Actual allocations separated for easier debugging/profiling
    /**********************************************************
     */

    private final byte[] balloc(int size)
    {
        return new byte[size];
    }

    private final char[] calloc(int size)
    {
        return new char[size];
    }
}
