package com.alibaba.acm.shaded.org.codehaus.jackson.format;

import java.io.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory;

/**
 * Interface used to expose beginning of a data file to data format
 * detection code.
 * 
 * @since 1.8
 */
public interface InputAccessor
{
    /**
     * Method to call to check if more input is available.
     * Since this may result in more content to be read (at least
     * one more byte), a {@link IOException} may get thrown.
     */
    public boolean hasMoreBytes() throws IOException;

    /**
     * Returns next byte available, if any; if no more bytes are
     * available, will throw {@link java.io.EOFException}.
     */
    public byte nextByte() throws IOException;

    /**
     * Method that can be called to reset accessor to read from beginning
     * of input.
     */
    public void reset();

    /*
    /**********************************************************
    /* Standard implementation
    /**********************************************************
     */

    /**
     * Basic implementation that reads data from given
     * {@link InputStream} and buffers it as necessary.
     */
    public class Std implements InputAccessor
    {
        protected final InputStream _in;

        protected final byte[] _buffer;

        /**
         * Number of bytes in {@link #_buffer} that are valid
         * buffered content.
         */
        protected int _bufferedAmount;
        
        /**
         * Pointer to next available buffered byte in {@link #_buffer}.
         */
        protected int _ptr;
        
        /**
         * Constructor used when content to check is available via
         * input stream and must be read.
         */
        public Std(InputStream in, byte[] buffer)
        {
            _in = in;
            _buffer = buffer;
            _bufferedAmount = 0;
        }

        /**
         * Constructor used when the full input (or at least enough leading bytes
         * of full input) is available.
         */
        public Std(byte[] inputDocument)
        {
            _in = null;
            _buffer = inputDocument;
            // we have it all:
            _bufferedAmount = inputDocument.length;
        }
        
        @Override
        public boolean hasMoreBytes() throws IOException
        {
            if (_ptr < _bufferedAmount) { // already got more
                return true;
            }
            int amount = _buffer.length - _ptr;
            if (amount < 1) { // can not load any more
                return false;
            }
            int count = _in.read(_buffer, _ptr, amount);
            if (count <= 0) { // EOF
                return false;
            }
            _bufferedAmount += count;
            return true;
        }

        @Override
        public byte nextByte() throws IOException
        {
            // should we just try loading more automatically?
            if (_ptr >- _bufferedAmount) {
                if (!hasMoreBytes()) {
                    throw new EOFException("Could not read more than "+_ptr+" bytes (max buffer size: "+_buffer.length+")");
                }
            }
            return _buffer[_ptr++];
        }

        @Override
        public void reset() {
            _ptr = 0;
        }

        /*
        /**********************************************************
        /* Extended API for DataFormatDetector/Matcher
        /**********************************************************
         */

        public DataFormatMatcher createMatcher(JsonFactory match, MatchStrength matchStrength)
        {
            return new DataFormatMatcher(_in, _buffer, _bufferedAmount, match, matchStrength);
        }
    }
}
