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

package org.apache.http.conn.scheme;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpParams;

/**
 * A factory for creating, initializing and connecting sockets. The factory encapsulates the logic
 * for establishing a socket connection.
 *
 * @since 4.1
 *
 * @deprecated (4.3) use {@link org.apache.http.conn.socket.ConnectionSocketFactory}
 */
@Deprecated
public interface SchemeSocketFactory {

    /**
     * Creates a new, unconnected socket. The socket should subsequently be passed to
     * {@link #connectSocket(Socket, InetSocketAddress, InetSocketAddress, HttpParams)}.
     *
     * @param params    Optional {@link HttpParams parameters}. In most cases these parameters
     *                  will not be required and will have no effect, as usually socket
     *                  initialization should take place in the
     *                  {@link #connectSocket(Socket, InetSocketAddress, InetSocketAddress, HttpParams)}
     *                  method. However, in rare cases one may want to pass additional parameters
     *                  to this method in order to create a customized {@link Socket} instance,
     *                  for instance bound to a SOCKS proxy server.
     *
     * @return  a new socket
     *
     * @throws IOException if an I/O error occurs while creating the socket
     */
    Socket createSocket(HttpParams params) throws IOException;

    /**
     * Connects a socket to the target host with the given remote address.
     * <p>
     * Please note that {@link org.apache.http.conn.HttpInetSocketAddress} class should
     * be used in order to pass the target remote address along with the original
     * {@link org.apache.http.HttpHost} value used to resolve the address. The use of
     * {@link org.apache.http.conn.HttpInetSocketAddress} can also ensure that no reverse
     * DNS lookup will be performed if the target remote address was specified
     * as an IP address.
     * </p>
     *
     * @param sock      the socket to connect, as obtained from
     *                  {@link #createSocket(HttpParams) createSocket}.
     *                  {@code null} indicates that a new socket
     *                  should be created and connected.
     * @param remoteAddress the remote address to connect to.
     * @param localAddress the local address to bind the socket to, or
     *                  {@code null} for any
     * @param params    additional {@link HttpParams parameters} for connecting
     *
     * @return  the connected socket. The returned object may be different
     *          from the {@code sock} argument if this factory supports
     *          a layered protocol.
     *
     * @throws IOException if an I/O error occurs
     * @throws UnknownHostException if the IP address of the target host
     *          can not be determined
     * @throws ConnectTimeoutException if the socket cannot be connected
     *          within the time limit defined in the {@code params}
     *
     * @see org.apache.http.conn.HttpInetSocketAddress
     */
    Socket connectSocket(
        Socket sock,
        InetSocketAddress remoteAddress,
        InetSocketAddress localAddress,
        HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException;

    /**
     * Checks whether a socket provides a secure connection. The socket must be
     * {@link #connectSocket(Socket, InetSocketAddress, InetSocketAddress, HttpParams) connected}
     * by this factory. The factory will <i>not</i> perform I/O operations in this method.
     * <p>
     * As a rule of thumb, plain sockets are not secure and TLS/SSL sockets are secure. However,
     * there may be application specific deviations. For example, a plain socket to a host in the
     * same intranet ("trusted zone") could be considered secure. On the other hand, a TLS/SSL
     * socket could be considered insecure based on the cipher suite chosen for the connection.
     *
     * @param sock      the connected socket to check
     *
     * @return  {@code true} if the connection of the socket
     *          should be considered secure, or
     *          {@code false} if it should not
     *
     * @throws IllegalArgumentException
     *  if the argument is invalid, for example because it is
     *  not a connected socket or was created by a different
     *  socket factory.
     *  Note that socket factories are <i>not</i> required to
     *  check these conditions, they may simply return a default
     *  value when called with an invalid socket argument.
     */
    boolean isSecure(Socket sock) throws IllegalArgumentException;

}
