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
package org.apache.http.impl.conn;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.io.EofSensor;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.util.CharArrayBuffer;

/**
 * Logs all data read to the wire LOG.
 *
 * @since 4.0
 *
 * @deprecated (4.3) no longer used.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class LoggingSessionInputBuffer implements SessionInputBuffer, EofSensor {

    /** Original session input buffer. */
    private final SessionInputBuffer in;

    private final EofSensor eofSensor;

    /** The wire log to use for writing. */
    private final Wire wire;

    private final String charset;

    /**
     * Create an instance that wraps the specified session input buffer.
     * @param in The session input buffer.
     * @param wire The wire log to use.
     * @param charset protocol charset, {@code ASCII} if {@code null}
     */
    public LoggingSessionInputBuffer(
            final SessionInputBuffer in, final Wire wire, final String charset) {
        super();
        this.in = in;
        this.eofSensor = in instanceof EofSensor ? (EofSensor) in : null;
        this.wire = wire;
        this.charset = charset != null ? charset : Consts.ASCII.name();
    }

    public LoggingSessionInputBuffer(final SessionInputBuffer in, final Wire wire) {
        this(in, wire, null);
    }

    @Override
    public boolean isDataAvailable(final int timeout) throws IOException {
        return this.in.isDataAvailable(timeout);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int l = this.in.read(b,  off,  len);
        if (this.wire.enabled() && l > 0) {
            this.wire.input(b, off, l);
        }
        return l;
    }

    @Override
    public int read() throws IOException {
        final int l = this.in.read();
        if (this.wire.enabled() && l != -1) {
            this.wire.input(l);
        }
        return l;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int l = this.in.read(b);
        if (this.wire.enabled() && l > 0) {
            this.wire.input(b, 0, l);
        }
        return l;
    }

    @Override
    public String readLine() throws IOException {
        final String s = this.in.readLine();
        if (this.wire.enabled() && s != null) {
            final String tmp = s + "\r\n";
            this.wire.input(tmp.getBytes(this.charset));
        }
        return s;
    }

    @Override
    public int readLine(final CharArrayBuffer buffer) throws IOException {
        final int l = this.in.readLine(buffer);
        if (this.wire.enabled() && l >= 0) {
            final int pos = buffer.length() - l;
            final String s = new String(buffer.buffer(), pos, l);
            final String tmp = s + "\r\n";
            this.wire.input(tmp.getBytes(this.charset));
        }
        return l;
    }

    @Override
    public HttpTransportMetrics getMetrics() {
        return this.in.getMetrics();
    }

    @Override
    public boolean isEof() {
        if (this.eofSensor != null) {
            return this.eofSensor.isEof();
        } else {
            return false;
        }
    }

}
