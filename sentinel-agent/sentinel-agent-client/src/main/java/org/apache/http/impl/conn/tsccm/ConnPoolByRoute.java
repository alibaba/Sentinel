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
package org.apache.http.impl.conn.tsccm;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * A connection pool that maintains connections by route.
 * This class is derived from {@code MultiThreadedHttpConnectionManager}
 * in HttpClient 3.x, see there for original authors. It implements the same
 * algorithm for connection re-use and connection-per-host enforcement:
 * <ul>
 * <li>connections are re-used only for the exact same route</li>
 * <li>connection limits are enforced per route rather than per host</li>
 * </ul>
 * Note that access to the pool data structures is synchronized via the
 * {@link AbstractConnPool#poolLock poolLock} in the base class,
 * not via {@code synchronized} methods.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  use {@link org.apache.http.pool.AbstractConnPool}
 */
@Deprecated
public class ConnPoolByRoute extends AbstractConnPool {

    private final Log log = LogFactory.getLog(getClass());

    private final Lock poolLock;

    /** Connection operator for this pool */
    protected final ClientConnectionOperator operator;

    /** Connections per route lookup */
    protected final ConnPerRoute connPerRoute;

    /** References to issued connections */
    protected final Set<BasicPoolEntry> leasedConnections;

    /** The list of free connections */
    protected final Queue<BasicPoolEntry> freeConnections;

    /** The list of WaitingThreads waiting for a connection */
    protected final Queue<WaitingThread> waitingThreads;

    /** Map of route-specific pools */
    protected final Map<HttpRoute, RouteSpecificPool> routeToPool;

    private final long connTTL;

    private final TimeUnit connTTLTimeUnit;

    protected volatile boolean shutdown;

    protected volatile int maxTotalConnections;

    protected volatile int numConnections;

    /**
     * Creates a new connection pool, managed by route.
     *
     * @since 4.1
     */
    public ConnPoolByRoute(
            final ClientConnectionOperator operator,
            final ConnPerRoute connPerRoute,
            final int maxTotalConnections) {
        this(operator, connPerRoute, maxTotalConnections, -1, TimeUnit.MILLISECONDS);
    }

    /**
     * @since 4.1
     */
    public ConnPoolByRoute(
            final ClientConnectionOperator operator,
            final ConnPerRoute connPerRoute,
            final int maxTotalConnections,
            final long connTTL,
            final TimeUnit connTTLTimeUnit) {
        super();
        Args.notNull(operator, "Connection operator");
        Args.notNull(connPerRoute, "Connections per route");
        this.poolLock = super.poolLock;
        this.leasedConnections = super.leasedConnections;
        this.operator = operator;
        this.connPerRoute = connPerRoute;
        this.maxTotalConnections = maxTotalConnections;
        this.freeConnections = createFreeConnQueue();
        this.waitingThreads  = createWaitingThreadQueue();
        this.routeToPool     = createRouteToPoolMap();
        this.connTTL = connTTL;
        this.connTTLTimeUnit = connTTLTimeUnit;
    }

    protected Lock getLock() {
        return this.poolLock;
    }

    /**
     * Creates a new connection pool, managed by route.
     *
     * @deprecated (4.1)  use {@link ConnPoolByRoute#ConnPoolByRoute(ClientConnectionOperator, ConnPerRoute, int)}
     */
    @Deprecated
    public ConnPoolByRoute(final ClientConnectionOperator operator, final HttpParams params) {
        this(operator,
                ConnManagerParams.getMaxConnectionsPerRoute(params),
                ConnManagerParams.getMaxTotalConnections(params));
    }

    /**
     * Creates the queue for {@link #freeConnections}.
     * Called once by the constructor.
     *
     * @return  a queue
     */
    protected Queue<BasicPoolEntry> createFreeConnQueue() {
        return new LinkedList<BasicPoolEntry>();
    }

    /**
     * Creates the queue for {@link #waitingThreads}.
     * Called once by the constructor.
     *
     * @return  a queue
     */
    protected Queue<WaitingThread> createWaitingThreadQueue() {
        return new LinkedList<WaitingThread>();
    }

    /**
     * Creates the map for {@link #routeToPool}.
     * Called once by the constructor.
     *
     * @return  a map
     */
    protected Map<HttpRoute, RouteSpecificPool> createRouteToPoolMap() {
        return new HashMap<HttpRoute, RouteSpecificPool>();
    }


    /**
     * Creates a new route-specific pool.
     * Called by {@link #getRoutePool} when necessary.
     *
     * @param route     the route
     *
     * @return  the new pool
     */
    protected RouteSpecificPool newRouteSpecificPool(final HttpRoute route) {
        return new RouteSpecificPool(route, this.connPerRoute);
    }


    /**
     * Creates a new waiting thread.
     * Called by {@link #getRoutePool} when necessary.
     *
     * @param cond      the condition to wait for
     * @param rospl     the route specific pool, or {@code null}
     *
     * @return  a waiting thread representation
     */
    protected WaitingThread newWaitingThread(final Condition cond,
                                             final RouteSpecificPool rospl) {
        return new WaitingThread(cond, rospl);
    }

    private void closeConnection(final BasicPoolEntry entry) {
        final OperatedClientConnection conn = entry.getConnection();
        if (conn != null) {
            try {
                conn.close();
            } catch (final IOException ex) {
                log.debug("I/O error closing connection", ex);
            }
        }
    }

    /**
     * Get a route-specific pool of available connections.
     *
     * @param route   the route
     * @param create    whether to create the pool if it doesn't exist
     *
     * @return  the pool for the argument route,
     *     never {@code null} if {@code create} is {@code true}
     */
    protected RouteSpecificPool getRoutePool(final HttpRoute route,
                                             final boolean create) {
        RouteSpecificPool rospl = null;
        poolLock.lock();
        try {

            rospl = routeToPool.get(route);
            if ((rospl == null) && create) {
                // no pool for this route yet (or anymore)
                rospl = newRouteSpecificPool(route);
                routeToPool.put(route, rospl);
            }

        } finally {
            poolLock.unlock();
        }

        return rospl;
    }

    public int getConnectionsInPool(final HttpRoute route) {
        poolLock.lock();
        try {
            // don't allow a pool to be created here!
            final RouteSpecificPool rospl = getRoutePool(route, false);
            return (rospl != null) ? rospl.getEntryCount() : 0;

        } finally {
            poolLock.unlock();
        }
    }

    public int getConnectionsInPool() {
        poolLock.lock();
        try {
            return numConnections;
        } finally {
            poolLock.unlock();
        }
    }

    @Override
    public PoolEntryRequest requestPoolEntry(
            final HttpRoute route,
            final Object state) {

        final WaitingThreadAborter aborter = new WaitingThreadAborter();

        return new PoolEntryRequest() {

            @Override
            public void abortRequest() {
                poolLock.lock();
                try {
                    aborter.abort();
                } finally {
                    poolLock.unlock();
                }
            }

            @Override
            public BasicPoolEntry getPoolEntry(
                    final long timeout,
                    final TimeUnit tunit)
                        throws InterruptedException, ConnectionPoolTimeoutException {
                return getEntryBlocking(route, state, timeout, tunit, aborter);
            }

        };
    }

    /**
     * Obtains a pool entry with a connection within the given timeout.
     * If a {@link WaitingThread} is used to block, {@link WaitingThreadAborter#setWaitingThread(WaitingThread)}
     * must be called before blocking, to allow the thread to be interrupted.
     *
     * @param route     the route for which to get the connection
     * @param timeout   the timeout, 0 or negative for no timeout
     * @param tunit     the unit for the {@code timeout},
     *                  may be {@code null} only if there is no timeout
     * @param aborter   an object which can abort a {@link WaitingThread}.
     *
     * @return  pool entry holding a connection for the route
     *
     * @throws ConnectionPoolTimeoutException
     *         if the timeout expired
     * @throws InterruptedException
     *         if the calling thread was interrupted
     */
    protected BasicPoolEntry getEntryBlocking(
                                   final HttpRoute route, final Object state,
                                   final long timeout, final TimeUnit tunit,
                                   final WaitingThreadAborter aborter)
        throws ConnectionPoolTimeoutException, InterruptedException {

        Date deadline = null;
        if (timeout > 0) {
            deadline = new Date
                (System.currentTimeMillis() + tunit.toMillis(timeout));
        }

        BasicPoolEntry entry = null;
        poolLock.lock();
        try {

            RouteSpecificPool rospl = getRoutePool(route, true);
            WaitingThread waitingThread = null;

            while (entry == null) {
                Asserts.check(!shutdown, "Connection pool shut down");

                if (log.isDebugEnabled()) {
                    log.debug("[" + route + "] total kept alive: " + freeConnections.size() +
                            ", total issued: " + leasedConnections.size() +
                            ", total allocated: " + numConnections + " out of " + maxTotalConnections);
                }

                // the cases to check for:
                // - have a free connection for that route
                // - allowed to create a free connection for that route
                // - can delete and replace a free connection for another route
                // - need to wait for one of the things above to come true

                entry = getFreeEntry(rospl, state);
                if (entry != null) {
                    break;
                }

                final boolean hasCapacity = rospl.getCapacity() > 0;

                if (log.isDebugEnabled()) {
                    log.debug("Available capacity: " + rospl.getCapacity()
                            + " out of " + rospl.getMaxEntries()
                            + " [" + route + "][" + state + "]");
                }

                if (hasCapacity && numConnections < maxTotalConnections) {

                    entry = createEntry(rospl, operator);

                } else if (hasCapacity && !freeConnections.isEmpty()) {

                    deleteLeastUsedEntry();
                    // if least used entry's route was the same as rospl,
                    // rospl is now out of date : we preemptively refresh
                    rospl = getRoutePool(route, true);
                    entry = createEntry(rospl, operator);

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Need to wait for connection" +
                                " [" + route + "][" + state + "]");
                    }

                    if (waitingThread == null) {
                        waitingThread =
                            newWaitingThread(poolLock.newCondition(), rospl);
                        aborter.setWaitingThread(waitingThread);
                    }

                    boolean success = false;
                    try {
                        rospl.queueThread(waitingThread);
                        waitingThreads.add(waitingThread);
                        success = waitingThread.await(deadline);

                    } finally {
                        // In case of 'success', we were woken up by the
                        // connection pool and should now have a connection
                        // waiting for us, or else we're shutting down.
                        // Just continue in the loop, both cases are checked.
                        rospl.removeThread(waitingThread);
                        waitingThreads.remove(waitingThread);
                    }

                    // check for spurious wakeup vs. timeout
                    if (!success && (deadline != null) &&
                        (deadline.getTime() <= System.currentTimeMillis())) {
                        throw new ConnectionPoolTimeoutException
                            ("Timeout waiting for connection from pool");
                    }
                }
            } // while no entry

        } finally {
            poolLock.unlock();
        }
        return entry;
    }

    @Override
    public void freeEntry(final BasicPoolEntry entry, final boolean reusable, final long validDuration, final TimeUnit timeUnit) {

        final HttpRoute route = entry.getPlannedRoute();
        if (log.isDebugEnabled()) {
            log.debug("Releasing connection" +
                    " [" + route + "][" + entry.getState() + "]");
        }

        poolLock.lock();
        try {
            if (shutdown) {
                // the pool is shut down, release the
                // connection's resources and get out of here
                closeConnection(entry);
                return;
            }

            // no longer issued, we keep a hard reference now
            leasedConnections.remove(entry);

            final RouteSpecificPool rospl = getRoutePool(route, true);

            if (reusable && rospl.getCapacity() >= 0) {
                if (log.isDebugEnabled()) {
                    final String s;
                    if (validDuration > 0) {
                        s = "for " + validDuration + " " + timeUnit;
                    } else {
                        s = "indefinitely";
                    }
                    log.debug("Pooling connection" +
                            " [" + route + "][" + entry.getState() + "]; keep alive " + s);
                }
                rospl.freeEntry(entry);
                entry.updateExpiry(validDuration, timeUnit);
                freeConnections.add(entry);
            } else {
                closeConnection(entry);
                rospl.dropEntry();
                numConnections--;
            }

            notifyWaitingThread(rospl);

        } finally {
            poolLock.unlock();
        }
    }

    /**
     * If available, get a free pool entry for a route.
     *
     * @param rospl       the route-specific pool from which to get an entry
     *
     * @return  an available pool entry for the given route, or
     *          {@code null} if none is available
     */
    protected BasicPoolEntry getFreeEntry(final RouteSpecificPool rospl, final Object state) {

        BasicPoolEntry entry = null;
        poolLock.lock();
        try {
            boolean done = false;
            while(!done) {

                entry = rospl.allocEntry(state);

                if (entry != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Getting free connection"
                                + " [" + rospl.getRoute() + "][" + state + "]");

                    }
                    freeConnections.remove(entry);
                    if (entry.isExpired(System.currentTimeMillis())) {
                        // If the free entry isn't valid anymore, get rid of it
                        // and loop to find another one that might be valid.
                        if (log.isDebugEnabled()) {
                            log.debug("Closing expired free connection"
                                    + " [" + rospl.getRoute() + "][" + state + "]");
                        }
                        closeConnection(entry);
                        // We use dropEntry instead of deleteEntry because the entry
                        // is no longer "free" (we just allocated it), and deleteEntry
                        // can only be used to delete free entries.
                        rospl.dropEntry();
                        numConnections--;
                    } else {
                        leasedConnections.add(entry);
                        done = true;
                    }

                } else {
                    done = true;
                    if (log.isDebugEnabled()) {
                        log.debug("No free connections"
                                + " [" + rospl.getRoute() + "][" + state + "]");
                    }
                }
            }
        } finally {
            poolLock.unlock();
        }
        return entry;
    }


    /**
     * Creates a new pool entry.
     * This method assumes that the new connection will be handed
     * out immediately.
     *
     * @param rospl       the route-specific pool for which to create the entry
     * @param op        the operator for creating a connection
     *
     * @return  the new pool entry for a new connection
     */
    protected BasicPoolEntry createEntry(final RouteSpecificPool rospl,
                                         final ClientConnectionOperator op) {

        if (log.isDebugEnabled()) {
            log.debug("Creating new connection [" + rospl.getRoute() + "]");
        }

        // the entry will create the connection when needed
        final BasicPoolEntry entry = new BasicPoolEntry(op, rospl.getRoute(), connTTL, connTTLTimeUnit);

        poolLock.lock();
        try {
            rospl.createdEntry(entry);
            numConnections++;
            leasedConnections.add(entry);
        } finally {
            poolLock.unlock();
        }

        return entry;
    }


    /**
     * Deletes a given pool entry.
     * This closes the pooled connection and removes all references,
     * so that it can be GCed.
     *
     * <p><b>Note:</b> Does not remove the entry from the freeConnections list.
     * It is assumed that the caller has already handled this step.</p>
     * <!-- @@@ is that a good idea? or rather fix it? -->
     *
     * @param entry         the pool entry for the connection to delete
     */
    protected void deleteEntry(final BasicPoolEntry entry) {

        final HttpRoute route = entry.getPlannedRoute();

        if (log.isDebugEnabled()) {
            log.debug("Deleting connection"
                    + " [" + route + "][" + entry.getState() + "]");
        }

        poolLock.lock();
        try {

            closeConnection(entry);

            final RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.deleteEntry(entry);
            numConnections--;
            if (rospl.isUnused()) {
                routeToPool.remove(route);
            }

        } finally {
            poolLock.unlock();
        }
    }


    /**
     * Delete an old, free pool entry to make room for a new one.
     * Used to replace pool entries with ones for a different route.
     */
    protected void deleteLeastUsedEntry() {
        poolLock.lock();
        try {

            final BasicPoolEntry entry = freeConnections.remove();

            if (entry != null) {
                deleteEntry(entry);
            } else if (log.isDebugEnabled()) {
                log.debug("No free connection to delete");
            }

        } finally {
            poolLock.unlock();
        }
    }

    @Override
    protected void handleLostEntry(final HttpRoute route) {

        poolLock.lock();
        try {

            final RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.dropEntry();
            if (rospl.isUnused()) {
                routeToPool.remove(route);
            }

            numConnections--;
            notifyWaitingThread(rospl);

        } finally {
            poolLock.unlock();
        }
    }

    /**
     * Notifies a waiting thread that a connection is available.
     * This will wake a thread waiting in the specific route pool,
     * if there is one.
     * Otherwise, a thread in the connection pool will be notified.
     *
     * @param rospl     the pool in which to notify, or {@code null}
     */
    protected void notifyWaitingThread(final RouteSpecificPool rospl) {

        //@@@ while this strategy provides for best connection re-use,
        //@@@ is it fair? only do this if the connection is open?
        // Find the thread we are going to notify. We want to ensure that
        // each waiting thread is only interrupted once, so we will remove
        // it from all wait queues before interrupting.
        WaitingThread waitingThread = null;

        poolLock.lock();
        try {

            if ((rospl != null) && rospl.hasThread()) {
                if (log.isDebugEnabled()) {
                    log.debug("Notifying thread waiting on pool" +
                            " [" + rospl.getRoute() + "]");
                }
                waitingThread = rospl.nextThread();
            } else if (!waitingThreads.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Notifying thread waiting on any pool");
                }
                waitingThread = waitingThreads.remove();
            } else if (log.isDebugEnabled()) {
                log.debug("Notifying no-one, there are no waiting threads");
            }

            if (waitingThread != null) {
                waitingThread.wakeup();
            }

        } finally {
            poolLock.unlock();
        }
    }


    @Override
    public void deleteClosedConnections() {
        poolLock.lock();
        try {
            final Iterator<BasicPoolEntry>  iter = freeConnections.iterator();
            while (iter.hasNext()) {
                final BasicPoolEntry entry = iter.next();
                if (!entry.getConnection().isOpen()) {
                    iter.remove();
                    deleteEntry(entry);
                }
            }
        } finally {
            poolLock.unlock();
        }
    }

    /**
     * Closes idle connections.
     *
     * @param idletime  the time the connections should have been idle
     *                  in order to be closed now
     * @param tunit     the unit for the {@code idletime}
     */
    @Override
    public void closeIdleConnections(final long idletime, final TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        final long t = idletime > 0 ? idletime : 0;
        if (log.isDebugEnabled()) {
            log.debug("Closing connections idle longer than "  + t + " " + tunit);
        }
        // the latest time for which connections will be closed
        final long deadline = System.currentTimeMillis() - tunit.toMillis(t);
        poolLock.lock();
        try {
            final Iterator<BasicPoolEntry>  iter = freeConnections.iterator();
            while (iter.hasNext()) {
                final BasicPoolEntry entry = iter.next();
                if (entry.getUpdated() <= deadline) {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing connection last used @ " + new Date(entry.getUpdated()));
                    }
                    iter.remove();
                    deleteEntry(entry);
                }
            }
        } finally {
            poolLock.unlock();
        }
    }

    @Override
    public void closeExpiredConnections() {
        log.debug("Closing expired connections");
        final long now = System.currentTimeMillis();

        poolLock.lock();
        try {
            final Iterator<BasicPoolEntry>  iter = freeConnections.iterator();
            while (iter.hasNext()) {
                final BasicPoolEntry entry = iter.next();
                if (entry.isExpired(now)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing connection expired @ " + new Date(entry.getExpiry()));
                    }
                    iter.remove();
                    deleteEntry(entry);
                }
            }
        } finally {
            poolLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        poolLock.lock();
        try {
            if (shutdown) {
                return;
            }
            shutdown = true;

            // close all connections that are issued to an application
            final Iterator<BasicPoolEntry> iter1 = leasedConnections.iterator();
            while (iter1.hasNext()) {
                final BasicPoolEntry entry = iter1.next();
                iter1.remove();
                closeConnection(entry);
            }

            // close all free connections
            final Iterator<BasicPoolEntry> iter2 = freeConnections.iterator();
            while (iter2.hasNext()) {
                final BasicPoolEntry entry = iter2.next();
                iter2.remove();

                if (log.isDebugEnabled()) {
                    log.debug("Closing connection"
                            + " [" + entry.getPlannedRoute() + "][" + entry.getState() + "]");
                }
                closeConnection(entry);
            }

            // wake up all waiting threads
            final Iterator<WaitingThread> iwth = waitingThreads.iterator();
            while (iwth.hasNext()) {
                final WaitingThread waiter = iwth.next();
                iwth.remove();
                waiter.wakeup();
            }

            routeToPool.clear();

        } finally {
            poolLock.unlock();
        }
    }

    /**
     * since 4.1
     */
    public void setMaxTotalConnections(final int max) {
        poolLock.lock();
        try {
            maxTotalConnections = max;
        } finally {
            poolLock.unlock();
        }
    }


    /**
     * since 4.1
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

}

