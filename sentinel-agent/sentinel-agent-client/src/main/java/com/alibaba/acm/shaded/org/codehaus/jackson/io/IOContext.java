package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonEncoding;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.BufferRecycler;
import com.alibaba.acm.shaded.org.codehaus.jackson.util.TextBuffer;

/**
 * To limit number of configuration and state objects to pass, all
 * contextual objects that need to be passed by the factory to
 * readers and writers are combined under this object. One instance
 * is created for each reader and writer.
 */
public final class IOContext
{
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Reference to the source object, which can be used for displaying
     * location information
     */
    protected final Object _sourceRef;

    /**
     * Encoding used by the underlying stream, if known.
     */
    protected JsonEncoding _encoding;

    /**
     * Flag that indicates whether underlying input/output source/target
     * object is fully managed by the owner of this context (parser or
     * generator). If true, it is, and is to be closed by parser/generator;
     * if false, calling application has to do closing (unless auto-closing
     * feature is enabled for the parser/generator in question; in which
     * case it acts like the owner).
     */
    protected final boolean _managedResource;

    /*
    /**********************************************************
    /* Buffer handling, recycling
    /**********************************************************
     */

    /**
     * Recycler used for actual allocation/deallocation/reuse
     */
    protected final BufferRecycler _bufferRecycler;

    /**
     * Reference to the allocated I/O buffer for low-level input reading,
     * if any allocated.
     */
    protected byte[] _readIOBuffer = null;

    /**
     * Reference to the allocated I/O buffer used for low-level
     * encoding-related buffering.
     */
    protected byte[] _writeEncodingBuffer = null;
    
    /**
     * Reference to the buffer allocated for tokenization purposes,
     * in which character input is read, and from which it can be
     * further returned.
     */
    protected char[] _tokenCBuffer = null;

    /**
     * Reference to the buffer allocated for buffering it for
     * output, before being encoded: generally this means concatenating
     * output, then encoding when buffer fills up.
     */
    protected char[] _concatCBuffer = null;

    /**
     * Reference temporary buffer Parser instances need if calling
     * app decides it wants to access name via 'getTextCharacters' method.
     * Regular text buffer can not be used as it may contain textual
     * representation of the value token.
     */
    protected char[] _nameCopyBuffer = null;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public IOContext(BufferRecycler br, Object sourceRef, boolean managedResource)
    {
        _bufferRecycler = br;
        _sourceRef = sourceRef;
        _managedResource = managedResource;
    }

    public void setEncoding(JsonEncoding enc)
    {
        _encoding = enc;
    }

    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    public final Object getSourceReference() { return _sourceRef; }
    public final JsonEncoding getEncoding() { return _encoding; }
    public final boolean isResourceManaged() { return _managedResource; }
    
    /*
    /**********************************************************
    /* Public API, buffer management
    /**********************************************************
     */

    public final TextBuffer constructTextBuffer() {
        return new TextBuffer(_bufferRecycler);
    }

    /**
     *<p>
     * Note: the method can only be called once during its life cycle.
     * This is to protect against accidental sharing.
     */
    public final byte[] allocReadIOBuffer()
    {
        if (_readIOBuffer != null) {
            throw new IllegalStateException("Trying to call allocReadIOBuffer() second time");
        }
        _readIOBuffer = _bufferRecycler.allocByteBuffer(BufferRecycler.ByteBufferType.READ_IO_BUFFER);
        return _readIOBuffer;
    }

    public final byte[] allocWriteEncodingBuffer()
    {
        if (_writeEncodingBuffer != null) {
            throw new IllegalStateException("Trying to call allocWriteEncodingBuffer() second time");
        }
        _writeEncodingBuffer = _bufferRecycler.allocByteBuffer(BufferRecycler.ByteBufferType.WRITE_ENCODING_BUFFER);
        return _writeEncodingBuffer;
    }
    
    public final char[] allocTokenBuffer()
    {
        if (_tokenCBuffer != null) {
            throw new IllegalStateException("Trying to call allocTokenBuffer() second time");
        }
        _tokenCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.TOKEN_BUFFER);
        return _tokenCBuffer;
    }

    public final char[] allocConcatBuffer()
    {
        if (_concatCBuffer != null) {
            throw new IllegalStateException("Trying to call allocConcatBuffer() second time");
        }
        _concatCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.CONCAT_BUFFER);
        return _concatCBuffer;
    }

    public final char[] allocNameCopyBuffer(int minSize)
    {
        if (_nameCopyBuffer != null) {
            throw new IllegalStateException("Trying to call allocNameCopyBuffer() second time");
        }
        _nameCopyBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CharBufferType.NAME_COPY_BUFFER, minSize);
        return _nameCopyBuffer;
    }

    /**
     * Method to call when all the processing buffers can be safely
     * recycled.
     */
    public final void releaseReadIOBuffer(byte[] buf)
    {
        if (buf != null) {
            /* Let's do sanity checks to ensure once-and-only-once release,
             * as well as avoiding trying to release buffers not owned
             */
            if (buf != _readIOBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            _readIOBuffer = null;
            _bufferRecycler.releaseByteBuffer(BufferRecycler.ByteBufferType.READ_IO_BUFFER, buf);
        }
    }

    public final void releaseWriteEncodingBuffer(byte[] buf)
    {
        if (buf != null) {
            /* Let's do sanity checks to ensure once-and-only-once release,
             * as well as avoiding trying to release buffers not owned
             */
            if (buf != _writeEncodingBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            _writeEncodingBuffer = null;
            _bufferRecycler.releaseByteBuffer(BufferRecycler.ByteBufferType.WRITE_ENCODING_BUFFER, buf);
        }
    }
    
    public final void releaseTokenBuffer(char[] buf)
    {
        if (buf != null) {
            if (buf != _tokenCBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            _tokenCBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.TOKEN_BUFFER, buf);
        }
    }

    public final void releaseConcatBuffer(char[] buf)
    {
        if (buf != null) {
            if (buf != _concatCBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            _concatCBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.CONCAT_BUFFER, buf);
        }
    }

    public final void releaseNameCopyBuffer(char[] buf)
    {
        if (buf != null) {
            if (buf != _nameCopyBuffer) {
                throw new IllegalArgumentException("Trying to release buffer not owned by the context");
            }
            _nameCopyBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CharBufferType.NAME_COPY_BUFFER, buf);
        }
    }
}
