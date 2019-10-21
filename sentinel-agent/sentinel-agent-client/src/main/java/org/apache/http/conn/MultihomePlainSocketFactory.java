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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * Socket factory that implements a simple multi-home fail-over on connect failure,
 * provided the same hostname resolves to multiple {@link InetAddress}es. Please note
 * the {@link #connectSocket(Socket, String, int, InetAddress, int, HttpParams)}
 * method cannot be reliably interrupted by closing the socket returned by the
 * {@link #createSocket()} method.
 *
 * @since 4.0
 *
 * @deprecated (4.1)  Do not use. For multihome support socket factories must implement
 * {@link org.apache.http.conn.scheme.SchemeSocketFactory} interface.
 */
@Deprecated
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class MultihomePlainSocketFactory implements SocketFactory {

    /**
     * The factory singleton.
     */
    private static final
    MultihomePlainSocketFactory DEFAULT_FACTORY = new MultihomePlainSocketFactory();

    /**
     * Gets the singleton instance of this class.
     * @return the one and only plain socket factory
     */
    public static MultihomePlainSocketFactory getSocketFactory() {
        return DEFAULT_FACTORY;
    }

    /**
     * Restricted default constructor.
     */
    private MultihomePlainSocketFactory() {
        super();
    }


    // non-javadoc, see interface org.apache.http.conn.SocketFactory
    @Override
    public Socket createSocket() {
        return new Socket();
    }

    /**
     * Attempts to connects the socket to any of the {@link InetAddress}es the
     * given host name resolves to. If connection to all addresses fail, the
     * last I/O exception is propagated to the caller.
     *
     * @param socket socket to connect to any of the given addresses
     * @param host Host name to connect to
     * @param port the port to connect to
     * @param localAddress local address
     * @param localPort local port
     * @param params HTTP parameters
     *
     * @throws  IOException if an error occurs during the connection
     * @throws  SocketTimeoutException if timeout expires before connecting
     */
    @Override
    public Socket connectSocket(final Socket socket, final String host, final int port,
                                final InetAddress localAddress, final int localPort,
                                final HttpParams params)
        throws IOException {
        Args.notNull(host, "Target host");
        Args.notNull(params, "HTTP parameters");

        Socket sock = socket;
        if (sock == null) {
            sock = createSocket();
        }

        if ((localAddress != null) || (localPort > 0)) {
            final InetSocketAddress isa = new InetSocketAddress(localAddress,
                    localPort > 0 ? localPort : 0);
            sock.bind(isa);
        }

        final int timeout = HttpConnectionParams.getConnectionTimeout(params);

        final InetAddress[] inetadrs = InetAddress.getAllByName(host);
        final List<InetAddress> addresses = new ArrayList<InetAddress>(inetadrs.length);
        addresses.addAll(Arrays.asList(inetadrs));
        Collections.shuffle(addresses);

        IOException lastEx = null;
        for (final InetAddress remoteAddress: addresses) {
            try {
                sock.connect(new InetSocketAddress(remoteAddress, port), timeout);
                break;
            } catch (final SocketTimeoutException ex) {
                throw new ConnectTimeoutException("Connect to " + remoteAddress + " timed out");
            } catch (final IOException ex) {
                // create new socket
                sock = new Socket();
                // keep the last exception and retry
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
        return sock;
    } // connectSocket


    /**
     * Checks whether a socket connection is secure.
     * This factory creates plain socket connections
     * which are not considered secure.
     *
     * @param sock      the connected socket
     *
     * @return  {@code false}
     *
     * @throws IllegalArgumentException if the argument is invalid
     */
    @Override
    public final boolean isSecure(final Socket sock)
        throws IllegalArgumentException {

        Args.notNull(sock, "Socket");
        // This check is performed last since it calls a method implemented
        // by the argument object. getClass() is final in java.lang.Object.
        Asserts.check(!sock.isClosed(), "Socket is closed");
        return false;

    } // isSecure

}
