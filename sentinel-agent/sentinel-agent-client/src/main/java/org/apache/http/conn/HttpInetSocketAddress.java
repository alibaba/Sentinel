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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.http.HttpHost;
import org.apache.http.util.Args;

/**
 * Extended {@link InetSocketAddress} implementation that also provides access to the original
 * {@link HttpHost} used to resolve the address.
 *
 * @since 4.2 no longer used.
 *
 * @deprecated (4.3)
 */
@Deprecated
public class HttpInetSocketAddress extends InetSocketAddress {

    private static final long serialVersionUID = -6650701828361907957L;

    private final HttpHost httphost;

    public HttpInetSocketAddress(final HttpHost httphost, final InetAddress addr, final int port) {
        super(addr, port);
        Args.notNull(httphost, "HTTP host");
        this.httphost = httphost;
    }

    public HttpHost getHttpHost() {
        return this.httphost;
    }

    @Override
    public String toString() {
        return this.httphost.getHostName() + ":" + getPort();
    }

}
