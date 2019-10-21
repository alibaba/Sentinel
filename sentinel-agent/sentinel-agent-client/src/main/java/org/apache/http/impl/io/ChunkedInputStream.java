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
import java.io.InputStream;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.TruncatedChunkException;
import org.apache.http.config.MessageConstraints;
import org.apache.http.io.BufferInfo;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 * Implements chunked transfer coding. The content is received in small chunks.
 * Entities transferred using this input stream can be of unlimited length.
 * After the stream is read to the end, it provides access to the trailers,
 * if any.
 * <p>
 * Note that this class NEVER closes the underlying stream, even when close
 * gets called.  Instead, it will read until the "end" of its chunking on
 * close, which allows for the seamless execution of subsequent HTTP 1.1
 * requests, while not requiring the client to remember to read the entire
 * contents of the response.
 *
 *
 * @since 4.0
 *
 */
public class ChunkedInputStream extends InputStream {

    private static final int CHUNK_LEN               = 1;
    private static final int CHUNK_DATA              = 2;
    private static final int CHUNK_CRLF              = 3;
    private static final int CHUNK_INVALID           = Integer.MAX_VALUE;

    private static final int BUFFER_SIZE = 2048;

    /** The session input buffer */
    private final SessionInputBuffer in;
    private final CharArrayBuffer buffer;
    private final MessageConstraints constraints;

    private int state;

    /** The chunk size */
    private long chunkSize;

    /** The current position within the current chunk */
    private long pos;

    /** True if we've reached the end of stream */
    private boolean eof = false;

    /** True if this stream is closed */
    private boolean closed = false;

    private Header[] footers = new Header[] {};

    /**
     * Wraps session input stream and reads chunk coded input.
     *
     * @param in The session input buffer
     * @param constraints Message constraints. If {@code null}
     *   {@link MessageConstraints#DEFAULT} will be used.
     *
     * @since 4.4
     */
    public ChunkedInputStream(final SessionInputBuffer in, final MessageConstraints constraints) {
        super();
        this.in = Args.notNull(in, "Session input buffer");
        this.pos = 0L;
        this.buffer = new CharArrayBuffer(16);
        this.constraints = constraints != null ? constraints : MessageConstraints.DEFAULT;
        this.state = CHUNK_LEN;
    }

    /**
     * Wraps session input stream and reads chunk coded input.
     *
     * @param in The session input buffer
     */
    public ChunkedInputStream(final SessionInputBuffer in) {
        this(in, null);
    }

    @Override
    public int available() throws IOException {
        if (this.in instanceof BufferInfo) {
            final int len = ((BufferInfo) this.in).length();
            return (int) Math.min(len, this.chunkSize - this.pos);
        } else {
            return 0;
        }
    }

    /**
     * <p> Returns all the data in a chunked stream in coalesced form. A chunk
     * is followed by a CRLF. The method returns -1 as soon as a chunksize of 0
     * is detected.</p>
     *
     * <p> Trailer headers are read automatically at the end of the stream and
     * can be obtained with the getResponseFooters() method.</p>
     *
     * @return -1 of the end of the stream has been reached or the next data
     * byte
     * @throws IOException in case of an I/O error
     */
    @Override
    public int read() throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (this.eof) {
            return -1;
        }
        if (state != CHUNK_DATA) {
            nextChunk();
            if (this.eof) {
                return -1;
            }
        }
        final int b = in.read();
        if (b != -1) {
            pos++;
            if (pos >= chunkSize) {
                state = CHUNK_CRLF;
            }
        }
        return b;
    }

    /**
     * Read some bytes from the stream.
     * @param b The byte array that will hold the contents from the stream.
     * @param off The offset into the byte array at which bytes will start to be
     * placed.
     * @param len the maximum number of bytes that can be returned.
     * @return The number of bytes returned or -1 if the end of stream has been
     * reached.
     * @throws IOException in case of an I/O error
     */
    @Override
    public int read (final byte[] b, final int off, final int len) throws IOException {

        if (closed) {
            throw new IOException("Attempted read from closed stream.");
        }

        if (eof) {
            return -1;
        }
        if (state != CHUNK_DATA) {
            nextChunk();
            if (eof) {
                return -1;
            }
        }
        final int bytesRead = in.read(b, off, (int) Math.min(len, chunkSize - pos));
        if (bytesRead != -1) {
            pos += bytesRead;
            if (pos >= chunkSize) {
                state = CHUNK_CRLF;
            }
            return bytesRead;
        } else {
            eof = true;
            throw new TruncatedChunkException("Truncated chunk "
                    + "( expected size: " + chunkSize
                    + "; actual size: " + pos + ")");
        }
    }

    /**
     * Read some bytes from the stream.
     * @param b The byte array that will hold the contents from the stream.
     * @return The number of bytes returned or -1 if the end of stream has been
     * reached.
     * @throws IOException in case of an I/O error
     */
    @Override
    public int read (final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Read the next chunk.
     * @throws IOException in case of an I/O error
     */
    private void nextChunk() throws IOException {
        if (state == CHUNK_INVALID) {
            throw new MalformedChunkCodingException("Corrupt data stream");
        }
        try {
            chunkSize = getChunkSize();
            if (chunkSize < 0L) {
                throw new MalformedChunkCodingException("Negative chunk size");
            }
            state = CHUNK_DATA;
            pos = 0L;
            if (chunkSize == 0L) {
                eof = true;
                parseTrailerHeaders();
            }
        } catch (final MalformedChunkCodingException ex) {
            state = CHUNK_INVALID;
            throw ex;
        }
    }

    /**
     * Expects the stream to start with a chunksize in hex with optional
     * comments after a semicolon. The line must end with a CRLF: "a3; some
     * comment\r\n" Positions the stream at the start of the next line.
     */
    private long getChunkSize() throws IOException {
        final int st = this.state;
        switch (st) {
        case CHUNK_CRLF:
            this.buffer.clear();
            final int bytesRead1 = this.in.readLine(this.buffer);
            if (bytesRead1 == -1) {
                throw new MalformedChunkCodingException(
                    "CRLF expected at end of chunk");
            }
            if (!this.buffer.isEmpty()) {
                throw new MalformedChunkCodingException(
                    "Unexpected content at the end of chunk");
            }
            state = CHUNK_LEN;
            //$FALL-THROUGH$
        case CHUNK_LEN:
            this.buffer.clear();
            final int bytesRead2 = this.in.readLine(this.buffer);
            if (bytesRead2 == -1) {
                throw new ConnectionClosedException("Premature end of chunk coded message body: " +
                        "closing chunk expected");
            }
            int separator = this.buffer.indexOf(';');
            if (separator < 0) {
                separator = this.buffer.length();
            }
            final String s = this.buffer.substringTrimmed(0, separator);
            try {
                return Long.parseLong(s, 16);
            } catch (final NumberFormatException e) {
                throw new MalformedChunkCodingException("Bad chunk header: " + s);
            }
        default:
            throw new IllegalStateException("Inconsistent codec state");
        }
    }

    /**
     * Reads and stores the Trailer headers.
     * @throws IOException in case of an I/O error
     */
    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParser.parseHeaders(in,
                    constraints.getMaxHeaderCount(),
                    constraints.getMaxLineLength(),
                    null);
        } catch (final HttpException ex) {
            final IOException ioe = new MalformedChunkCodingException("Invalid footer: "
                    + ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Upon close, this reads the remainder of the chunked message,
     * leaving the underlying socket at a position to start reading the
     * next response without scanning.
     * @throws IOException in case of an I/O error
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                if (!eof && state != CHUNK_INVALID) {
                    // read and discard the remainder of the message
                    final byte buff[] = new byte[BUFFER_SIZE];
                    while (read(buff) >= 0) {
                    }
                }
            } finally {
                eof = true;
                closed = true;
            }
        }
    }

    public Header[] getFooters() {
        return this.footers.clone();
    }

}
