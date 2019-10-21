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
import java.net.Socket;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpInetConnection;
import org.apache.http.params.HttpParams;

/**
 * A client-side connection that relies on outside logic to connect sockets to the
 * appropriate hosts. It can be operated directly by an application, or through an
 * {@link ClientConnectionOperator operator}.
 *
 * @since 4.0
 *
 * @deprecated (4.3) replaced by {@link HttpClientConnectionManager}.
 */
@Deprecated
public interface OperatedClientConnection extends HttpClientConnection, HttpInetConnection {

    /**
     * Obtains the target host for this connection.
     * If the connection is to a proxy but not tunnelled, this is
     * the proxy. If the connection is tunnelled through a proxy,
     * this is the target of the tunnel.
     * <p>
     * The return value is well-defined only while the connection is open.
     * It may change even while the connection is open,
     * because of an {@link #update update}.
     * </p>
     *
     * @return  the host to which this connection is opened
     */
    HttpHost getTargetHost();

    /**
     * Indicates whether this connection is secure.
     * The return value is well-defined only while the connection is open.
     * It may change even while the connection is open,
     * because of an {@link #update update}.
     *
     * @return  {@code true} if this connection is secure,
     *          {@code false} otherwise
     */
    boolean isSecure();

    /**
     * Obtains the socket for this connection.
     * The return value is well-defined only while the connection is open.
     * It may change even while the connection is open,
     * because of an {@link #update update}.
     *
     * @return  the socket for communicating with the
     *          {@link #getTargetHost target host}
     */
    Socket getSocket();

    /**
     * Signals that this connection is in the process of being open.
     * <p>
     * By calling this method, the connection can be re-initialized
     * with a new Socket instance before {@link #openCompleted} is called.
     * This enabled the connection to close that socket if
     * {@link org.apache.http.HttpConnection#shutdown shutdown}
     * is called before it is fully open. Closing an unconnected socket
     * will interrupt a thread that is blocked on the connect.
     * Otherwise, that thread will either time out on the connect,
     * or it returns successfully and then opens this connection
     * which was just shut down.
     * <p>
     * This method can be called multiple times if the connection
     * is layered over another protocol. <b>Note:</b> This method
     * will <i>not</i> close the previously used socket. It is
     * the caller's responsibility to close that socket if it is
     * no longer required.
     * <p>
     * The caller must invoke {@link #openCompleted} in order to complete
     * the process.
     *
     * @param sock      the unconnected socket which is about to
     *                  be connected.
     * @param target    the target host of this connection
     */
    void opening(Socket sock, HttpHost target)
        throws IOException;

    /**
     * Signals that the connection has been successfully open.
     * An attempt to call this method on an open connection will cause
     * an exception.
     *
     * @param secure    {@code true} if this connection is secure, for
     *                  example if an {@code SSLSocket} is used, or
     *                  {@code false} if it is not secure
     * @param params    parameters for this connection. The parameters will
     *                  be used when creating dependent objects, for example
     *                  to determine buffer sizes.
     */
    void openCompleted(boolean secure, HttpParams params)
        throws IOException;

    /**
     * Updates this connection.
     * A connection can be updated only while it is open.
     * Updates are used for example when a tunnel has been established,
     * or when a TLS/SSL connection has been layered on top of a plain
     * socket connection.
     * <p>
     * <b>Note:</b> Updating the connection will <i>not</i> close the
     * previously used socket. It is the caller's responsibility to close
     * that socket if it is no longer required.
     * </p>
     *
     * @param sock      the new socket for communicating with the target host,
     *                  or {@code null} to continue using the old socket.
     *                  If {@code null} is passed, helper objects that
     *                  depend on the socket should be re-used. In that case,
     *                  some changes in the parameters will not take effect.
     * @param target    the new target host of this connection
     * @param secure    {@code true} if this connection is now secure,
     *                  {@code false} if it is not secure
     * @param params    new parameters for this connection
     */
    void update(Socket sock, HttpHost target,
                boolean secure, HttpParams params)
        throws IOException;

}
