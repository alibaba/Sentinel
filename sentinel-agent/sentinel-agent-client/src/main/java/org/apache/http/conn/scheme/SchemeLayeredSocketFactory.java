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
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.params.HttpParams;

/**
 * Extended {@link SchemeSocketFactory} interface for layered sockets such as SSL/TLS.
 *
 * @since 4.2
 *
 * @deprecated (4.3) use {@link
 *   org.apache.http.conn.socket.LayeredConnectionSocketFactory}
 */
@Deprecated
public interface SchemeLayeredSocketFactory extends SchemeSocketFactory {

    /**
     * Returns a socket connected to the given host that is layered over an
     * existing socket.  Used primarily for creating secure sockets through
     * proxies.
     *
     * @param socket the existing socket
     * @param target    the name of the target host.
     * @param port      the port to connect to on the target host
     * @param params    HTTP parameters
     *
     * @return Socket a new socket
     *
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    Socket createLayeredSocket(
        Socket socket,
        String target,
        int port,
        HttpParams params) throws IOException, UnknownHostException;

}
