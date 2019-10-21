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

import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.util.Args;

/**
 * Output stream that cuts off after a defined number of bytes. This class
 * is used to send content of HTTP messages where the end of the content entity
 * is determined by the value of the {@code Content-Length header}.
 * Entities transferred using this stream can be maximum {@link Long#MAX_VALUE}
 * long.
 * <p>
 * Note that this class NEVER closes the underlying stream, even when close
 * gets called.  Instead, the stream will be marked as closed and no further
 * output will be permitted.
 *
 * @since 4.0
 */
public class ContentLengthOutputStream extends OutputStream {

    /**
     * Wrapped session output buffer.
     */
    private final SessionOutputBuffer out;

    /**
     * The maximum number of bytes that can be written the stream. Subsequent
     * write operations will be ignored.
     */
    private final long contentLength;

    /** Total bytes written */
    private long total = 0;

    /** True if the stream is closed. */
    private boolean closed = false;

    /**
     * Wraps a session output buffer and cuts off output after a defined number
     * of bytes.
     *
     * @param out The session output buffer
     * @param contentLength The maximum number of bytes that can be written to
     * the stream. Subsequent write operations will be ignored.
     *
     * @since 4.0
     */
    public ContentLengthOutputStream(final SessionOutputBuffer out, final long contentLength) {
        super();
        this.out = Args.notNull(out, "Session output buffer");
        this.contentLength = Args.notNegative(contentLength, "Content length");
    }

    /**
     * <p>Does not close the underlying socket output.</p>
     *
     * @throws IOException If an I/O problem occurs.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.out.flush();
        }
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        if (this.total < this.contentLength) {
            final long max = this.contentLength - this.total;
            int chunk = len;
            if (chunk > max) {
                chunk = (int) max;
            }
            this.out.write(b, off, chunk);
            this.total += chunk;
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        }
        if (this.total < this.contentLength) {
            this.out.write(b);
            this.total++;
        }
    }

}
