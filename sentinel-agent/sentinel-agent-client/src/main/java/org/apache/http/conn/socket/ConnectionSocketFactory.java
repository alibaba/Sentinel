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

package org.apache.http.conn.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.protocol.HttpContext;

/**
 * A factory for creating and connecting connection sockets.
 *
 * @since 4.3
 */
public interface ConnectionSocketFactory {

    /**
     * Creates new, unconnected socket. The socket should subsequently be passed to
     * {@link #connectSocket(int, Socket, HttpHost, InetSocketAddress, InetSocketAddress,
     *    HttpContext) connectSocket} method.
     *
     * @return  a new socket
     *
     * @throws IOException if an I/O error occurs while creating the socket
     */
    Socket createSocket(HttpContext context) throws IOException;

    /**
     * Connects the socket to the target host with the given resolved remote address.
     *
     * @param connectTimeout connect timeout.
     * @param sock the socket to connect, as obtained from {@link #createSocket(HttpContext)}.
     * {@code null} indicates that a new socket should be created and connected.
     * @param host target host as specified by the caller (end user).
     * @param remoteAddress the resolved remote address to connect to.
     * @param localAddress the local address to bind the socket to, or {@code null} for any.
     * @param context the actual HTTP context.
     *
     * @return  the connected socket. The returned object may be different
     *          from the {@code sock} argument if this factory supports
     *          a layered protocol.
     *
     * @throws IOException if an I/O error occurs
     */
    Socket connectSocket(
        int connectTimeout,
        Socket sock,
        HttpHost host,
        InetSocketAddress remoteAddress,
        InetSocketAddress localAddress,
        HttpContext context) throws IOException;

}
