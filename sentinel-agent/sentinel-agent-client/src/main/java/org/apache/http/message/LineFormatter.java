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

package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.util.CharArrayBuffer;

/**
 * Interface for formatting elements of the HEAD section of an HTTP message.
 * This is the complement to {@link LineParser}.
 * There are individual methods for formatting a request line, a
 * status line, or a header line. The formatting does <i>not</i> include the
 * trailing line break sequence CR-LF.
 * Instances of this interface are expected to be stateless and thread-safe.
 *
 * <p>
 * The formatted lines are returned in memory, the formatter does not depend
 * on any specific IO mechanism.
 * In order to avoid unnecessary creation of temporary objects,
 * a buffer can be passed as argument to all formatting methods.
 * The implementation may or may not actually use that buffer for formatting.
 * If it is used, the buffer will first be cleared by the
 * {@code formatXXX} methods.
 * The argument buffer can always be re-used after the call. The buffer
 * returned as the result, if it is different from the argument buffer,
 * MUST NOT be modified.
 * </p>
 *
 * @since 4.0
 */
public interface LineFormatter {

    /**
     * Formats a protocol version.
     * This method does <i>not</i> follow the general contract for
     * {@code buffer} arguments.
     * It does <i>not</i> clear the argument buffer, but appends instead.
     * The returned buffer can always be modified by the caller.
     * Because of these differing conventions, it is not named
     * {@code formatProtocolVersion}.
     *
     * @param buffer    a buffer to which to append, or {@code null}
     * @param version   the protocol version to format
     *
     * @return  a buffer with the formatted protocol version appended.
     *          The caller is allowed to modify the result buffer.
     *          If the {@code buffer} argument is not {@code null},
     *          the returned buffer is the argument buffer.
     */
    CharArrayBuffer appendProtocolVersion(CharArrayBuffer buffer,
                                          ProtocolVersion version);

    /**
     * Formats a request line.
     *
     * @param buffer    a buffer available for formatting, or
     *                  {@code null}.
     *                  The buffer will be cleared before use.
     * @param reqline   the request line to format
     *
     * @return  the formatted request line
     */
    CharArrayBuffer formatRequestLine(CharArrayBuffer buffer,
                                      RequestLine reqline);

    /**
     * Formats a status line.
     *
     * @param buffer    a buffer available for formatting, or
     *                  {@code null}.
     *                  The buffer will be cleared before use.
     * @param statline  the status line to format
     *
     * @return  the formatted status line
     *
     * @throws org.apache.http.ParseException        in case of a parse error
     */
    CharArrayBuffer formatStatusLine(CharArrayBuffer buffer,
                                     StatusLine statline);

    /**
     * Formats a header.
     * Due to header continuation, the result may be multiple lines.
     * In order to generate well-formed HTTP, the lines in the result
     * must be separated by the HTTP line break sequence CR-LF.
     * There is <i>no</i> trailing CR-LF in the result.
     * <p>
     * See the class comment for details about the buffer argument.
     * </p>
     *
     * @param buffer    a buffer available for formatting, or
     *                  {@code null}.
     *                  The buffer will be cleared before use.
     * @param header    the header to format
     *
     * @return  a buffer holding the formatted header, never {@code null}.
     *          The returned buffer may be different from the argument buffer.
     *
     * @throws org.apache.http.ParseException        in case of a parse error
     */
    CharArrayBuffer formatHeader(CharArrayBuffer buffer,
                                 Header header);

}
