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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.protocol.HttpContext;

/**
 * {@link org.apache.http.conn.routing.HttpRoutePlanner} implementation
 * based on {@link ProxySelector}. By default, this class will pick up
 * the proxy settings of the JVM, either from system properties
 * or from the browser running the application.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class SystemDefaultRoutePlanner extends DefaultRoutePlanner {

    private final ProxySelector proxySelector;

    /**
     * @param proxySelector the proxy selector, or {@code null} for the system default
     */
    public SystemDefaultRoutePlanner(
            final SchemePortResolver schemePortResolver,
            final ProxySelector proxySelector) {
        super(schemePortResolver);
        this.proxySelector = proxySelector;
    }

    /**
     * @param proxySelector the proxy selector, or {@code null} for the system default
     */
    public SystemDefaultRoutePlanner(final ProxySelector proxySelector) {
        this(null, proxySelector);
    }

    @Override
    protected HttpHost determineProxy(
            final HttpHost    target,
            final HttpRequest request,
            final HttpContext context) throws HttpException {
        final URI targetURI;
        try {
            targetURI = new URI(target.toURI());
        } catch (final URISyntaxException ex) {
            throw new HttpException("Cannot convert host to URI: " + target, ex);
        }
        ProxySelector proxySelectorInstance = this.proxySelector;
        if (proxySelectorInstance == null) {
            proxySelectorInstance = ProxySelector.getDefault();
        }
        if (proxySelectorInstance == null) {
            //The proxy selector can be "unset", so we must be able to deal with a null selector
            return null;
        }
        final List<Proxy> proxies = proxySelectorInstance.select(targetURI);
        final Proxy p = chooseProxy(proxies);
        HttpHost result = null;
        if (p.type() == Proxy.Type.HTTP) {
            // convert the socket address to an HttpHost
            if (!(p.address() instanceof InetSocketAddress)) {
                throw new HttpException("Unable to handle non-Inet proxy address: " + p.address());
            }
            final InetSocketAddress isa = (InetSocketAddress) p.address();
            // assume default scheme (http)
            result = new HttpHost(getHost(isa), isa.getPort());
        }

        return result;
    }

    private String getHost(final InetSocketAddress isa) {

        //@@@ Will this work with literal IPv6 addresses, or do we
        //@@@ need to wrap these in [] for the string representation?
        //@@@ Having it in this method at least allows for easy workarounds.
       return isa.isUnresolved() ?
            isa.getHostName() : isa.getAddress().getHostAddress();

    }

    private Proxy chooseProxy(final List<Proxy> proxies) {
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
