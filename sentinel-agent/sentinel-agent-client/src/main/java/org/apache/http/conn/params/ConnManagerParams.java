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
package org.apache.http.conn.params;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * An adaptor for manipulating HTTP connection management
 * parameters in {@link HttpParams}.
 *
 * @since 4.0
 *
 * @see ConnManagerPNames
 *
 * @deprecated (4.1) use configuration methods of the specific connection manager implementation.
 */
@Deprecated
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class ConnManagerParams implements ConnManagerPNames {

    /** The default maximum number of connections allowed overall */
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    /**
     * Returns the timeout in milliseconds used when retrieving a
     * {@link org.apache.http.conn.ManagedClientConnection} from the
     * {@link org.apache.http.conn.ClientConnectionManager}.
     *
     * @return timeout in milliseconds.
     *
     * @deprecated (4.1)  use {@link
     *   org.apache.http.params.HttpConnectionParams#getConnectionTimeout(HttpParams)}
     */
    @Deprecated
    public static long getTimeout(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getLongParameter(TIMEOUT, 0);
    }

    /**
     * Sets the timeout in milliseconds used when retrieving a
     * {@link org.apache.http.conn.ManagedClientConnection} from the
     * {@link org.apache.http.conn.ClientConnectionManager}.
     *
     * @param timeout the timeout in milliseconds
     *
     * @deprecated (4.1)  use {@link
     *   org.apache.http.params.HttpConnectionParams#setConnectionTimeout(HttpParams, int)}
     */
    @Deprecated
    public static void setTimeout(final HttpParams params, final long timeout) {
        Args.notNull(params, "HTTP parameters");
        params.setLongParameter(TIMEOUT, timeout);
    }

    /** The default maximum number of connections allowed per host */
    private static final ConnPerRoute DEFAULT_CONN_PER_ROUTE = new ConnPerRoute() {

        @Override
        public int getMaxForRoute(final HttpRoute route) {
            return ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        }

    };

    /**
     * Sets lookup interface for maximum number of connections allowed per route.
     *
     * @param params HTTP parameters
     * @param connPerRoute lookup interface for maximum number of connections allowed
     *        per route
     */
    public static void setMaxConnectionsPerRoute(final HttpParams params,
                                                final ConnPerRoute connPerRoute) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(MAX_CONNECTIONS_PER_ROUTE, connPerRoute);
    }

    /**
     * Returns lookup interface for maximum number of connections allowed per route.
     *
     * @param params HTTP parameters
     *
     * @return lookup interface for maximum number of connections allowed per route.
     */
    public static ConnPerRoute getMaxConnectionsPerRoute(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        ConnPerRoute connPerRoute = (ConnPerRoute) params.getParameter(MAX_CONNECTIONS_PER_ROUTE);
        if (connPerRoute == null) {
            connPerRoute = DEFAULT_CONN_PER_ROUTE;
        }
        return connPerRoute;
    }

    /**
     * Sets the maximum number of connections allowed.
     *
     * @param params HTTP parameters
     * @param maxTotalConnections The maximum number of connections allowed.
     */
    public static void setMaxTotalConnections(
            final HttpParams params,
            final int maxTotalConnections) {
        Args.notNull(params, "HTTP parameters");
        params.setIntParameter(MAX_TOTAL_CONNECTIONS, maxTotalConnections);
    }

    /**
     * Gets the maximum number of connections allowed.
     *
     * @param params HTTP parameters
     *
     * @return The maximum number of connections allowed.
     */
    public static int getMaxTotalConnections(
            final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getIntParameter(MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS);
    }

}
