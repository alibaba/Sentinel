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
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Arrays;

import org.apache.http.HttpHost;

/**
 * A {@link ConnectException} that specifies the {@link HttpHost} that was
 * being connected to.
 *
 * @since 4.0
 */
public class HttpHostConnectException extends ConnectException {

    private static final long serialVersionUID = -3194482710275220224L;

    private final HttpHost host;

    /**
     * @deprecated (4.3) use {@link #HttpHostConnectException(java.io.IOException, org.apache.http.HttpHost,
     *   java.net.InetAddress...)}
     */
    @Deprecated
    public HttpHostConnectException(final HttpHost host, final ConnectException cause) {
        this(cause, host, (InetAddress[]) null);
    }

    /**
     * Creates a HttpHostConnectException based on original {@link java.io.IOException}.
     *
     * @since 4.3
     */
    public HttpHostConnectException(
            final IOException cause,
            final HttpHost host,
            final InetAddress... remoteAddresses) {
        super("Connect to " +
                (host != null ? host.toHostString() : "remote host") +
                (remoteAddresses != null && remoteAddresses .length > 0 ?
                        " " + Arrays.asList(remoteAddresses) : "") +
                ((cause != null && cause.getMessage() != null) ?
                        " failed: " + cause.getMessage() : " refused"));
        this.host = host;
        initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }

}
