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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

/**
 * Default implementation of {@link RequestDirector}.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#PROTOCOL_VERSION}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#STRICT_TRANSFER_ENCODING}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#USE_EXPECT_CONTINUE}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#WAIT_FOR_CONTINUE}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#USER_AGENT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_LINGER}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_REUSEADDR}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#TCP_NODELAY}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#STALE_CONNECTION_CHECK}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#DEFAULT_PROXY}</li>
 *  <li>{@link org.apache.http.cookie.params.CookieSpecPNames#DATE_PATTERNS}</li>
 *  <li>{@link org.apache.http.cookie.params.CookieSpecPNames#SINGLE_COOKIE_HEADER}</li>
 *  <li>{@link org.apache.http.auth.params.AuthPNames#CREDENTIAL_CHARSET}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#COOKIE_POLICY}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#HANDLE_AUTHENTICATION}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#HANDLE_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#MAX_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#ALLOW_CIRCULAR_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#VIRTUAL_HOST}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HOST}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HEADERS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#CONN_MANAGER_TIMEOUT}</li>
 * </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3)
 */
@Deprecated
public class DefaultRequestDirector implements RequestDirector {

    private final Log log;

    /** The connection manager. */
    protected final ClientConnectionManager connManager;

    /** The route planner. */
    protected final HttpRoutePlanner routePlanner;

    /** The connection re-use strategy. */
    protected final ConnectionReuseStrategy reuseStrategy;

    /** The keep-alive duration strategy. */
    protected final ConnectionKeepAliveStrategy keepAliveStrategy;

    /** The request executor. */
    protected final HttpRequestExecutor requestExec;

    /** The HTTP protocol processor. */
    protected final HttpProcessor httpProcessor;

    /** The request retry handler. */
    protected final HttpRequestRetryHandler retryHandler;

    /** The redirect handler. */
    @Deprecated
    protected final RedirectHandler redirectHandler;

    /** The redirect strategy. */
    protected final RedirectStrategy redirectStrategy;

    /** The target authentication handler. */
    @Deprecated
    protected final AuthenticationHandler targetAuthHandler;

    /** The target authentication handler. */
    protected final AuthenticationStrategy targetAuthStrategy;

    /** The proxy authentication handler. */
    @Deprecated
    protected final AuthenticationHandler proxyAuthHandler;

    /** The proxy authentication handler. */
    protected final AuthenticationStrategy proxyAuthStrategy;

    /** The user token handler. */
    protected final UserTokenHandler userTokenHandler;

    /** The HTTP parameters. */
    protected final HttpParams params;

    /** The currently allocated connection. */
    protected ManagedClientConnection managedConn;

    protected final AuthState targetAuthState;

    protected final AuthState proxyAuthState;

    private final HttpAuthenticator authenticator;

    private int execCount;

    private int redirectCount;

    private final int maxRedirects;

    private HttpHost virtualHost;

    @Deprecated
    public DefaultRequestDirector(
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectHandler redirectHandler,
            final AuthenticationHandler targetAuthHandler,
            final AuthenticationHandler proxyAuthHandler,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {
        this(LogFactory.getLog(DefaultRequestDirector.class),
                requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler,
                new DefaultRedirectStrategyAdaptor(redirectHandler),
                new AuthenticationStrategyAdaptor(targetAuthHandler),
                new AuthenticationStrategyAdaptor(proxyAuthHandler),
                userTokenHandler,
                params);
    }


    @Deprecated
    public DefaultRequestDirector(
            final Log log,
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectStrategy redirectStrategy,
            final AuthenticationHandler targetAuthHandler,
            final AuthenticationHandler proxyAuthHandler,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {
        this(LogFactory.getLog(DefaultRequestDirector.class),
                requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler,
                redirectStrategy,
                new AuthenticationStrategyAdaptor(targetAuthHandler),
                new AuthenticationStrategyAdaptor(proxyAuthHandler),
                userTokenHandler,
                params);
    }

    /**
     * @since 4.2
     */
    public DefaultRequestDirector(
            final Log log,
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectStrategy redirectStrategy,
            final AuthenticationStrategy targetAuthStrategy,
            final AuthenticationStrategy proxyAuthStrategy,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {

        Args.notNull(log, "Log");
        Args.notNull(requestExec, "Request executor");
        Args.notNull(conman, "Client connection manager");
        Args.notNull(reustrat, "Connection reuse strategy");
        Args.notNull(kastrat, "Connection keep alive strategy");
        Args.notNull(rouplan, "Route planner");
        Args.notNull(httpProcessor, "HTTP protocol processor");
        Args.notNull(retryHandler, "HTTP request retry handler");
        Args.notNull(redirectStrategy, "Redirect strategy");
        Args.notNull(targetAuthStrategy, "Target authentication strategy");
        Args.notNull(proxyAuthStrategy, "Proxy authentication strategy");
        Args.notNull(userTokenHandler, "User token handler");
        Args.notNull(params, "HTTP parameters");
        this.log               = log;
        this.authenticator     = new HttpAuthenticator(log);
        this.requestExec        = requestExec;
        this.connManager        = conman;
        this.reuseStrategy      = reustrat;
        this.keepAliveStrategy  = kastrat;
        this.routePlanner       = rouplan;
        this.httpProcessor      = httpProcessor;
        this.retryHandler       = retryHandler;
        this.redirectStrategy   = redirectStrategy;
        this.targetAuthStrategy = targetAuthStrategy;
        this.proxyAuthStrategy  = proxyAuthStrategy;
        this.userTokenHandler   = userTokenHandler;
        this.params             = params;

        if (redirectStrategy instanceof DefaultRedirectStrategyAdaptor) {
            this.redirectHandler = ((DefaultRedirectStrategyAdaptor) redirectStrategy).getHandler();
        } else {
            this.redirectHandler = null;
        }
        if (targetAuthStrategy instanceof AuthenticationStrategyAdaptor) {
            this.targetAuthHandler = ((AuthenticationStrategyAdaptor) targetAuthStrategy).getHandler();
        } else {
            this.targetAuthHandler = null;
        }
        if (proxyAuthStrategy instanceof AuthenticationStrategyAdaptor) {
            this.proxyAuthHandler = ((AuthenticationStrategyAdaptor) proxyAuthStrategy).getHandler();
        } else {
            this.proxyAuthHandler = null;
        }

        this.managedConn = null;

        this.execCount = 0;
        this.redirectCount = 0;
        this.targetAuthState = new AuthState();
        this.proxyAuthState = new AuthState();
        this.maxRedirects = this.params.getIntParameter(ClientPNames.MAX_REDIRECTS, 100);
    }


    private RequestWrapper wrapRequest(
            final HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper(
                    (HttpEntityEnclosingRequest) request);
        } else {
            return new RequestWrapper(
                    request);
        }
    }


    protected void rewriteRequestURI(
            final RequestWrapper request,
            final HttpRoute route) throws ProtocolException {
        try {

            URI uri = request.getURI();
            if (route.getProxyHost() != null && !route.isTunnelled()) {
                // Make sure the request URI is absolute
                if (!uri.isAbsolute()) {
                    final HttpHost target = route.getTargetHost();
                    uri = URIUtils.rewriteURI(uri, target, true);
                } else {
                    uri = URIUtils.rewriteURI(uri);
                }
            } else {
                // Make sure the request URI is relative
                if (uri.isAbsolute()) {
                    uri = URIUtils.rewriteURI(uri, null, true);
                } else {
                    uri = URIUtils.rewriteURI(uri);
                }
            }
            request.setURI(uri);

        } catch (final URISyntaxException ex) {
            throw new ProtocolException("Invalid URI: " +
                    request.getRequestLine().getUri(), ex);
        }
    }


    // non-javadoc, see interface ClientRequestDirector
    @Override
    public HttpResponse execute(final HttpHost targetHost, final HttpRequest request,
                                final HttpContext context)
        throws HttpException, IOException {

        context.setAttribute(ClientContext.TARGET_AUTH_STATE, targetAuthState);
        context.setAttribute(ClientContext.PROXY_AUTH_STATE, proxyAuthState);

        HttpHost target = targetHost;

        final HttpRequest orig = request;
        final RequestWrapper origWrapper = wrapRequest(orig);
        origWrapper.setParams(params);
        final HttpRoute origRoute = determineRoute(target, origWrapper, context);

        virtualHost = (HttpHost) origWrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);

        // HTTPCLIENT-1092 - add the port if necessary
        if (virtualHost != null && virtualHost.getPort() == -1) {
            final HttpHost host = (target != null) ? target : origRoute.getTargetHost();
            final int port = host.getPort();
            if (port != -1){
                virtualHost = new HttpHost(virtualHost.getHostName(), port, virtualHost.getSchemeName());
            }
        }

        RoutedRequest roureq = new RoutedRequest(origWrapper, origRoute);

        boolean reuse = false;
        boolean done = false;
        try {
            HttpResponse response = null;
            while (!done) {
                // In this loop, the RoutedRequest may be replaced by a
                // followup request and route. The request and route passed
                // in the method arguments will be replaced. The original
                // request is still available in 'orig'.

                final RequestWrapper wrapper = roureq.getRequest();
                final HttpRoute route = roureq.getRoute();
                response = null;

                // See if we have a user token bound to the execution context
                Object userToken = context.getAttribute(ClientContext.USER_TOKEN);

                // Allocate connection if needed
                if (managedConn == null) {
                    final ClientConnectionRequest connRequest = connManager.requestConnection(
                            route, userToken);
                    if (orig instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) orig).setConnectionRequest(connRequest);
                    }

                    final long timeout = HttpClientParams.getConnectionManagerTimeout(params);
                    try {
                        managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
                    } catch(final InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException();
                    }

                    if (HttpConnectionParams.isStaleCheckingEnabled(params)) {
                        // validate connection
                        if (managedConn.isOpen()) {
                            this.log.debug("Stale connection check");
                            if (managedConn.isStale()) {
                                this.log.debug("Stale connection detected");
                                managedConn.close();
                            }
                        }
                    }
                }

                if (orig instanceof AbortableHttpRequest) {
                    ((AbortableHttpRequest) orig).setReleaseTrigger(managedConn);
                }

                try {
                    tryConnect(roureq, context);
                } catch (final TunnelRefusedException ex) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug(ex.getMessage());
                    }
                    response = ex.getResponse();
                    break;
                }

                final String userinfo = wrapper.getURI().getUserInfo();
                if (userinfo != null) {
                    targetAuthState.update(
                            new BasicScheme(), new UsernamePasswordCredentials(userinfo));
                }

                // Get target.  Even if there's virtual host, we may need the target to set the port.
                if (virtualHost != null) {
                    target = virtualHost;
                } else {
                    final URI requestURI = wrapper.getURI();
                    if (requestURI.isAbsolute()) {
                        target = URIUtils.extractHost(requestURI);
                    }
                }
                if (target == null) {
                    target = route.getTargetHost();
                }

                // Reset headers on the request wrapper
                wrapper.resetHeaders();
                // Re-write request URI if needed
                rewriteRequestURI(wrapper, route);

                // Populate the execution context
                context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, target);
                context.setAttribute(ClientContext.ROUTE, route);
                context.setAttribute(ExecutionContext.HTTP_CONNECTION, managedConn);

                // Run request protocol interceptors
                requestExec.preProcess(wrapper, httpProcessor, context);

                response = tryExecute(roureq, context);
                if (response == null) {
                    // Need to start over
                    continue;
                }

                // Run response protocol interceptors
                response.setParams(params);
                requestExec.postProcess(response, httpProcessor, context);


                // The connection is in or can be brought to a re-usable state.
                reuse = reuseStrategy.keepAlive(response, context);
                if (reuse) {
                    // Set the idle duration of this connection
                    final long duration = keepAliveStrategy.getKeepAliveDuration(response, context);
                    if (this.log.isDebugEnabled()) {
                        final String s;
                        if (duration > 0) {
                            s = "for " + duration + " " + TimeUnit.MILLISECONDS;
                        } else {
                            s = "indefinitely";
                        }
                        this.log.debug("Connection can be kept alive " + s);
                    }
                    managedConn.setIdleDuration(duration, TimeUnit.MILLISECONDS);
                }

                final RoutedRequest followup = handleResponse(roureq, response, context);
                if (followup == null) {
                    done = true;
                } else {
                    if (reuse) {
                        // Make sure the response body is fully consumed, if present
                        final HttpEntity entity = response.getEntity();
                        EntityUtils.consume(entity);
                        // entity consumed above is not an auto-release entity,
                        // need to mark the connection re-usable explicitly
                        managedConn.markReusable();
                    } else {
                        managedConn.close();
                        if (proxyAuthState.getState().compareTo(AuthProtocolState.CHALLENGED) > 0
                                && proxyAuthState.getAuthScheme() != null
                                && proxyAuthState.getAuthScheme().isConnectionBased()) {
                            this.log.debug("Resetting proxy auth state");
                            proxyAuthState.reset();
                        }
                        if (targetAuthState.getState().compareTo(AuthProtocolState.CHALLENGED) > 0
                                && targetAuthState.getAuthScheme() != null
                                && targetAuthState.getAuthScheme().isConnectionBased()) {
                            this.log.debug("Resetting target auth state");
                            targetAuthState.reset();
                        }
                    }
                    // check if we can use the same connection for the followup
                    if (!followup.getRoute().equals(roureq.getRoute())) {
                        releaseConnection();
                    }
                    roureq = followup;
                }

                if (managedConn != null) {
                    if (userToken == null) {
                        userToken = userTokenHandler.getUserToken(context);
                        context.setAttribute(ClientContext.USER_TOKEN, userToken);
                    }
                    if (userToken != null) {
                        managedConn.setState(userToken);
                    }
                }

            } // while not done


            // check for entity, release connection if possible
            if ((response == null) || (response.getEntity() == null) ||
                !response.getEntity().isStreaming()) {
                // connection not needed and (assumed to be) in re-usable state
                if (reuse) {
                    managedConn.markReusable();
                }
                releaseConnection();
            } else {
                // install an auto-release entity
                HttpEntity entity = response.getEntity();
                entity = new BasicManagedEntity(entity, managedConn, reuse);
                response.setEntity(entity);
            }

            return response;

        } catch (final ConnectionShutdownException ex) {
            final InterruptedIOException ioex = new InterruptedIOException(
                    "Connection has been shut down");
            ioex.initCause(ex);
            throw ioex;
        } catch (final HttpException ex) {
            abortConnection();
            throw ex;
        } catch (final IOException ex) {
            abortConnection();
            throw ex;
        } catch (final RuntimeException ex) {
            abortConnection();
            throw ex;
        }
    } // execute

    /**
     * Establish connection either directly or through a tunnel and retry in case of
     * a recoverable I/O failure
     */
    private void tryConnect(
            final RoutedRequest req, final HttpContext context) throws HttpException, IOException {
        final HttpRoute route = req.getRoute();
        final HttpRequest wrapper = req.getRequest();

        int connectCount = 0;
        for (;;) {
            context.setAttribute(ExecutionContext.HTTP_REQUEST, wrapper);
            // Increment connect count
            connectCount++;
            try {
                if (!managedConn.isOpen()) {
                    managedConn.open(route, context, params);
                } else {
                    managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(params));
                }
                establishRoute(route, context);
                break;
            } catch (final IOException ex) {
                try {
                    managedConn.close();
                } catch (final IOException ignore) {
                }
                if (retryHandler.retryRequest(ex, connectCount, context)) {
                    if (this.log.isInfoEnabled()) {
                        this.log.info("I/O exception ("+ ex.getClass().getName() +
                                ") caught when connecting to "
                                + route +
                                ": "
                                + ex.getMessage());
                        if (this.log.isDebugEnabled()) {
                            this.log.debug(ex.getMessage(), ex);
                        }
                        this.log.info("Retrying connect to " + route);
                    }
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * Execute request and retry in case of a recoverable I/O failure
     */
    private HttpResponse tryExecute(
            final RoutedRequest req, final HttpContext context) throws HttpException, IOException {
        final RequestWrapper wrapper = req.getRequest();
        final HttpRoute route = req.getRoute();
        HttpResponse response = null;

        Exception retryReason = null;
        for (;;) {
            // Increment total exec count (with redirects)
            execCount++;
            // Increment exec count for this particular request
            wrapper.incrementExecCount();
            if (!wrapper.isRepeatable()) {
                this.log.debug("Cannot retry non-repeatable request");
                if (retryReason != null) {
                    throw new NonRepeatableRequestException("Cannot retry request " +
                        "with a non-repeatable request entity.  The cause lists the " +
                        "reason the original request failed.", retryReason);
                } else {
                    throw new NonRepeatableRequestException("Cannot retry request " +
                            "with a non-repeatable request entity.");
                }
            }

            try {
                if (!managedConn.isOpen()) {
                    // If we have a direct route to the target host
                    // just re-open connection and re-try the request
                    if (!route.isTunnelled()) {
                        this.log.debug("Reopening the direct connection.");
                        managedConn.open(route, context, params);
                    } else {
                        // otherwise give up
                        this.log.debug("Proxied connection. Need to start over.");
                        break;
                    }
                }

                if (this.log.isDebugEnabled()) {
                    this.log.debug("Attempt " + execCount + " to execute request");
                }
                response = requestExec.execute(wrapper, managedConn, context);
                break;

            } catch (final IOException ex) {
                this.log.debug("Closing the connection.");
                try {
                    managedConn.close();
                } catch (final IOException ignore) {
                }
                if (retryHandler.retryRequest(ex, wrapper.getExecCount(), context)) {
                    if (this.log.isInfoEnabled()) {
                        this.log.info("I/O exception ("+ ex.getClass().getName() +
                                ") caught when processing request to "
                                + route +
                                ": "
                                + ex.getMessage());
                    }
                    if (this.log.isDebugEnabled()) {
                        this.log.debug(ex.getMessage(), ex);
                    }
                    if (this.log.isInfoEnabled()) {
                        this.log.info("Retrying request to " + route);
                    }
                    retryReason = ex;
                } else {
                    if (ex instanceof NoHttpResponseException) {
                        final NoHttpResponseException updatedex = new NoHttpResponseException(
                                route.getTargetHost().toHostString() + " failed to respond");
                        updatedex.setStackTrace(ex.getStackTrace());
                        throw updatedex;
                    } else {
                        throw ex;
                    }
                }
            }
        }
        return response;
    }

    /**
     * Returns the connection back to the connection manager
     * and prepares for retrieving a new connection during
     * the next request.
     */
    protected void releaseConnection() {
        // Release the connection through the ManagedConnection instead of the
        // ConnectionManager directly.  This lets the connection control how
        // it is released.
        try {
            managedConn.releaseConnection();
        } catch(final IOException ignored) {
            this.log.debug("IOException releasing connection", ignored);
        }
        managedConn = null;
    }

    /**
     * Determines the route for a request.
     * Called by {@link #execute}
     * to determine the route for either the original or a followup request.
     *
     * @param targetHost  the target host for the request.
     *                  Implementations may accept {@code null}
     *                  if they can still determine a route, for example
     *                  to a default target or by inspecting the request.
     * @param request   the request to execute
     * @param context   the context to use for the execution,
     *                  never {@code null}
     *
     * @return  the route the request should take
     *
     * @throws HttpException    in case of a problem
     */
    protected HttpRoute determineRoute(final HttpHost targetHost,
                                           final HttpRequest request,
                                           final HttpContext context)
        throws HttpException {
        return this.routePlanner.determineRoute(
                targetHost != null ? targetHost : (HttpHost) request.getParams()
                        .getParameter(ClientPNames.DEFAULT_HOST),
                request, context);
    }


    /**
     * Establishes the target route.
     *
     * @param route     the route to establish
     * @param context   the context for the request execution
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected void establishRoute(final HttpRoute route, final HttpContext context)
        throws HttpException, IOException {

        final HttpRouteDirector rowdy = new BasicRouteDirector();
        int step;
        do {
            final HttpRoute fact = managedConn.getRoute();
            step = rowdy.nextStep(route, fact);

            switch (step) {

            case HttpRouteDirector.CONNECT_TARGET:
            case HttpRouteDirector.CONNECT_PROXY:
                managedConn.open(route, context, this.params);
                break;

            case HttpRouteDirector.TUNNEL_TARGET: {
                final boolean secure = createTunnelToTarget(route, context);
                this.log.debug("Tunnel to target created.");
                managedConn.tunnelTarget(secure, this.params);
            }   break;

            case HttpRouteDirector.TUNNEL_PROXY: {
                // The most simple example for this case is a proxy chain
                // of two proxies, where P1 must be tunnelled to P2.
                // route: Source -> P1 -> P2 -> Target (3 hops)
                // fact:  Source -> P1 -> Target       (2 hops)
                final int hop = fact.getHopCount()-1; // the hop to establish
                final boolean secure = createTunnelToProxy(route, hop, context);
                this.log.debug("Tunnel to proxy created.");
                managedConn.tunnelProxy(route.getHopTarget(hop),
                                        secure, this.params);
            }   break;


            case HttpRouteDirector.LAYER_PROTOCOL:
                managedConn.layerProtocol(context, this.params);
                break;

            case HttpRouteDirector.UNREACHABLE:
                throw new HttpException("Unable to establish route: " +
                        "planned = " + route + "; current = " + fact);
            case HttpRouteDirector.COMPLETE:
                // do nothing
                break;
            default:
                throw new IllegalStateException("Unknown step indicator "
                        + step + " from RouteDirector.");
            }

        } while (step > HttpRouteDirector.COMPLETE);

    } // establishConnection


    /**
     * Creates a tunnel to the target server.
     * The connection must be established to the (last) proxy.
     * A CONNECT request for tunnelling through the proxy will
     * be created and sent, the response received and checked.
     * This method does <i>not</i> update the connection with
     * information about the tunnel, that is left to the caller.
     *
     * @param route     the route to establish
     * @param context   the context for request execution
     *
     * @return  {@code true} if the tunnelled route is secure,
     *          {@code false} otherwise.
     *          The implementation here always returns {@code false},
     *          but derived classes may override.
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected boolean createTunnelToTarget(final HttpRoute route,
                                           final HttpContext context)
        throws HttpException, IOException {

        final HttpHost proxy = route.getProxyHost();
        final HttpHost target = route.getTargetHost();
        HttpResponse response = null;

        for (;;) {
            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, context, this.params);
            }

            final HttpRequest connect = createConnectRequest(route, context);
            connect.setParams(this.params);

            // Populate the execution context
            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, target);
            context.setAttribute(ClientContext.ROUTE, route);
            context.setAttribute(ExecutionContext.HTTP_PROXY_HOST, proxy);
            context.setAttribute(ExecutionContext.HTTP_CONNECTION, managedConn);
            context.setAttribute(ExecutionContext.HTTP_REQUEST, connect);

            this.requestExec.preProcess(connect, this.httpProcessor, context);

            response = this.requestExec.execute(connect, this.managedConn, context);

            response.setParams(this.params);
            this.requestExec.postProcess(response, this.httpProcessor, context);

            final int status = response.getStatusLine().getStatusCode();
            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " +
                        response.getStatusLine());
            }

            if (HttpClientParams.isAuthenticating(this.params)) {
                if (this.authenticator.isAuthenticationRequested(proxy, response,
                        this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    if (this.authenticator.authenticate(proxy, response,
                            this.proxyAuthStrategy, this.proxyAuthState, context)) {
                        // Retry request
                        if (this.reuseStrategy.keepAlive(response, context)) {
                            this.log.debug("Connection kept alive");
                            // Consume response content
                            final HttpEntity entity = response.getEntity();
                            EntityUtils.consume(entity);
                        } else {
                            this.managedConn.close();
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        final int status = response.getStatusLine().getStatusCode();

        if (status > 299) {

            // Buffer response content
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntity(entity));
            }

            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " +
                    response.getStatusLine(), response);
        }

        this.managedConn.markReusable();

        // How to decide on security of the tunnelled connection?
        // The socket factory knows only about the segment to the proxy.
        // Even if that is secure, the hop to the target may be insecure.
        // Leave it to derived classes, consider insecure by default here.
        return false;

    } // createTunnelToTarget



    /**
     * Creates a tunnel to an intermediate proxy.
     * This method is <i>not</i> implemented in this class.
     * It just throws an exception here.
     *
     * @param route     the route to establish
     * @param hop       the hop in the route to establish now.
     *                  {@code route.getHopTarget(hop)}
     *                  will return the proxy to tunnel to.
     * @param context   the context for request execution
     *
     * @return  {@code true} if the partially tunnelled connection
     *          is secure, {@code false} otherwise.
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected boolean createTunnelToProxy(final HttpRoute route, final int hop,
                                          final HttpContext context)
        throws HttpException, IOException {

        // Have a look at createTunnelToTarget and replicate the parts
        // you need in a custom derived class. If your proxies don't require
        // authentication, it is not too hard. But for the stock version of
        // HttpClient, we cannot make such simplifying assumptions and would
        // have to include proxy authentication code. The HttpComponents team
        // is currently not in a position to support rarely used code of this
        // complexity. Feel free to submit patches that refactor the code in
        // createTunnelToTarget to facilitate re-use for proxy tunnelling.

        throw new HttpException("Proxy chains are not supported.");
    }



    /**
     * Creates the CONNECT request for tunnelling.
     * Called by {@link #createTunnelToTarget createTunnelToTarget}.
     *
     * @param route     the route to establish
     * @param context   the context for request execution
     *
     * @return  the CONNECT request for tunnelling
     */
    protected HttpRequest createConnectRequest(final HttpRoute route,
                                               final HttpContext context) {
        // see RFC 2817, section 5.2 and
        // INTERNET-DRAFT: Tunneling TCP based protocols through
        // Web proxy servers

        final HttpHost target = route.getTargetHost();

        final String host = target.getHostName();
        int port = target.getPort();
        if (port < 0) {
            final Scheme scheme = connManager.getSchemeRegistry().
                getScheme(target.getSchemeName());
            port = scheme.getDefaultPort();
        }

        final StringBuilder buffer = new StringBuilder(host.length() + 6);
        buffer.append(host);
        buffer.append(':');
        buffer.append(Integer.toString(port));

        final String authority = buffer.toString();
        final ProtocolVersion ver = HttpProtocolParams.getVersion(params);
        final HttpRequest req = new BasicHttpRequest
            ("CONNECT", authority, ver);

        return req;
    }


    /**
     * Analyzes a response to check need for a followup.
     *
     * @param roureq    the request and route.
     * @param response  the response to analayze
     * @param context   the context used for the current request execution
     *
     * @return  the followup request and route if there is a followup, or
     *          {@code null} if the response should be returned as is
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected RoutedRequest handleResponse(final RoutedRequest roureq,
                                           final HttpResponse response,
                                           final HttpContext context)
        throws HttpException, IOException {

        final HttpRoute route = roureq.getRoute();
        final RequestWrapper request = roureq.getRequest();

        final HttpParams params = request.getParams();

        if (HttpClientParams.isAuthenticating(params)) {
            HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            if (target == null) {
                target = route.getTargetHost();
            }
            if (target.getPort() < 0) {
                final Scheme scheme = connManager.getSchemeRegistry().getScheme(target);
                target = new HttpHost(target.getHostName(), scheme.getDefaultPort(), target.getSchemeName());
            }

            final boolean targetAuthRequested = this.authenticator.isAuthenticationRequested(
                    target, response, this.targetAuthStrategy, targetAuthState, context);

            HttpHost proxy = route.getProxyHost();
            // if proxy is not set use target host instead
            if (proxy == null) {
                proxy = route.getTargetHost();
            }
            final boolean proxyAuthRequested = this.authenticator.isAuthenticationRequested(
                    proxy, response, this.proxyAuthStrategy, proxyAuthState, context);

            if (targetAuthRequested) {
                if (this.authenticator.authenticate(target, response,
                        this.targetAuthStrategy, this.targetAuthState, context)) {
                    // Re-try the same request via the same route
                    return roureq;
                }
            }
            if (proxyAuthRequested) {
                if (this.authenticator.authenticate(proxy, response,
                        this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    // Re-try the same request via the same route
                    return roureq;
                }
            }
        }

        if (HttpClientParams.isRedirecting(params) &&
                this.redirectStrategy.isRedirected(request, response, context)) {

            if (redirectCount >= maxRedirects) {
                throw new RedirectException("Maximum redirects ("
                        + maxRedirects + ") exceeded");
            }
            redirectCount++;

            // Virtual host cannot be used any longer
            virtualHost = null;

            final HttpUriRequest redirect = redirectStrategy.getRedirect(request, response, context);
            final HttpRequest orig = request.getOriginal();
            redirect.setHeaders(orig.getAllHeaders());

            final URI uri = redirect.getURI();
            final HttpHost newTarget = URIUtils.extractHost(uri);
            if (newTarget == null) {
                throw new ProtocolException("Redirect URI does not specify a valid host name: " + uri);
            }

            // Reset auth states if redirecting to another host
            if (!route.getTargetHost().equals(newTarget)) {
                this.log.debug("Resetting target auth state");
                targetAuthState.reset();
                final AuthScheme authScheme = proxyAuthState.getAuthScheme();
                if (authScheme != null && authScheme.isConnectionBased()) {
                    this.log.debug("Resetting proxy auth state");
                    proxyAuthState.reset();
                }
            }

            final RequestWrapper wrapper = wrapRequest(redirect);
            wrapper.setParams(params);

            final HttpRoute newRoute = determineRoute(newTarget, wrapper, context);
            final RoutedRequest newRequest = new RoutedRequest(wrapper, newRoute);

            if (this.log.isDebugEnabled()) {
                this.log.debug("Redirecting to '" + uri + "' via " + newRoute);
            }

            return newRequest;
        }

        return null;
    } // handleResponse


    /**
     * Shuts down the connection.
     * This method is called from a {@code catch} block in
     * {@link #execute execute} during exception handling.
     */
    private void abortConnection() {
        final ManagedClientConnection mcc = managedConn;
        if (mcc != null) {
            // we got here as the result of an exception
            // no response will be returned, release the connection
            managedConn = null;
            try {
                mcc.abortConnection();
            } catch (final IOException ex) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(ex.getMessage(), ex);
                }
            }
            // ensure the connection manager properly releases this connection
            try {
                mcc.releaseConnection();
            } catch(final IOException ignored) {
                this.log.debug("Error releasing connection", ignored);
            }
        }
    } // abortConnection


} // class DefaultClientRequestDirector
