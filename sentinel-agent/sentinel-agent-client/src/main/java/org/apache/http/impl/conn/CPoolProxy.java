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
package org.apache.http.impl.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;

/**
 * @since 4.3
 */
class CPoolProxy implements ManagedHttpClientConnection, HttpContext {

    private volatile CPoolEntry poolEntry;

    CPoolProxy(final CPoolEntry entry) {
        super();
        this.poolEntry = entry;
    }

    CPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    CPoolEntry detach() {
        final CPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    ManagedHttpClientConnection getConnection() {
        final CPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        return local.getConnection();
    }

    ManagedHttpClientConnection getValidConnection() {
        final ManagedHttpClientConnection conn = getConnection();
        if (conn == null) {
            throw new ConnectionShutdownException();
        }
        return conn;
    }

    @Override
    public void close() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.closeConnection();
        }
    }

    @Override
    public void shutdown() throws IOException {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.shutdownConnection();
        }
    }

    @Override
    public boolean isOpen() {
        final CPoolEntry local = this.poolEntry;
        if (local != null) {
            return !local.isClosed();
        } else {
            return false;
        }
    }

    @Override
    public boolean isStale() {
        final HttpClientConnection conn = getConnection();
        if (conn != null) {
            return conn.isStale();
        } else {
            return true;
        }
    }

    @Override
    public void setSocketTimeout(final int timeout) {
        getValidConnection().setSocketTimeout(timeout);
    }

    @Override
    public int getSocketTimeout() {
        return getValidConnection().getSocketTimeout();
    }

    @Override
    public String getId() {
        return getValidConnection().getId();
    }

    @Override
    public void bind(final Socket socket) throws IOException {
        getValidConnection().bind(socket);
    }

    @Override
    public Socket getSocket() {
        return getValidConnection().getSocket();
    }

    @Override
    public SSLSession getSSLSession() {
        return getValidConnection().getSSLSession();
    }

    @Override
    public boolean isResponseAvailable(final int timeout) throws IOException {
        return getValidConnection().isResponseAvailable(timeout);
    }

    @Override
    public void sendRequestHeader(final HttpRequest request) throws HttpException, IOException {
        getValidConnection().sendRequestHeader(request);
    }

    @Override
    public void sendRequestEntity(final HttpEntityEnclosingRequest request) throws HttpException, IOException {
        getValidConnection().sendRequestEntity(request);
    }

    @Override
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        return getValidConnection().receiveResponseHeader();
    }

    @Override
    public void receiveResponseEntity(final HttpResponse response) throws HttpException, IOException {
        getValidConnection().receiveResponseEntity(response);
    }

    @Override
    public void flush() throws IOException {
        getValidConnection().flush();
    }

    @Override
    public HttpConnectionMetrics getMetrics() {
        return getValidConnection().getMetrics();
    }

    @Override
    public InetAddress getLocalAddress() {
        return getValidConnection().getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return getValidConnection().getLocalPort();
    }

    @Override
    public InetAddress getRemoteAddress() {
        return getValidConnection().getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return getValidConnection().getRemotePort();
    }

    @Override
    public Object getAttribute(final String id) {
        final ManagedHttpClientConnection conn = getValidConnection();
        if (conn instanceof HttpContext) {
            return ((HttpContext) conn).getAttribute(id);
        } else {
            return null;
        }
    }

    @Override
    public void setAttribute(final String id, final Object obj) {
        final ManagedHttpClientConnection conn = getValidConnection();
        if (conn instanceof HttpContext) {
            ((HttpContext) conn).setAttribute(id, obj);
        }
    }

    @Override
    public Object removeAttribute(final String id) {
        final ManagedHttpClientConnection conn = getValidConnection();
        if (conn instanceof HttpContext) {
            return ((HttpContext) conn).removeAttribute(id);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CPoolProxy{");
        final ManagedHttpClientConnection conn = getConnection();
        if (conn != null) {
            sb.append(conn);
        } else {
            sb.append("detached");
        }
        sb.append('}');
        return sb.toString();
    }

    public static HttpClientConnection newProxy(final CPoolEntry poolEntry) {
        return new CPoolProxy(poolEntry);
    }

    private static CPoolProxy getProxy(final HttpClientConnection conn) {
        if (!CPoolProxy.class.isInstance(conn)) {
            throw new IllegalStateException("Unexpected connection proxy class: " + conn.getClass());
        }
        return CPoolProxy.class.cast(conn);
    }

    public static CPoolEntry getPoolEntry(final HttpClientConnection proxy) {
        final CPoolEntry entry = getProxy(proxy).getPoolEntry();
        if (entry == null) {
            throw new ConnectionShutdownException();
        }
        return entry;
    }

    public static CPoolEntry detach(final HttpClientConnection conn) {
        return getProxy(conn).detach();
    }

}
