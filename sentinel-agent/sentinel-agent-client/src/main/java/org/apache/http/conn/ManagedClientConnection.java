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
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * A client-side connection with advanced connection logic.
 * Instances are typically obtained from a connection manager.
 *
 * @since 4.0
 *
 * @deprecated (4.3) replaced by {@link HttpClientConnectionManager}.
 */
@Deprecated
public interface ManagedClientConnection extends
    HttpClientConnection, HttpRoutedConnection, ManagedHttpClientConnection, ConnectionReleaseTrigger {

    /**
     * Indicates whether this connection is secure.
     * The return value is well-defined only while the connection is open.
     * It may change even while the connection is open.
     *
     * @return  {@code true} if this connection is secure,
     *          {@code false} otherwise
     */
    @Override
    boolean isSecure();

    /**
     * Obtains the current route of this connection.
     *
     * @return  the route established so far, or
     *          {@code null} if not connected
     */
    @Override
    HttpRoute getRoute();

    /**
     * Obtains the SSL session of the underlying connection, if any.
     * If this connection is open, and the underlying socket is an
     * {@link javax.net.ssl.SSLSocket SSLSocket}, the SSL session of
     * that socket is obtained. This is a potentially blocking operation.
     * <p>
     * <b>Note:</b> Whether the underlying socket is an SSL socket
     * can not necessarily be determined via {@link #isSecure}.
     * Plain sockets may be considered secure, for example if they are
     * connected to a known host in the same network segment.
     * On the other hand, SSL sockets may be considered insecure,
     * for example depending on the chosen cipher suite.
     * </p>
     *
     * @return  the underlying SSL session if available,
     *          {@code null} otherwise
     */
    @Override
    SSLSession getSSLSession();

    /**
     * Opens this connection according to the given route.
     *
     * @param route     the route along which to open. It will be opened to
     *                  the first proxy if present, or directly to the target.
     * @param context   the context for opening this connection
     * @param params    the parameters for opening this connection
     *
     * @throws IOException      in case of a problem
     */
    void open(HttpRoute route, HttpContext context, HttpParams params)
        throws IOException;

    /**
     * Indicates that a tunnel to the target has been established.
     * The route is the one previously passed to {@link #open open}.
     * Subsequently, {@link #layerProtocol layerProtocol} can be called
     * to layer the TLS/SSL protocol on top of the tunnelled connection.
     * <p>
     * <b>Note:</b> In HttpClient 3, a call to the corresponding method
     * would automatically trigger the layering of the TLS/SSL protocol.
     * This is not the case anymore, you can establish a tunnel without
     * layering a new protocol over the connection.
     * </p>
     *
     * @param secure    {@code true} if the tunnel should be considered
     *                  secure, {@code false} otherwise
     * @param params    the parameters for tunnelling this connection
     *
     * @throws IOException  in case of a problem
     */
    void tunnelTarget(boolean secure, HttpParams params)
        throws IOException;

    /**
     * Indicates that a tunnel to an intermediate proxy has been established.
     * This is used exclusively for so-called <i>proxy chains</i>, where
     * a request has to pass through multiple proxies before reaching the
     * target. In that case, all proxies but the last need to be tunnelled
     * when establishing the connection. Tunnelling of the last proxy to the
     * target is optional and would be indicated via {@link #tunnelTarget}.
     *
     * @param next      the proxy to which the tunnel was established.
     *                  This is <i>not</i> the proxy <i>through</i> which
     *                  the tunnel was established, but the new end point
     *                  of the tunnel. The tunnel does <i>not</i> yet
     *                  reach to the target, use {@link #tunnelTarget}
     *                  to indicate an end-to-end tunnel.
     * @param secure    {@code true} if the connection should be
     *                  considered secure, {@code false} otherwise
     * @param params    the parameters for tunnelling this connection
     *
     * @throws IOException  in case of a problem
     */
    void tunnelProxy(HttpHost next, boolean secure, HttpParams params)
        throws IOException;

    /**
     * Layers a new protocol on top of a {@link #tunnelTarget tunnelled}
     * connection. This is typically used to create a TLS/SSL connection
     * through a proxy.
     * The route is the one previously passed to {@link #open open}.
     * It is not guaranteed that the layered connection is
     * {@link #isSecure secure}.
     *
     * @param context   the context for layering on top of this connection
     * @param params    the parameters for layering on top of this connection
     *
     * @throws IOException      in case of a problem
     */
    void layerProtocol(HttpContext context, HttpParams params)
        throws IOException;

    /**
     * Marks this connection as being in a reusable communication state.
     * The checkpoints for reuseable communication states (in the absence
     * of pipelining) are before sending a request and after receiving
     * the response in its entirety.
     * The connection will automatically clear the checkpoint when
     * used for communication. A call to this method indicates that
     * the next checkpoint has been reached.
     * <p>
     * A reusable communication state is necessary but not sufficient
     * for the connection to be reused.
     * A {@link #getRoute route} mismatch, the connection being closed,
     * or other circumstances might prevent reuse.
     * </p>
     */
    void markReusable();

    /**
     * Marks this connection as not being in a reusable state.
     * This can be used immediately before releasing this connection
     * to prevent its reuse. Reasons for preventing reuse include
     * error conditions and the evaluation of a
     * {@link org.apache.http.ConnectionReuseStrategy reuse strategy}.
     * <p>
     * <b>Note:</b>
     * It is <i>not</i> necessary to call here before writing to
     * or reading from this connection. Communication attempts will
     * automatically unmark the state as non-reusable. It can then
     * be switched back using {@link #markReusable markReusable}.
     * </p>
     */
    void unmarkReusable();

    /**
     * Indicates whether this connection is in a reusable communication state.
     * See {@link #markReusable markReusable} and
     * {@link #unmarkReusable unmarkReusable} for details.
     *
     * @return  {@code true} if this connection is marked as being in
     *          a reusable communication state,
     *          {@code false} otherwise
     */
    boolean isMarkedReusable();

    /**
     * Assigns a state object to this connection. Connection managers may make
     * use of the connection state when allocating persistent connections.
     *
     * @param state The state object
     */
    void setState(Object state);

    /**
     * Returns the state object associated with this connection.
     *
     * @return The state object
     */
    Object getState();

    /**
     * Sets the duration that this connection can remain idle before it is
     * reused. The connection should not be used again if this time elapses. The
     * idle duration must be reset after each request sent over this connection.
     * The elapsed time starts counting when the connection is released, which
     * is typically after the headers (and any response body, if present) is
     * fully consumed.
     */
    void setIdleDuration(long duration, TimeUnit unit);

}
