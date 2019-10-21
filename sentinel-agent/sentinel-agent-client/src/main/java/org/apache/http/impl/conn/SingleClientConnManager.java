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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * A connection manager for a single connection. This connection manager
 * maintains only one active connection at a time. Even though this class
 * is thread-safe it ought to be used by one execution thread only.
 * <p>
 * SingleClientConnManager will make an effort to reuse the connection
 * for subsequent requests with the same {@link HttpRoute route}.
 * It will, however, close the existing connection and open it
 * for the given route, if the route of the persistent connection does
 * not match that of the connection request. If the connection has been
 * already been allocated {@link IllegalStateException} is thrown.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  use {@link BasicClientConnectionManager}
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public class SingleClientConnManager implements ClientConnectionManager {

    private final Log log = LogFactory.getLog(getClass());

    /** The message to be logged on multiple allocation. */
    public final static String MISUSE_MESSAGE =
    "Invalid use of SingleClientConnManager: connection still allocated.\n" +
    "Make sure to release the connection before allocating another one.";

    /** The schemes supported by this connection manager. */
    protected final SchemeRegistry schemeRegistry;

    /** The operator for opening and updating connections. */
    protected final ClientConnectionOperator connOperator;

    /** Whether the connection should be shut down  on release. */
    protected final boolean alwaysShutDown;

    /** The one and only entry in this pool. */
    protected volatile PoolEntry uniquePoolEntry;

    /** The currently issued managed connection, if any. */
    protected volatile ConnAdapter managedConn;

    /** The time of the last connection release, or -1. */
    protected volatile long lastReleaseTime;

    /** The time the last released connection expires and shouldn't be reused. */
    protected volatile long connectionExpiresTime;

    /** Indicates whether this connection manager is shut down. */
    protected volatile boolean isShutDown;

    /**
     * Creates a new simple connection manager.
     *
     * @param params    the parameters for this manager
     * @param schreg    the scheme registry
     *
     * @deprecated (4.1)  use {@link SingleClientConnManager#SingleClientConnManager(SchemeRegistry)}
     */
    @Deprecated
    public SingleClientConnManager(final HttpParams params,
                                   final SchemeRegistry schreg) {
        this(schreg);
    }
    /**
     * Creates a new simple connection manager.
     *
     * @param schreg    the scheme registry
     */
    public SingleClientConnManager(final SchemeRegistry schreg) {
        Args.notNull(schreg, "Scheme registry");
        this.schemeRegistry  = schreg;
        this.connOperator    = createConnectionOperator(schreg);
        this.uniquePoolEntry = new PoolEntry();
        this.managedConn     = null;
        this.lastReleaseTime = -1L;
        this.alwaysShutDown  = false; //@@@ from params? as argument?
        this.isShutDown      = false;
    }

    /**
     * @since 4.1
     */
    public SingleClientConnManager() {
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

    /**
     * Hook for creating the connection operator.
     * It is called by the constructor.
     * Derived classes can override this method to change the
     * instantiation of the operator.
     * The default implementation here instantiates
     * {@link DefaultClientConnectionOperator DefaultClientConnectionOperator}.
     *
     * @param schreg    the scheme registry to use, or {@code null}
     *
     * @return  the connection operator to use
     */
    protected ClientConnectionOperator
        createConnectionOperator(final SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }

    /**
     * Asserts that this manager is not shut down.
     *
     * @throws IllegalStateException    if this manager is shut down
     */
    protected final void assertStillUp() throws IllegalStateException {
        Asserts.check(!this.isShutDown, "Manager is shut down");
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
                return SingleClientConnManager.this.getConnection(
                        route, state);
            }

        };
    }

    /**
     * Obtains a connection.
     *
     * @param route     where the connection should point to
     *
     * @return  a connection that can be used to communicate
     *          along the given route
     */
    public ManagedClientConnection getConnection(final HttpRoute route, final Object state) {
        Args.notNull(route, "Route");
        assertStillUp();

        if (log.isDebugEnabled()) {
            log.debug("Get connection for route " + route);
        }

        synchronized (this) {

            Asserts.check(managedConn == null, MISUSE_MESSAGE);

            // check re-usability of the connection
            boolean recreate = false;
            boolean shutdown = false;

            // Kill the connection if it expired.
            closeExpiredConnections();

            if (uniquePoolEntry.connection.isOpen()) {
                final RouteTracker tracker = uniquePoolEntry.tracker;
                shutdown = (tracker == null || // can happen if method is aborted
                            !tracker.toRoute().equals(route));
            } else {
                // If the connection is not open, create a new PoolEntry,
                // as the connection may have been marked not reusable,
                // due to aborts -- and the PoolEntry should not be reused
                // either.  There's no harm in recreating an entry if
                // the connection is closed.
                recreate = true;
            }

            if (shutdown) {
                recreate = true;
                try {
                    uniquePoolEntry.shutdown();
                } catch (final IOException iox) {
                    log.debug("Problem shutting down connection.", iox);
                }
            }

            if (recreate) {
                uniquePoolEntry = new PoolEntry();
            }

            managedConn = new ConnAdapter(uniquePoolEntry, route);

            return managedConn;
        }
    }

    @Override
    public void releaseConnection(
            final ManagedClientConnection conn,
            final long validDuration, final TimeUnit timeUnit) {
        Args.check(conn instanceof ConnAdapter, "Connection class mismatch, " +
            "connection not obtained from this manager");
        assertStillUp();

        if (log.isDebugEnabled()) {
            log.debug("Releasing connection " + conn);
        }

        final ConnAdapter sca = (ConnAdapter) conn;
        synchronized (sca) {
            if (sca.poolEntry == null)
             {
                return; // already released
            }
            final ClientConnectionManager manager = sca.getManager();
            Asserts.check(manager == this, "Connection not obtained from this manager");
            try {
                // make sure that the response has been read completely
                if (sca.isOpen() && (this.alwaysShutDown ||
                                     !sca.isMarkedReusable())
                    ) {
                    if (log.isDebugEnabled()) {
                        log.debug
                            ("Released connection open but not reusable.");
                    }

                    // make sure this connection will not be re-used
                    // we might have gotten here because of a shutdown trigger
                    // shutdown of the adapter also clears the tracked route
                    sca.shutdown();
                }
            } catch (final IOException iox) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception shutting down released connection.",
                              iox);
                }
            } finally {
                sca.detach();
                synchronized (this) {
                    managedConn = null;
                    lastReleaseTime = System.currentTimeMillis();
                    if(validDuration > 0) {
                        connectionExpiresTime = timeUnit.toMillis(validDuration) + lastReleaseTime;
                    } else {
                        connectionExpiresTime = Long.MAX_VALUE;
                    }
                }
            }
        }
    }

    @Override
    public void closeExpiredConnections() {
        final long time = connectionExpiresTime;
        if (System.currentTimeMillis() >= time) {
            closeIdleConnections(0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void closeIdleConnections(final long idletime, final TimeUnit tunit) {
        assertStillUp();

        // idletime can be 0 or negative, no problem there
        Args.notNull(tunit, "Time unit");

        synchronized (this) {
            if ((managedConn == null) && uniquePoolEntry.connection.isOpen()) {
                final long cutoff =
                    System.currentTimeMillis() - tunit.toMillis(idletime);
                if (lastReleaseTime <= cutoff) {
                    try {
                        uniquePoolEntry.close();
                    } catch (final IOException iox) {
                        // ignore
                        log.debug("Problem closing idle connection.", iox);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
        this.isShutDown = true;
        synchronized (this) {
            try {
                if (uniquePoolEntry != null) {
                    uniquePoolEntry.shutdown();
                }
            } catch (final IOException iox) {
                // ignore
                log.debug("Problem while shutting down manager.", iox);
            } finally {
                uniquePoolEntry = null;
                managedConn = null;
            }
        }
    }

    protected void revokeConnection() {
        final ConnAdapter conn = managedConn;
        if (conn == null) {
            return;
        }
        conn.detach();

        synchronized (this) {
            try {
                uniquePoolEntry.shutdown();
            } catch (final IOException iox) {
                // ignore
                log.debug("Problem while shutting down connection.", iox);
            }
        }
    }

    /**
     * The pool entry for this connection manager.
     */
    protected class PoolEntry extends AbstractPoolEntry {

        /**
         * Creates a new pool entry.
         *
         */
        protected PoolEntry() {
            super(SingleClientConnManager.this.connOperator, null);
        }

        /**
         * Closes the connection in this pool entry.
         */
        protected void close() throws IOException {
            shutdownEntry();
            if (connection.isOpen()) {
                connection.close();
            }
        }

        /**
         * Shuts down the connection in this pool entry.
         */
        protected void shutdown() throws IOException {
            shutdownEntry();
            if (connection.isOpen()) {
                connection.shutdown();
            }
        }

    }

    /**
     * The connection adapter used by this manager.
     */
    protected class ConnAdapter extends AbstractPooledConnAdapter {

        /**
         * Creates a new connection adapter.
         *
         * @param entry   the pool entry for the connection being wrapped
         * @param route   the planned route for this connection
         */
        protected ConnAdapter(final PoolEntry entry, final HttpRoute route) {
            super(SingleClientConnManager.this, entry);
            markReusable();
            entry.route = route;
        }

    }

}
