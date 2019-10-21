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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

/**
 * This class maintains a map of HTTP routes to maximum number of connections allowed
 * for those routes. This class can be used by pooling
 * {@link org.apache.http.conn.ClientConnectionManager connection managers} for
 * a fine-grained control of connections on a per route basis.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  use {@link org.apache.http.pool.ConnPoolControl}
 */
@Deprecated
@Contract(threading = ThreadingBehavior.SAFE)
public final class ConnPerRouteBean implements ConnPerRoute {

    /** The default maximum number of connections allowed per host */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 2;   // Per RFC 2616 sec 8.1.4

    private final ConcurrentHashMap<HttpRoute, Integer> maxPerHostMap;

    private volatile int defaultMax;

    public ConnPerRouteBean(final int defaultMax) {
        super();
        this.maxPerHostMap = new ConcurrentHashMap<HttpRoute, Integer>();
        setDefaultMaxPerRoute(defaultMax);
    }

    public ConnPerRouteBean() {
        this(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
    }

    public int getDefaultMax() {
        return this.defaultMax;
    }

    /**
     * @since 4.1
     */
    public int getDefaultMaxPerRoute() {
        return this.defaultMax;
    }

    public void setDefaultMaxPerRoute(final int max) {
        Args.positive(max, "Default max per route");
        this.defaultMax = max;
    }

    public void setMaxForRoute(final HttpRoute route, final int max) {
        Args.notNull(route, "HTTP route");
        Args.positive(max, "Max per route");
        this.maxPerHostMap.put(route, Integer.valueOf(max));
    }

    @Override
    public int getMaxForRoute(final HttpRoute route) {
        Args.notNull(route, "HTTP route");
        final Integer max = this.maxPerHostMap.get(route);
        if (max != null) {
            return max.intValue();
        } else {
            return this.defaultMax;
        }
    }

    public void setMaxForRoutes(final Map<HttpRoute, Integer> map) {
        if (map == null) {
            return;
        }
        this.maxPerHostMap.clear();
        this.maxPerHostMap.putAll(map);
    }

    @Override
    public String toString() {
        return this.maxPerHostMap.toString();
    }

}
