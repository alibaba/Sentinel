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

package org.apache.http;

/**
 * Signals that an HTTP protocol violation has occurred.
 * For example a malformed status line or headers, a missing message body, etc.
 *
 *
 * @since 4.0
 */
public class ProtocolException extends HttpException {

    private static final long serialVersionUID = -2143571074341228994L;

    /**
     * Creates a new ProtocolException with a {@code null} detail message.
     */
    public ProtocolException() {
        super();
    }

    /**
     * Creates a new ProtocolException with the specified detail message.
     *
     * @param message The exception detail message
     */
    public ProtocolException(final String message) {
        super(message);
    }

    /**
     * Creates a new ProtocolException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause the {@code Throwable} that caused this exception, or {@code null}
     * if the cause is unavailable, unknown, or not a {@code Throwable}
     */
    public ProtocolException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
