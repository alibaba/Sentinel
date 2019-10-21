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

import java.net.InetAddress;

import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/**
 * An adaptor for manipulating HTTP routing parameters
 * in {@link HttpParams}.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.client.config.RequestConfig}.
 */
@Deprecated
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class ConnRouteParams implements ConnRoutePNames {

    /**
     * A special value indicating "no host".
     * This relies on a nonsense scheme name to avoid conflicts
     * with actual hosts. Note that this is a <i>valid</i> host.
     */
    public static final HttpHost NO_HOST =
        new HttpHost("127.0.0.255", 0, "no-host"); // Immutable

    /**
     * A special value indicating "no route".
     * This is a route with {@link #NO_HOST} as the target.
     */
    public static final HttpRoute NO_ROUTE = new HttpRoute(NO_HOST); // Immutable

    /** Disabled default constructor. */
    private ConnRouteParams() {
        // no body
    }

    /**
     * Obtains the {@link ConnRoutePNames#DEFAULT_PROXY DEFAULT_PROXY}
     * parameter value.
     * {@link #NO_HOST} will be mapped to {@code null},
     * to allow unsetting in a hierarchy.
     *
     * @param params    the parameters in which to look up
     *
     * @return  the default proxy set in the argument parameters, or
     *          {@code null} if not set
     */
    public static HttpHost getDefaultProxy(final HttpParams params) {
        Args.notNull(params, "Parameters");
        HttpHost proxy = (HttpHost)
            params.getParameter(DEFAULT_PROXY);
        if ((proxy != null) && NO_HOST.equals(proxy)) {
            // value is explicitly unset
            proxy = null;
        }
        return proxy;
    }

    /**
     * Sets the {@link ConnRoutePNames#DEFAULT_PROXY DEFAULT_PROXY}
     * parameter value.
     *
     * @param params    the parameters in which to set the value
     * @param proxy     the value to set, may be {@code null}.
     *                  Note that {@link #NO_HOST} will be mapped to
     *                  {@code null} by {@link #getDefaultProxy},
     *                  to allow for explicit unsetting in hierarchies.
     */
    public static void setDefaultProxy(final HttpParams params,
                                             final HttpHost proxy) {
        Args.notNull(params, "Parameters");
        params.setParameter(DEFAULT_PROXY, proxy);
    }

    /**
     * Obtains the {@link ConnRoutePNames#FORCED_ROUTE FORCED_ROUTE}
     * parameter value.
     * {@link #NO_ROUTE} will be mapped to {@code null},
     * to allow unsetting in a hierarchy.
     *
     * @param params    the parameters in which to look up
     *
     * @return  the forced route set in the argument parameters, or
     *          {@code null} if not set
     */
    public static HttpRoute getForcedRoute(final HttpParams params) {
        Args.notNull(params, "Parameters");
        HttpRoute route = (HttpRoute)
            params.getParameter(FORCED_ROUTE);
        if ((route != null) && NO_ROUTE.equals(route)) {
            // value is explicitly unset
            route = null;
        }
        return route;
    }

    /**
     * Sets the {@link ConnRoutePNames#FORCED_ROUTE FORCED_ROUTE}
     * parameter value.
     *
     * @param params    the parameters in which to set the value
     * @param route     the value to set, may be {@code null}.
     *                  Note that {@link #NO_ROUTE} will be mapped to
     *                  {@code null} by {@link #getForcedRoute},
     *                  to allow for explicit unsetting in hierarchies.
     */
    public static void setForcedRoute(final HttpParams params,
                                            final HttpRoute route) {
        Args.notNull(params, "Parameters");
        params.setParameter(FORCED_ROUTE, route);
    }

    /**
     * Obtains the {@link ConnRoutePNames#LOCAL_ADDRESS LOCAL_ADDRESS}
     * parameter value.
     * There is no special value that would automatically be mapped to
     * {@code null}. You can use the wildcard address (0.0.0.0 for IPv4,
     * :: for IPv6) to override a specific local address in a hierarchy.
     *
     * @param params    the parameters in which to look up
     *
     * @return  the local address set in the argument parameters, or
     *          {@code null} if not set
     */
    public static InetAddress getLocalAddress(final HttpParams params) {
        Args.notNull(params, "Parameters");
        final InetAddress local = (InetAddress)
            params.getParameter(LOCAL_ADDRESS);
        // no explicit unsetting
        return local;
    }

    /**
     * Sets the {@link ConnRoutePNames#LOCAL_ADDRESS LOCAL_ADDRESS}
     * parameter value.
     *
     * @param params    the parameters in which to set the value
     * @param local     the value to set, may be {@code null}
     */
    public static void setLocalAddress(final HttpParams params,
                                             final InetAddress local) {
        Args.notNull(params, "Parameters");
        params.setParameter(LOCAL_ADDRESS, local);
    }

}

