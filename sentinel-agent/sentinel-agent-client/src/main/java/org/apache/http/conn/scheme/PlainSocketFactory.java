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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * The default class for creating plain (unencrypted) sockets.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.conn.socket.PlainConnectionSocketFactory}
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class PlainSocketFactory implements SocketFactory, SchemeSocketFactory {

    private final HostNameResolver nameResolver;

    /**
     * Gets the default factory.
     *
     * @return the default factory
     */
    public static PlainSocketFactory getSocketFactory() {
        return new PlainSocketFactory();
    }

    /**
     * @deprecated (4.1) use {@link org.apache.http.conn.DnsResolver}
     */
    @Deprecated
    public PlainSocketFactory(final HostNameResolver nameResolver) {
        super();
        this.nameResolver = nameResolver;
    }

    public PlainSocketFactory() {
        super();
        this.nameResolver = null;
    }

    /**
     * @param params Optional parameters. Parameters passed to this method will have no effect.
     *               This method will create a unconnected instance of {@link Socket} class
     *               using default constructor.
     *
     * @since 4.1
     */
    @Override
    public Socket createSocket(final HttpParams params) {
        return new Socket();
    }

    @Override
    public Socket createSocket() {
        return new Socket();
    }

    /**
     * @since 4.1
     */
    @Override
    public Socket connectSocket(
            final Socket socket,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpParams params) throws IOException, ConnectTimeoutException {
        Args.notNull(remoteAddress, "Remote address");
        Args.notNull(params, "HTTP parameters");
        Socket sock = socket;
        if (sock == null) {
            sock = createSocket();
        }
        if (localAddress != null) {
            sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
            sock.bind(localAddress);
        }
        final int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        final int soTimeout = HttpConnectionParams.getSoTimeout(params);

        try {
            sock.setSoTimeout(soTimeout);
            sock.connect(remoteAddress, connTimeout);
        } catch (final SocketTimeoutException ex) {
            throw new ConnectTimeoutException("Connect to " + remoteAddress + " timed out");
        }
        return sock;
    }

    /**
     * Checks whether a socket connection is secure.
     * This factory creates plain socket connections
     * which are not considered secure.
     *
     * @param sock      the connected socket
     *
     * @return  {@code false}
     */
    @Override
    public final boolean isSecure(final Socket sock) {
        return false;
    }

    /**
     * @deprecated (4.1)  Use {@link #connectSocket(Socket, InetSocketAddress, InetSocketAddress, HttpParams)}
     */
    @Override
    @Deprecated
    public Socket connectSocket(
            final Socket socket,
            final String host, final int port,
            final InetAddress localAddress, final int localPort,
            final HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        InetSocketAddress local = null;
        if (localAddress != null || localPort > 0) {
            local = new InetSocketAddress(localAddress, localPort > 0 ? localPort : 0);
        }
        final InetAddress remoteAddress;
        if (this.nameResolver != null) {
            remoteAddress = this.nameResolver.resolve(host);
        } else {
            remoteAddress = InetAddress.getByName(host);
        }
        final InetSocketAddress remote = new InetSocketAddress(remoteAddress, port);
        return connectSocket(socket, remote, local, params);
    }

}
