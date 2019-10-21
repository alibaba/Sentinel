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


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;


/**
 * Default implementation of an {@link HttpRoutePlanner}.
 * This implementation is based on {@link java.net.ProxySelector}.
 * By default, it will pick up the proxy settings of the JVM, either
 * from system properties or from the browser running the application.
 * Additionally, it interprets some
 * {@link org.apache.http.conn.params.ConnRoutePNames parameters},
 * though not the {@link
 * org.apache.http.conn.params.ConnRoutePNames#DEFAULT_PROXY DEFAULT_PROXY}.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
 * </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link SystemDefaultRoutePlanner}
 */
@Deprecated
public class ProxySelectorRoutePlanner implements HttpRoutePlanner {

    /** The scheme registry. */
    protected final SchemeRegistry schemeRegistry; // @Contract(threading = ThreadingBehavior.SAFE)

    /** The proxy selector to use, or {@code null} for system default. */
    protected ProxySelector proxySelector;

    /**
     * Creates a new proxy selector route planner.
     *
     * @param schreg    the scheme registry
     * @param prosel    the proxy selector, or
     *                  {@code null} for the system default
     */
    public ProxySelectorRoutePlanner(final SchemeRegistry schreg,
                                     final ProxySelector prosel) {
        Args.notNull(schreg, "SchemeRegistry");
        schemeRegistry = schreg;
        proxySelector  = prosel;
    }

    /**
     * Obtains the proxy selector to use.
     *
     * @return the proxy selector, or {@code null} for the system default
     */
    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }

    /**
     * Sets the proxy selector to use.
     *
     * @param prosel    the proxy selector, or
     *                  {@code null} to use the system default
     */
    public void setProxySelector(final ProxySelector prosel) {
        this.proxySelector = prosel;
    }

    @Override
    public HttpRoute determineRoute(final HttpHost target,
                                    final HttpRequest request,
                                    final HttpContext context)
        throws HttpException {

        Args.notNull(request, "HTTP request");

        // If we have a forced route, we can do without a target.
        HttpRoute route =
            ConnRouteParams.getForcedRoute(request.getParams());
        if (route != null) {
            return route;
        }

        // If we get here, there is no forced route.
        // So we need a target to compute a route.

        Asserts.notNull(target, "Target host");

        final InetAddress local =
            ConnRouteParams.getLocalAddress(request.getParams());
        final HttpHost proxy = determineProxy(target, request, context);

        final Scheme schm =
            this.schemeRegistry.getScheme(target.getSchemeName());
        // as it is typically used for TLS/SSL, we assume that
        // a layered scheme implies a secure connection
        final boolean secure = schm.isLayered();

        if (proxy == null) {
            route = new HttpRoute(target, local, secure);
        } else {
            route = new HttpRoute(target, local, proxy, secure);
        }
        return route;
    }

    /**
     * Determines a proxy for the given target.
     *
     * @param target    the planned target, never {@code null}
     * @param request   the request to be sent, never {@code null}
     * @param context   the context, or {@code null}
     *
     * @return  the proxy to use, or {@code null} for a direct route
     *
     * @throws HttpException
     *         in case of system proxy settings that cannot be handled
     */
    protected HttpHost determineProxy(final HttpHost    target,
                                      final HttpRequest request,
                                      final HttpContext context)
        throws HttpException {

        // the proxy selector can be 'unset', so we better deal with null here
        ProxySelector psel = this.proxySelector;
        if (psel == null) {
            psel = ProxySelector.getDefault();
        }
        if (psel == null) {
            return null;
        }

        URI targetURI = null;
        try {
            targetURI = new URI(target.toURI());
        } catch (final URISyntaxException usx) {
            throw new HttpException
                ("Cannot convert host to URI: " + target, usx);
        }
        final List<Proxy> proxies = psel.select(targetURI);

        final Proxy p = chooseProxy(proxies, target, request, context);

        HttpHost result = null;
        if (p.type() == Proxy.Type.HTTP) {
            // convert the socket address to an HttpHost
            if (!(p.address() instanceof InetSocketAddress)) {
                throw new HttpException
                    ("Unable to handle non-Inet proxy address: "+p.address());
            }
            final InetSocketAddress isa = (InetSocketAddress) p.address();
            // assume default scheme (http)
            result = new HttpHost(getHost(isa), isa.getPort());
        }

        return result;
    }

    /**
     * Obtains a host from an {@link InetSocketAddress}.
     *
     * @param isa       the socket address
     *
     * @return  a host string, either as a symbolic name or
     *          as a literal IP address string
     * <p>
     * (TODO: determine format for IPv6 addresses, with or without [brackets])
     * </p>
     */
    protected String getHost(final InetSocketAddress isa) {

        //@@@ Will this work with literal IPv6 addresses, or do we
        //@@@ need to wrap these in [] for the string representation?
        //@@@ Having it in this method at least allows for easy workarounds.
       return isa.isUnresolved() ?
            isa.getHostName() : isa.getAddress().getHostAddress();

    }

    /**
     * Chooses a proxy from a list of available proxies.
     * The default implementation just picks the first non-SOCKS proxy
     * from the list. If there are only SOCKS proxies,
     * {@link Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     * Derived classes may implement more advanced strategies,
     * such as proxy rotation if there are multiple options.
     *
     * @param proxies   the list of proxies to choose from,
     *                  never {@code null} or empty
     * @param target    the planned target, never {@code null}
     * @param request   the request to be sent, never {@code null}
     * @param context   the context, or {@code null}
     *
     * @return  a proxy type
     */
    protected Proxy chooseProxy(final List<Proxy> proxies,
                                final HttpHost    target,
                                final HttpRequest request,
                                final HttpContext context) {
        Args.notEmpty(proxies, "List of proxies");

        Proxy result = null;

        // check the list for one we can use
        for (int i=0; (result == null) && (i < proxies.size()); i++) {

            final Proxy p = proxies.get(i);
            switch (p.type()) {

            case DIRECT:
            case HTTP:
                result = p;
                break;

            case SOCKS:
                // SOCKS hosts are not handled on the route level.
                // The socket may make use of the SOCKS host though.
                break;
            }
        }

        if (result == null) {
            //@@@ log as warning or info that only a socks proxy is available?
            // result can only be null if all proxies are socks proxies
            // socks proxies are not handled on the route planning level
            result = Proxy.NO_PROXY;
        }

        return result;
    }

}

