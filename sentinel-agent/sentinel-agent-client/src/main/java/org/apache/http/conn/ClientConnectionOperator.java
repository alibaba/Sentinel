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

import org.apache.http.HttpHost;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * ClientConnectionOperator represents a strategy for creating
 * {@link OperatedClientConnection} instances and updating the underlying
 * {@link java.net.Socket} of those objects. Implementations will most
 * likely make use of {@link org.apache.http.conn.scheme.SchemeSocketFactory}s
 * to create {@link java.net.Socket} instances.
 * <p>
 * The methods in this interface allow the creation of plain and layered
 * sockets. Creating a tunnelled connection through a proxy, however,
 * is not within the scope of the operator.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 *
 * @since 4.0
 *
 * @deprecated (4.3) replaced by {@link HttpClientConnectionManager}.
 */
@Deprecated
public interface ClientConnectionOperator {

    /**
     * Creates a new connection that can be operated.
     *
     * @return  a new, unopened connection for use with this operator
     */
    OperatedClientConnection createConnection();

    /**
     * Opens a connection to the given target host.
     *
     * @param conn      the connection to open
     * @param target    the target host to connect to
     * @param local     the local address to route from, or
     *                  {@code null} for the default
     * @param context   the context for the connection
     * @param params    the parameters for the connection
     *
     * @throws IOException      in case of a problem
     */
    void openConnection(OperatedClientConnection conn,
                        HttpHost target,
                        InetAddress local,
                        HttpContext context,
                        HttpParams params)
        throws IOException;

    /**
     * Updates a connection with a layered secure connection.
     * The typical use of this method is to update a tunnelled plain
     * connection (HTTP) to a secure TLS/SSL connection (HTTPS).
     *
     * @param conn      the open connection to update
     * @param target    the target host for the updated connection.
     *                  The connection must already be open or tunnelled
     *                  to the host and port, but the scheme of the target
     *                  will be used to create a layered connection.
     * @param context   the context for the connection
     * @param params    the parameters for the updated connection
     *
     * @throws IOException      in case of a problem
     */
    void updateSecureConnection(OperatedClientConnection conn,
                                HttpHost target,
                                HttpContext context,
                                HttpParams params)
        throws IOException;

}

