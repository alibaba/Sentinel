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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpClientConnection;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * A connection manager for a single connection. This connection manager maintains only one active
 * connection. Even though this class is fully thread-safe it ought to be used by one execution
 * thread only, as only one thread a time can lease the connection at a time.
 * <p>
 * This connection manager will make an effort to reuse the connection for subsequent requests
 * with the same {@link HttpRoute route}. It will, however, close the existing connection and
 * open it for the given route, if the route of the persistent connection does not match that
 * of the connection request. If the connection has been already been allocated
 * {@link IllegalStateException} is thrown.
 * </p>
 * <p>
 * This connection manager implementation should be used inside an EJB container instead of
 * {@link PoolingClientConnectionManager}.
 * </p>
 *
 * @since 4.2
 *
 * @deprecated (4.3) use {@link BasicHttpClientConnectionManager}.
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public class BasicClientConnectionManager implements ClientConnectionManager {

    private final Log log = LogFactory.getLog(getClass());

    private static final AtomicLong COUNTER = new AtomicLong();

    /** The message to be logged on multiple allocation. */
    public final static String MISUSE_MESSAGE =
    "Invalid use of BasicClientConnManager: connection still allocated.\n" +
    "Make sure to release the connection before allocating another one.";

    /** The schemes supported by this connection manager. */
    private final SchemeRegistry schemeRegistry;

    /** The operator for opening and updating connections. */
    private final ClientConnectionOperator connOperator;

    /** The one and only entry in this pool. */
    private HttpPoolEntry poolEntry;

    /** The currently issued managed connection, if any. */
    private ManagedClientConnectionImpl conn;

    /** Indicates whether this connection manager is shut down. */
    private volatile boolean shutdown;

    /**
     * Creates a new simple connection manager.
     *
     * @param schreg    the scheme registry
     */
    public BasicClientConnectionManager(final SchemeRegistry schreg) {
        Args.notNull(schreg, "Scheme registry");
        this.schemeRegistry = schreg;
        this.connOperator = createConnectionOperator(schreg);
    }

    public BasicClientConnectionManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            shutdown();
        } finally { // Make sure we call overridden method even if shutdown barfs
            super.finalize();
        }
    }

    @Override
    public SchemeRegistry getSchemeRegistry() {
        return this.schemeRegistry;
    }

    protected ClientConnectionOperator createConnectionOperator(final SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }

    @Override
    public final ClientConnectionRequest requestConnection(
            final HttpRoute route,
            final Object state) {

        return new ClientConnectionRequest() {

            @Override
            public void abortRequest() {
                // Nothing to abort, since requests are immediate.
            }

            @Override
            public ManagedClientConnection getConnection(
                    final long timeout, final TimeUnit tunit) {
                return BasicClientConnectionManager.this.getConnection(
                        route, state);
            }

        };
    }

    private void assertNotShutdown() {
        Asserts.check(!this.shutdown, "Connection manager has been shut down");
    }

    ManagedClientConnection getConnection(final HttpRoute route, final Object state) {
        Args.notNull(route, "Route");
        synchronized (this) {
            assertNotShutdown();
            if (this.log.isDebugEnabled()) {
                this.log.debug("Get connection for route " + route);
            }
            Asserts.check(this.conn == null, MISUSE_MESSAGE);
            if (this.poolEntry != null && !this.poolEntry.getPlannedRoute().equals(route)) {
                this.poolEntry.close();
                this.poolEntry = null;
            }
            if (this.poolEntry == null) {
                final String id = Long.toString(COUNTER.getAndIncrement());
                final OperatedClientConnection opconn = this.connOperator.createConnection();
                this.poolEntry = new HttpPoolEntry(this.log, id, route, opconn, 0, TimeUnit.MILLISECONDS);
            }
            final long now = System.currentTimeMillis();
            if (this.poolEntry.isExpired(now)) {
                this.poolEntry.close();
                this.poolEntry.getTracker().reset();
            }
            this.conn = new ManagedClientConnectionImpl(this, this.connOperator, this.poolEntry);
            return this.conn;
        }
    }

    private void shutdownConnection(final HttpClientConnection conn) {
        try {
            conn.shutdown();
        } catch (final IOException iox) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("I/O exception shutting down connection", iox);
            }
        }
    }

    @Override
    public void releaseConnection(final ManagedClientConnection conn, final long keepalive, final TimeUnit tunit) {
        Args.check(conn instanceof ManagedClientConnectionImpl, "Connection class mismatch, " +
            "connection not obtained from this manager");
        final ManagedClientConnectionImpl managedConn = (ManagedClientConnectionImpl) conn;
        synchronized (managedConn) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Releasing connection " + conn);
            }
            if (managedConn.getPoolEntry() == null) {
                return; // already released
            }
            final ClientConnectionManager manager = managedConn.getManager();
            Asserts.check(manager == this, "Connection not obtained from this manager");
            synchronized (this) {
                if (this.shutdown) {
                    shutdownConnection(managedConn);
                    return;
                }
                try {
                    if (managedConn.isOpen() && !managedConn.isMarkedReusable()) {
                        shutdownConnection(managedConn);
                    }
                    if (managedConn.isMarkedReusable()) {
                        this.poolEntry.updateExpiry(keepalive, tunit != null ? tunit : TimeUnit.MILLISECONDS);
                        if (this.log.isDebugEnabled()) {
                            final String s;
                            if (keepalive > 0) {
                                s = "for " + keepalive + " " + tunit;
                            } else {
                                s = "indefinitely";
                            }
                            this.log.debug("Connection can be kept alive " + s);
                        }
                    }
                } finally {
                    managedConn.detach();
                    this.conn = null;
                    if (this.poolEntry.isClosed()) {
                        this.poolEntry = null;
                    }
                }
            }
        }
    }

    @Override
    public void closeExpiredConnections() {
        synchronized (this) {
            assertNotShutdown();
            final long now = System.currentTimeMillis();
            if (this.poolEntry != null && this.poolEntry.isExpired(now)) {
                this.poolEntry.close();
                this.poolEntry.getTracker().reset();
            }
        }
    }

    @Override
    public void closeIdleConnections(final long idletime, final TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        synchronized (this) {
            assertNotShutdown();
            long time = tunit.toMillis(idletime);
            if (time < 0) {
                time = 0;
            }
            final long deadline = System.currentTimeMillis() - time;
            if (this.poolEntry != null && this.poolEntry.getUpdated() <= deadline) {
                this.poolEntry.close();
                this.poolEntry.getTracker().reset();
            }
        }
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            this.shutdown = true;
            try {
                if (this.poolEntry != null) {
                    this.poolEntry.close();
                }
            } finally {
                this.poolEntry = null;
                this.conn = null;
            }
        }
    }

}
