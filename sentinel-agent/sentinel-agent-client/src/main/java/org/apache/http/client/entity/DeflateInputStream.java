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
package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * Deflate input stream.    This class includes logic needed for various Rfc's in order
 * to reasonably implement the "deflate" compression style.
 */
public class DeflateInputStream extends InputStream {

    private final InputStream sourceStream;

    public DeflateInputStream(final InputStream wrapped) throws IOException {

        final PushbackInputStream pushback = new PushbackInputStream(wrapped, 2);
        final int i1 = pushback.read();
        final int i2 = pushback.read();
        if (i1 == -1 || i2 == -1) {
            throw new ZipException("Unexpected end of stream");
        }

        pushback.unread(i2);
        pushback.unread(i1);

        boolean nowrap = true;
        final int b1 = i1 & 0xFF;
        final int compressionMethod = b1 & 0xF;
        final int compressionInfo = b1 >> 4 & 0xF;
        final int b2 = i2 & 0xFF;
        if (compressionMethod == 8 && compressionInfo <= 7 && ((b1 << 8) | b2) % 31 == 0) {
            nowrap = false;
        }
        sourceStream = new DeflateStream(pushback, new Inflater(nowrap));
    }

    /**
     * Read a byte.
     */
    @Override
    public int read() throws IOException {
        return sourceStream.read();
    }

    /**
     * Read lots of bytes.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return sourceStream.read(b);
    }

    /**
     * Read lots of specific bytes.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return sourceStream.read(b, off, len);
    }

    /**
     * Skip
     */
    @Override
    public long skip(final long n) throws IOException {
        return sourceStream.skip(n);
    }

    /**
     * Get available.
     */
    @Override
    public int available() throws IOException {
        return sourceStream.available();
    }

    /**
     * Mark.
     */
    @Override
    public void mark(final int readLimit) {
        sourceStream.mark(readLimit);
    }

    /**
     * Reset.
     */
    @Override
    public void reset() throws IOException {
        sourceStream.reset();
    }

    /**
     * Check if mark is supported.
     */
    @Override
    public boolean markSupported() {
        return sourceStream.markSupported();
    }

    /**
     * Close.
     */
    @Override
    public void close() throws IOException {
        sourceStream.close();
    }

    static class DeflateStream extends InflaterInputStream {

        private boolean closed = false;

        public DeflateStream(final InputStream in, final Inflater inflater) {
            super(in, inflater);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            inf.end();
            super.close();
        }

    }

}

