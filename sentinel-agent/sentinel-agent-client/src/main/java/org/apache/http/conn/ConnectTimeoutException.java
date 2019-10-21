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

package org.apache.http.conn;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.apache.http.HttpHost;

/**
 * A timeout while connecting to an HTTP server or waiting for an
 * available connection from an HttpConnectionManager.
 *
 *
 * @since 4.0
 */
public class ConnectTimeoutException extends InterruptedIOException {

    private static final long serialVersionUID = -4816682903149535989L;

    private final HttpHost host;

    /**
     * Creates a ConnectTimeoutException with a {@code null} detail message.
     */
    public ConnectTimeoutException() {
        super();
        this.host = null;
    }

    /**
     * Creates a ConnectTimeoutException with the specified detail message.
     */
    public ConnectTimeoutException(final String message) {
        super(message);
        this.host = null;
    }

    /**
     * Creates a ConnectTimeoutException based on original {@link IOException}.
     *
     * @since 4.3
     */
    public ConnectTimeoutException(
            final IOException cause,
            final HttpHost host,
            final InetAddress... remoteAddresses) {
        super("Connect to " +
                (host != null ? host.toHostString() : "remote host") +
                (remoteAddresses != null && remoteAddresses.length > 0 ?
                        " " + Arrays.asList(remoteAddresses) : "") +
                ((cause != null && cause.getMessage() != null) ?
                        " failed: " + cause.getMessage() : " timed out"));
        this.host = host;
        initCause(cause);
    }

    /**
     * @since 4.3
     */
    public HttpHost getHost() {
        return host;
    }

}
