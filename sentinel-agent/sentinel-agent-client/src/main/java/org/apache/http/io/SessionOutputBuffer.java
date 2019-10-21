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

package org.apache.http.io;

import java.io.IOException;

import org.apache.http.util.CharArrayBuffer;

/**
 * Session output buffer for blocking connections. This interface is similar to
 * OutputStream class, but it also provides methods for writing lines of text.
 * <p>
 * Implementing classes are also expected to manage intermediate data buffering
 * for optimal output performance.
 *
 * @since 4.0
 */
public interface SessionOutputBuffer {

    /**
     * Writes {@code len} bytes from the specified byte array
     * starting at offset {@code off} to this session buffer.
     * <p>
     * If {@code off} is negative, or {@code len} is negative, or
     * {@code off+len} is greater than the length of the array
     * {@code b}, then an {@code IndexOutOfBoundsException} is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(byte[] b, int off, int len) throws IOException;

    /**
     * Writes {@code b.length} bytes from the specified byte array
     * to this session buffer.
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(byte[] b) throws IOException;

    /**
     * Writes the specified byte to this session buffer.
     *
     * @param      b   the {@code byte}.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(int b) throws IOException;

    /**
     * Writes characters from the specified string followed by a line delimiter
     * to this session buffer.
     * <p>
     * The choice of a char encoding and line delimiter sequence is up to the
     * specific implementations of this interface.
     *
     * @param      s   the line.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeLine(String s) throws IOException;

    /**
     * Writes characters from the specified char array followed by a line
     * delimiter to this session buffer.
     * <p>
     * The choice of a char encoding and line delimiter sequence is up to the
     * specific implementations of this interface.
     *
     * @param      buffer   the buffer containing chars of the line.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeLine(CharArrayBuffer buffer) throws IOException;

    /**
     * Flushes this session buffer and forces any buffered output bytes
     * to be written out. The general contract of {@code flush} is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    void flush() throws IOException;

    /**
     * Returns {@link HttpTransportMetrics} for this session buffer.
     *
     * @return transport metrics.
     */
    HttpTransportMetrics getMetrics();

}
