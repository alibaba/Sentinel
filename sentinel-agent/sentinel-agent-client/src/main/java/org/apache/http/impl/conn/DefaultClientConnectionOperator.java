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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpInetSocketAddress;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/**
 * Default implementation of a {@link ClientConnectionOperator}. It uses a {@link SchemeRegistry}
 * to look up {@link SchemeSocketFactory} objects.
 * <p>
 * This connection operator is multihome network aware and will attempt to retry failed connects
 * against all known IP addresses sequentially until the connect is successful or all known
 * addresses fail to respond. Please note the same
 * {@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT} value will be used
 * for each connection attempt, so in the worst case the total elapsed time before timeout
 * can be {@code CONNECTION_TIMEOUT * n} where {@code n} is the number of IP addresses
 * of the given host. One can disable multihome support by overriding
 * the {@link #resolveHostname(String)} method and returning only one IP address for the given
 * host name.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_LINGER}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_REUSEADDR}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#TCP_NODELAY}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 * </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link PoolingHttpClientConnectionManager}.
 */
@Deprecated
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class DefaultClientConnectionOperator implements ClientConnectionOperator {

    private final Log log = LogFactory.getLog(getClass());

    /** The scheme registry for looking up socket factories. */
    protected final SchemeRegistry schemeRegistry; // @Contract(threading = ThreadingBehavior.SAFE)

    /** the custom-configured DNS lookup mechanism. */
    protected final DnsResolver dnsResolver;

    /**
     * Creates a new client connection operator for the given scheme registry.
     *
     * @param schemes   the scheme registry
     *
     * @since 4.2
     */
    public DefaultClientConnectionOperator(final SchemeRegistry schemes) {
        Args.notNull(schemes, "Scheme registry");
        this.schemeRegistry = schemes;
        this.dnsResolver = new SystemDefaultDnsResolver();
    }

    /**
    * Creates a new client connection operator for the given scheme registry
    * and the given custom DNS lookup mechanism.
    *
    * @param schemes
    *            the scheme registry
    * @param dnsResolver
    *            the custom DNS lookup mechanism
    */
    public DefaultClientConnectionOperator(final SchemeRegistry schemes,final DnsResolver dnsResolver) {
        Args.notNull(schemes, "Scheme registry");

        Args.notNull(dnsResolver, "DNS resolver");

        this.schemeRegistry = schemes;
        this.dnsResolver = dnsResolver;
    }

    @Override
    public OperatedClientConnection createConnection() {
        return new DefaultClientConnection();
    }

    private SchemeRegistry getSchemeRegistry(final HttpContext context) {
        SchemeRegistry reg = (SchemeRegistry) context.getAttribute(
                ClientContext.SCHEME_REGISTRY);
        if (reg == null) {
            reg = this.schemeRegistry;
        }
        return reg;
    }

    @Override
    public void openConnection(
            final OperatedClientConnection conn,
            final HttpHost target,
            final InetAddress local,
            final HttpContext context,
            final HttpParams params) throws IOException {
        Args.notNull(conn, "Connection");
        Args.notNull(target, "Target host");
        Args.notNull(params, "HTTP parameters");
        Asserts.check(!conn.isOpen(), "Connection must not be open");

        final SchemeRegistry registry = getSchemeRegistry(context);
        final Scheme schm = registry.getScheme(target.getSchemeName());
        final SchemeSocketFactory sf = schm.getSchemeSocketFactory();

        final InetAddress[] addresses = resolveHostname(target.getHostName());
        final int port = schm.resolvePort(target.getPort());
        for (int i = 0; i < addresses.length; i++) {
            final InetAddress address = addresses[i];
            final boolean last = i == addresses.length - 1;

            Socket sock = sf.createSocket(params);
            conn.opening(sock, target);

            final InetSocketAddress remoteAddress = new HttpInetSocketAddress(target, address, port);
            InetSocketAddress localAddress = null;
            if (local != null) {
                localAddress = new InetSocketAddress(local, 0);
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connecting to " + remoteAddress);
            }
            try {
                final Socket connsock = sf.connectSocket(sock, remoteAddress, localAddress, params);
                if (sock != connsock) {
                    sock = connsock;
                    conn.opening(sock, target);
                }
                prepareSocket(sock, context, params);
                conn.openCompleted(sf.isSecure(sock), params);
                return;
            } catch (final ConnectException ex) {
                if (last) {
                    throw ex;
                }
            } catch (final ConnectTimeoutException ex) {
                if (last) {
                    throw ex;
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connect to " + remoteAddress + " timed out. " +
                        "Connection will be retried using another IP address");
            }
        }
    }

    @Override
    public void updateSecureConnection(
            final OperatedClientConnection conn,
            final HttpHost target,
            final HttpContext context,
            final HttpParams params) throws IOException {
        Args.notNull(conn, "Connection");
        Args.notNull(target, "Target host");
        Args.notNull(params, "Parameters");
        Asserts.check(conn.isOpen(), "Connection must be open");

        final SchemeRegistry registry = getSchemeRegistry(context);
        final Scheme schm = registry.getScheme(target.getSchemeName());
        Asserts.check(schm.getSchemeSocketFactory() instanceof SchemeLayeredSocketFactory,
            "Socket factory must implement SchemeLayeredSocketFactory");
        final SchemeLayeredSocketFactory lsf = (SchemeLayeredSocketFactory) schm.getSchemeSocketFactory();
        final Socket sock = lsf.createLayeredSocket(
                conn.getSocket(), target.getHostName(), schm.resolvePort(target.getPort()), params);
        prepareSocket(sock, context, params);
        conn.update(sock, target, lsf.isSecure(sock), params);
    }

    /**
     * Performs standard initializations on a newly created socket.
     *
     * @param sock      the socket to prepare
     * @param context   the context for the connection
     * @param params    the parameters from which to prepare the socket
     *
     * @throws IOException      in case of an IO problem
     */
    protected void prepareSocket(
            final Socket sock,
            final HttpContext context,
            final HttpParams params) throws IOException {
        sock.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        sock.setSoTimeout(HttpConnectionParams.getSoTimeout(params));

        final int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            sock.setSoLinger(linger > 0, linger);
        }
    }

    /**
     * Resolves the given host name to an array of corresponding IP addresses, based on the
     * configured name service on the provided DNS resolver. If one wasn't provided, the system
     * configuration is used.
     *
     * @param host host name to resolve
     * @return array of IP addresses
     * @exception  UnknownHostException  if no IP address for the host could be determined.
     *
     * @see DnsResolver
     * @see SystemDefaultDnsResolver
     *
     * @since 4.1
     */
    protected InetAddress[] resolveHostname(final String host) throws UnknownHostException {
            return dnsResolver.resolve(host);
    }

}

