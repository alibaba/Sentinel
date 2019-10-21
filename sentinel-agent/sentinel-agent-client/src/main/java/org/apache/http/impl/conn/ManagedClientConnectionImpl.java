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
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * @since 4.2
 *
 * @deprecated (4.3) use {@link ManagedHttpClientConnectionFactory}.
 */
@Deprecated
class ManagedClientConnectionImpl implements ManagedClientConnection {

    private final ClientConnectionManager manager;
    private final ClientConnectionOperator operator;
    private volatile HttpPoolEntry poolEntry;
    private volatile boolean reusable;
    private volatile long duration;

    ManagedClientConnectionImpl(
            final ClientConnectionManager manager,
            final ClientConnectionOperator operator,
            final HttpPoolEntry entry) {
        super();
        Args.notNull(manager, "Connection manager");
        Args.notNull(operator, "Connection operator");
        Args.notNull(entry, "HTTP pool entry");
        this.manager = manager;
        this.operator = operator;
        this.poolEntry = entry;
        this.reusable = false;
        this.duration = Long.MAX_VALUE;
    }

    @Override
    public String getId() {
        return null;
    }

    HttpPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    HttpPoolEntry detach() {
        final HttpPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    public ClientConnectionManager getManager() {
        return this.manager;
    }

    private OperatedClientConnection getConnection() {
        final HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        return local.getConnection();
    }

    private OperatedClientConnection ensureConnection() {
        final HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            throw new ConnectionShutdownException();
        }
        return local.getConnection();
    }

    private HttpPoolEntry ensurePoolEntry() {
        final HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            throw new ConnectionShutdownException();
        }
        return local;
    }

    @Override
    public void close() throws IOException {
        final HttpPoolEntry local = this.poolEntry;
        if (local != null) {
            final OperatedClientConnection conn = local.getConnection();
            local.getTracker().reset();
            conn.close();
        }
    }

    @Override
    public void shutdown() throws IOException {
        final HttpPoolEntry local = this.poolEntry;
        if (local != null) {
            final OperatedClientConnection conn = local.getConnection();
            local.getTracker().reset();
            conn.shutdown();
        }
    }

    @Override
    public boolean isOpen() {
        final OperatedClientConnection conn = getConnection();
        if (conn != null) {
            return conn.isOpen();
        } else {
            return false;
        }
    }

    @Override
    public boolean isStale() {
        final OperatedClientConnection conn = getConnection();
        if (conn != null) {
            return conn.isStale();
        } else {
            return true;
        }
    }

    @Override
    public void setSocketTimeout(final int timeout) {
        final OperatedClientConnection conn = ensureConnection();
        conn.setSocketTimeout(timeout);
    }

    @Override
    public int getSocketTimeout() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getSocketTimeout();
    }

    @Override
    public HttpConnectionMetrics getMetrics() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getMetrics();
    }

    @Override
    public void flush() throws IOException {
        final OperatedClientConnection conn = ensureConnection();
        conn.flush();
    }

    @Override
    public boolean isResponseAvailable(final int timeout) throws IOException {
        final OperatedClientConnection conn = ensureConnection();
        return conn.isResponseAvailable(timeout);
    }

    @Override
    public void receiveResponseEntity(
            final HttpResponse response) throws HttpException, IOException {
        final OperatedClientConnection conn = ensureConnection();
        conn.receiveResponseEntity(response);
    }

    @Override
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        final OperatedClientConnection conn = ensureConnection();
        return conn.receiveResponseHeader();
    }

    @Override
    public void sendRequestEntity(
            final HttpEntityEnclosingRequest request) throws HttpException, IOException {
        final OperatedClientConnection conn = ensureConnection();
        conn.sendRequestEntity(request);
    }

    @Override
    public void sendRequestHeader(
            final HttpRequest request) throws HttpException, IOException {
        final OperatedClientConnection conn = ensureConnection();
        conn.sendRequestHeader(request);
    }

    @Override
    public InetAddress getLocalAddress() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getLocalPort();
    }

    @Override
    public InetAddress getRemoteAddress() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getRemotePort();
    }

    @Override
    public boolean isSecure() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.isSecure();
    }

    @Override
    public void bind(final Socket socket) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket getSocket() {
        final OperatedClientConnection conn = ensureConnection();
        return conn.getSocket();
    }

    @Override
    public SSLSession getSSLSession() {
        final OperatedClientConnection conn = ensureConnection();
        SSLSession result = null;
        final Socket sock = conn.getSocket();
        if (sock instanceof SSLSocket) {
            result = ((SSLSocket)sock).getSession();
        }
        return result;
    }

    public Object getAttribute(final String id) {
        final OperatedClientConnection conn = ensureConnection();
        if (conn instanceof HttpContext) {
            return ((HttpContext) conn).getAttribute(id);
        } else {
            return null;
        }
    }

    public Object removeAttribute(final String id) {
        final OperatedClientConnection conn = ensureConnection();
        if (conn instanceof HttpContext) {
            return ((HttpContext) conn).removeAttribute(id);
        } else {
            return null;
        }
    }

    public void setAttribute(final String id, final Object obj) {
        final OperatedClientConnection conn = ensureConnection();
        if (conn instanceof HttpContext) {
            ((HttpContext) conn).setAttribute(id, obj);
        }
    }

    @Override
    public HttpRoute getRoute() {
        final HttpPoolEntry local = ensurePoolEntry();
        return local.getEffectiveRoute();
    }

    @Override
    public void open(
            final HttpRoute route,
            final HttpContext context,
            final HttpParams params) throws IOException {
        Args.notNull(route, "Route");
        Args.notNull(params, "HTTP parameters");
        final OperatedClientConnection conn;
        synchronized (this) {
            if (this.poolEntry == null) {
                throw new ConnectionShutdownException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            Asserts.notNull(tracker, "Route tracker");
            Asserts.check(!tracker.isConnected(), "Connection already open");
            conn = this.poolEntry.getConnection();
        }

        final HttpHost proxy  = route.getProxyHost();
        this.operator.openConnection(
                conn,
                (proxy != null) ? proxy : route.getTargetHost(),
                route.getLocalAddress(),
                context, params);

        synchronized (this) {
            if (this.poolEntry == null) {
                throw new InterruptedIOException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            if (proxy == null) {
                tracker.connectTarget(conn.isSecure());
            } else {
                tracker.connectProxy(proxy, conn.isSecure());
            }
        }
    }

    @Override
    public void tunnelTarget(
            final boolean secure, final HttpParams params) throws IOException {
        Args.notNull(params, "HTTP parameters");
        final HttpHost target;
        final OperatedClientConnection conn;
        synchronized (this) {
            if (this.poolEntry == null) {
                throw new ConnectionShutdownException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            Asserts.notNull(tracker, "Route tracker");
            Asserts.check(tracker.isConnected(), "Connection not open");
            Asserts.check(!tracker.isTunnelled(), "Connection is already tunnelled");
            target = tracker.getTargetHost();
            conn = this.poolEntry.getConnection();
        }

        conn.update(null, target, secure, params);

        synchronized (this) {
            if (this.poolEntry == null) {
                throw new InterruptedIOException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            tracker.tunnelTarget(secure);
        }
    }

    @Override
    public void tunnelProxy(
            final HttpHost next, final boolean secure, final HttpParams params) throws IOException {
        Args.notNull(next, "Next proxy");
        Args.notNull(params, "HTTP parameters");
        final OperatedClientConnection conn;
        synchronized (this) {
            if (this.poolEntry == null) {
                throw new ConnectionShutdownException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            Asserts.notNull(tracker, "Route tracker");
            Asserts.check(tracker.isConnected(), "Connection not open");
            conn = this.poolEntry.getConnection();
        }

        conn.update(null, next, secure, params);

        synchronized (this) {
            if (this.poolEntry == null) {
                throw new InterruptedIOException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            tracker.tunnelProxy(next, secure);
        }
    }

    @Override
    public void layerProtocol(
            final HttpContext context, final HttpParams params) throws IOException {
        Args.notNull(params, "HTTP parameters");
        final HttpHost target;
        final OperatedClientConnection conn;
        synchronized (this) {
            if (this.poolEntry == null) {
                throw new ConnectionShutdownException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            Asserts.notNull(tracker, "Route tracker");
            Asserts.check(tracker.isConnected(), "Connection not open");
            Asserts.check(tracker.isTunnelled(), "Protocol layering without a tunnel not supported");
            Asserts.check(!tracker.isLayered(), "Multiple protocol layering not supported");
            target = tracker.getTargetHost();
            conn = this.poolEntry.getConnection();
        }
        this.operator.updateSecureConnection(conn, target, context, params);

        synchronized (this) {
            if (this.poolEntry == null) {
                throw new InterruptedIOException();
            }
            final RouteTracker tracker = this.poolEntry.getTracker();
            tracker.layerProtocol(conn.isSecure());
        }
    }

    @Override
    public Object getState() {
        final HttpPoolEntry local = ensurePoolEntry();
        return local.getState();
    }

    @Override
    public void setState(final Object state) {
        final HttpPoolEntry local = ensurePoolEntry();
        local.setState(state);
    }

    @Override
    public void markReusable() {
        this.reusable = true;
    }

    @Override
    public void unmarkReusable() {
        this.reusable = false;
    }

    @Override
    public boolean isMarkedReusable() {
        return this.reusable;
    }

    @Override
    public void setIdleDuration(final long duration, final TimeUnit unit) {
        if(duration > 0) {
            this.duration = unit.toMillis(duration);
        } else {
            this.duration = -1;
        }
    }

    @Override
    public void releaseConnection() {
        synchronized (this) {
            if (this.poolEntry == null) {
                return;
            }
            this.manager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
            this.poolEntry = null;
        }
    }

    @Override
    public void abortConnection() {
        synchronized (this) {
            if (this.poolEntry == null) {
                return;
            }
            this.reusable = false;
            final OperatedClientConnection conn = this.poolEntry.getConnection();
            try {
                conn.shutdown();
            } catch (final IOException ignore) {
            }
            this.manager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
            this.poolEntry = null;
        }
    }

}
