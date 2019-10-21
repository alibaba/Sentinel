/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.apache.http.io.BufferInfo;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

/**
 * Abstract base class for session output buffers that stream data to
 * an arbitrary {@link OutputStream}. This class buffers small chunks of
 * output data in an internal byte array for optimal output performance.
 * <p>
 * {@link #writeLine(CharArrayBuffer)} and {@link #writeLine(String)} methods
 * of this class use CR-LF as a line delimiter.
 *
 * @since 4.3
 */
public class SessionOutputBufferImpl implements SessionOutputBuffer, BufferInfo {

    private static final byte[] CRLF = new byte[] {HTTP.CR, HTTP.LF};

    private final HttpTransportMetricsImpl metrics;
    private final ByteArrayBuffer buffer;
    private final int fragementSizeHint;
    private final CharsetEncoder encoder;

    private OutputStream outstream;
    private ByteBuffer bbuf;

    /**
     * Creates new instance of SessionOutputBufferImpl.
     *
     * @param metrics HTTP transport metrics.
     * @param buffersize buffer size. Must be a positive number.
     * @param fragementSizeHint fragment size hint defining a minimal size of a fragment
     *   that should be written out directly to the socket bypassing the session buffer.
     *   Value {@code 0} disables fragment buffering.
     * @param charencoder charencoder to be used for encoding HTTP protocol elements.
     *   If {@code null} simple type cast will be used for char to byte conversion.
     */
    public SessionOutputBufferImpl(
            final HttpTransportMetricsImpl metrics,
            final int buffersize,
            final int fragementSizeHint,
            final CharsetEncoder charencoder) {
        super();
        Args.positive(buffersize, "Buffer size");
        Args.notNull(metrics, "HTTP transport metrcis");
        this.metrics = metrics;
        this.buffer = new ByteArrayBuffer(buffersize);
        this.fragementSizeHint = fragementSizeHint >= 0 ? fragementSizeHint : 0;
        this.encoder = charencoder;
    }

    public SessionOutputBufferImpl(
            final HttpTransportMetricsImpl metrics,
            final int buffersize) {
        this(metrics, buffersize, buffersize, null);
    }

    public void bind(final OutputStream outstream) {
        this.outstream = outstream;
    }

    public boolean isBound() {
        return this.outstream != null;
    }

    @Override
    public int capacity() {
        return this.buffer.capacity();
    }

    @Override
    public int length() {
        return this.buffer.length();
    }

    @Override
    public int available() {
        return capacity() - length();
    }

    private void streamWrite(final byte[] b, final int off, final int len) throws IOException {
        Asserts.notNull(outstream, "Output stream");
        this.outstream.write(b, off, len);
    }

    private void flushStream() throws IOException {
        if (this.outstream != null) {
            this.outstream.flush();
        }
    }

    private void flushBuffer() throws IOException {
        final int len = this.buffer.length();
        if (len > 0) {
            streamWrite(this.buffer.buffer(), 0, len);
            this.buffer.clear();
            this.metrics.incrementBytesTransferred(len);
        }
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
        flushStream();
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            return;
        }
        // Do not want to buffer large-ish chunks
        // if the byte array is larger then MIN_CHUNK_LIMIT
        // write it directly to the output stream
        if (len > this.fragementSizeHint || len > this.buffer.capacity()) {
            // flush the buffer
            flushBuffer();
            // write directly to the out stream
            streamWrite(b, off, len);
            this.metrics.incrementBytesTransferred(len);
        } else {
            // Do not let the buffer grow unnecessarily
            final int freecapacity = this.buffer.capacity() - this.buffer.length();
            if (len > freecapacity) {
                // flush the buffer
                flushBuffer();
            }
            // buffer
            this.buffer.append(b, off, len);
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        if (b == null) {
            return;
        }
        write(b, 0, b.length);
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.fragementSizeHint > 0) {
            if (this.buffer.isFull()) {
                flushBuffer();
            }
            this.buffer.append(b);
        } else {
            flushBuffer();
            this.outstream.write(b);
        }
    }

    /**
     * Writes characters from the specified string followed by a line delimiter
     * to this session buffer.
     * <p>
     * This method uses CR-LF as a line delimiter.
     *
     * @param      s   the line.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void writeLine(final String s) throws IOException {
        if (s == null) {
            return;
        }
        if (s.length() > 0) {
            if (this.encoder == null) {
                for (int i = 0; i < s.length(); i++) {
                    write(s.charAt(i));
                }
            } else {
                final CharBuffer cbuf = CharBuffer.wrap(s);
                writeEncoded(cbuf);
            }
        }
        write(CRLF);
    }

    /**
     * Writes characters from the specified char array followed by a line
     * delimiter to this session buffer.
     * <p>
     * This method uses CR-LF as a line delimiter.
     *
     * @param      charbuffer the buffer containing chars of the line.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void writeLine(final CharArrayBuffer charbuffer) throws IOException {
        if (charbuffer == null) {
            return;
        }
        if (this.encoder == null) {
            int off = 0;
            int remaining = charbuffer.length();
            while (remaining > 0) {
                int chunk = this.buffer.capacity() - this.buffer.length();
                chunk = Math.min(chunk, remaining);
                if (chunk > 0) {
                    this.buffer.append(charbuffer, off, chunk);
                }
                if (this.buffer.isFull()) {
                    flushBuffer();
                }
                off += chunk;
                remaining -= chunk;
            }
        } else {
            final CharBuffer cbuf = CharBuffer.wrap(charbuffer.buffer(), 0, charbuffer.length());
            writeEncoded(cbuf);
        }
        write(CRLF);
    }

    private void writeEncoded(final CharBuffer cbuf) throws IOException {
        if (!cbuf.hasRemaining()) {
            return;
        }
        if (this.bbuf == null) {
            this.bbuf = ByteBuffer.allocate(1024);
        }
        this.encoder.reset();
        while (cbuf.hasRemaining()) {
            final CoderResult result = this.encoder.encode(cbuf, this.bbuf, true);
            handleEncodingResult(result);
        }
        final CoderResult result = this.encoder.flush(this.bbuf);
        handleEncodingResult(result);
        this.bbuf.clear();
    }

    private void handleEncodingResult(final CoderResult result) throws IOException {
        if (result.isError()) {
            result.throwException();
        }
        this.bbuf.flip();
        while (this.bbuf.hasRemaining()) {
            write(this.bbuf.get());
        }
        this.bbuf.compact();
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }

}
