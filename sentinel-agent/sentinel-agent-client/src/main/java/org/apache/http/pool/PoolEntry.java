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
package org.apache.http.pool;

import java.util.concurrent.TimeUnit;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;

/**
 * Pool entry containing a pool connection object along with its route.
 * <p>
 * The connection contained by the pool entry may have an expiration time which
 * can be either set upon construction time or updated with
 * the {@link #updateExpiry(long, TimeUnit)}.
 * <p>
 * Pool entry may also have an object associated with it that represents
 * a connection state (usually a security principal or a unique token identifying
 * the user whose credentials have been used while establishing the connection).
 *
 * @param <T> the route type that represents the opposite endpoint of a pooled
 *   connection.
 * @param <C> the connection type.
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public abstract class PoolEntry<T, C> {

    private final String id;
    private final T route;
    private final C conn;
    private final long created;
    private final long validityDeadline;

    private long updated;

    private long expiry;

    private volatile Object state;

    /**
     * Creates new {@code PoolEntry} instance.
     *
     * @param id unique identifier of the pool entry. May be {@code null}.
     * @param route route to the opposite endpoint.
     * @param conn the connection.
     * @param timeToLive maximum time to live. May be zero if the connection
     *   does not have an expiry deadline.
     * @param tunit time unit.
     */
    public PoolEntry(final String id, final T route, final C conn,
            final long timeToLive, final TimeUnit tunit) {
        super();
        Args.notNull(route, "Route");
        Args.notNull(conn, "Connection");
        Args.notNull(tunit, "Time unit");
        this.id = id;
        this.route = route;
        this.conn = conn;
        this.created = System.currentTimeMillis();
        this.updated = this.created;
        if (timeToLive > 0) {
            this.validityDeadline = this.created + tunit.toMillis(timeToLive);
        } else {
            this.validityDeadline = Long.MAX_VALUE;
        }
        this.expiry = this.validityDeadline;
    }

    /**
     * Creates new {@code PoolEntry} instance without an expiry deadline.
     *
     * @param id unique identifier of the pool entry. May be {@code null}.
     * @param route route to the opposite endpoint.
     * @param conn the connection.
     */
    public PoolEntry(final String id, final T route, final C conn) {
        this(id, route, conn, 0, TimeUnit.MILLISECONDS);
    }

    public String getId() {
        return this.id;
    }

    public T getRoute() {
        return this.route;
    }

    public C getConnection() {
        return this.conn;
    }

    public long getCreated() {
        return this.created;
    }

    /**
     * @since 4.4
     */
    public long getValidityDeadline() {
        return this.validityDeadline;
    }

    /**
     * @deprecated use {@link #getValidityDeadline()}
     */
    @Deprecated
    public long getValidUnit() {
        return this.validityDeadline;
    }

    public Object getState() {
        return this.state;
    }

    public void setState(final Object state) {
        this.state = state;
    }

    public synchronized long getUpdated() {
        return this.updated;
    }

    public synchronized long getExpiry() {
        return this.expiry;
    }

    public synchronized void updateExpiry(final long time, final TimeUnit tunit) {
        Args.notNull(tunit, "Time unit");
        this.updated = System.currentTimeMillis();
        final long newExpiry;
        if (time > 0) {
            newExpiry = this.updated + tunit.toMillis(time);
        } else {
            newExpiry = Long.MAX_VALUE;
        }
        this.expiry = Math.min(newExpiry, this.validityDeadline);
    }

    public synchronized boolean isExpired(final long now) {
        return now >= this.expiry;
    }

    /**
     * Invalidates the pool entry and closes the pooled connection associated
     * with it.
     */
    public abstract void close();

    /**
     * Returns {@code true} if the pool entry has been invalidated.
     */
    public abstract boolean isClosed();

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[id:");
        buffer.append(this.id);
        buffer.append("][route:");
        buffer.append(this.route);
        buffer.append("][state:");
        buffer.append(this.state);
        buffer.append("]");
        return buffer.toString();
    }

}
