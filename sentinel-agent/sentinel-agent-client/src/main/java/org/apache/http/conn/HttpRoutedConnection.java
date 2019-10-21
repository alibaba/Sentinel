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

import javax.net.ssl.SSLSession;

import org.apache.http.HttpInetConnection;
import org.apache.http.conn.routing.HttpRoute;

/**
 * Interface to access routing information of a client side connection.
 *
 * @since 4.1
 *
 * @deprecated (4.3) replaced by {@link HttpClientConnectionManager}.
 */
@Deprecated
public interface HttpRoutedConnection extends HttpInetConnection {

    /**
     * Indicates whether this connection is secure.
     * The return value is well-defined only while the connection is open.
     * It may change even while the connection is open.
     *
     * @return  {@code true} if this connection is secure,
     *          {@code false} otherwise
     */
    boolean isSecure();

    /**
     * Obtains the current route of this connection.
     *
     * @return  the route established so far, or
     *          {@code null} if not connected
     */
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
    SSLSession getSSLSession();

}
