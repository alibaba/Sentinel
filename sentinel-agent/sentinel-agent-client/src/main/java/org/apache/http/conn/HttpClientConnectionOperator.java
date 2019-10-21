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
import java.net.InetSocketAddress;

import org.apache.http.HttpHost;
import org.apache.http.config.SocketConfig;
import org.apache.http.protocol.HttpContext;

/**
 * Connection operator that performs connection connect and upgrade operations. Usually, components
 * participating in these operations are registry of {@link org.apache.http.conn.socket.ConnectionSocketFactory},
 * {@link SchemePortResolver} and {@link DnsResolver}. In general, HTTP client user should not
 * provide implementations of this interface, as HttpClient will use the default one that covers
 * most of the cases needed for majority of users.
 *
 * @since 4.4
 */
public interface HttpClientConnectionOperator {

    void connect(
            ManagedHttpClientConnection conn,
            HttpHost host,
            InetSocketAddress localAddress,
            int connectTimeout,
            SocketConfig socketConfig,
            HttpContext context) throws IOException;

    void upgrade(
            ManagedHttpClientConnection conn,
            HttpHost host,
            HttpContext context) throws IOException;

}
