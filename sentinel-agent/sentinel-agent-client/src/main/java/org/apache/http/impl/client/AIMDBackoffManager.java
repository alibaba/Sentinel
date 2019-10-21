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
package org.apache.http.impl.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.BackoffManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.util.Args;

/**
 * <p>The {@code AIMDBackoffManager} applies an additive increase,
 * multiplicative decrease (AIMD) to managing a dynamic limit to
 * the number of connections allowed to a given host. You may want
 * to experiment with the settings for the cooldown periods and the
 * backoff factor to get the adaptive behavior you want.</p>
 *
 * <p>Generally speaking, shorter cooldowns will lead to more steady-state
 * variability but faster reaction times, while longer cooldowns
 * will lead to more stable equilibrium behavior but slower reaction
 * times.</p>
 *
 * <p>Similarly, higher backoff factors promote greater
 * utilization of available capacity at the expense of fairness
 * among clients. Lower backoff factors allow equal distribution of
 * capacity among clients (fairness) to happen faster, at the
 * expense of having more server capacity unused in the short term.</p>
 *
 * @since 4.2
 */
public class AIMDBackoffManager implements BackoffManager {

    private final ConnPoolControl<HttpRoute> connPerRoute;
    private final Clock clock;
    private final Map<HttpRoute,Long> lastRouteProbes;
    private final Map<HttpRoute,Long> lastRouteBackoffs;
    private long coolDown = 5 * 1000L;
    private double backoffFactor = 0.5;
    private int cap = 2; // Per RFC 2616 sec 8.1.4

    /**
     * Creates an {@code AIMDBackoffManager} to manage
     * per-host connection pool sizes represented by the
     * given {@link ConnPoolControl}.
     * @param connPerRoute per-host routing maximums to
     *   be managed
     */
    public AIMDBackoffManager(final ConnPoolControl<HttpRoute> connPerRoute) {
        this(connPerRoute, new SystemClock());
    }

    AIMDBackoffManager(final ConnPoolControl<HttpRoute> connPerRoute, final Clock clock) {
        this.clock = clock;
        this.connPerRoute = connPerRoute;
        this.lastRouteProbes = new HashMap<HttpRoute,Long>();
        this.lastRouteBackoffs = new HashMap<HttpRoute,Long>();
    }

    @Override
    public void backOff(final HttpRoute route) {
        synchronized(connPerRoute) {
            final int curr = connPerRoute.getMaxPerRoute(route);
            final Long lastUpdate = getLastUpdate(lastRouteBackoffs, route);
            final long now = clock.getCurrentTime();
            if (now - lastUpdate.longValue() < coolDown) {
                return;
            }
            connPerRoute.setMaxPerRoute(route, getBackedOffPoolSize(curr));
            lastRouteBackoffs.put(route, Long.valueOf(now));
        }
    }

    private int getBackedOffPoolSize(final int curr) {
        if (curr <= 1) {
            return 1;
        }
        return (int)(Math.floor(backoffFactor * curr));
    }

    @Override
    public void probe(final HttpRoute route) {
        synchronized(connPerRoute) {
            final int curr = connPerRoute.getMaxPerRoute(route);
            final int max = (curr >= cap) ? cap : curr + 1;
            final Long lastProbe = getLastUpdate(lastRouteProbes, route);
            final Long lastBackoff = getLastUpdate(lastRouteBackoffs, route);
            final long now = clock.getCurrentTime();
            if (now - lastProbe.longValue() < coolDown || now - lastBackoff.longValue() < coolDown) {
                return;
            }
            connPerRoute.setMaxPerRoute(route, max);
            lastRouteProbes.put(route, Long.valueOf(now));
        }
    }

    private Long getLastUpdate(final Map<HttpRoute,Long> updates, final HttpRoute route) {
        Long lastUpdate = updates.get(route);
        if (lastUpdate == null) {
            lastUpdate = Long.valueOf(0L);
        }
        return lastUpdate;
    }

    /**
     * Sets the factor to use when backing off; the new
     * per-host limit will be roughly the current max times
     * this factor. {@code Math.floor} is applied in the
     * case of non-integer outcomes to ensure we actually
     * decrease the pool size. Pool sizes are never decreased
     * below 1, however. Defaults to 0.5.
     * @param d must be between 0.0 and 1.0, exclusive.
     */
    public void setBackoffFactor(final double d) {
        Args.check(d > 0.0 && d < 1.0, "Backoff factor must be 0.0 < f < 1.0");
        backoffFactor = d;
    }

    /**
     * Sets the amount of time, in milliseconds, to wait between
     * adjustments in pool sizes for a given host, to allow
     * enough time for the adjustments to take effect. Defaults
     * to 5000L (5 seconds).
     * @param l must be positive
     */
    public void setCooldownMillis(final long l) {
        Args.positive(coolDown, "Cool down");
        coolDown = l;
    }

    /**
     * Sets the absolute maximum per-host connection pool size to
     * probe up to; defaults to 2 (the default per-host max).
     * @param cap must be &gt;= 1
     */
    public void setPerHostConnectionCap(final int cap) {
        Args.positive(cap, "Per host connection cap");
        this.cap = cap;
    }

}
