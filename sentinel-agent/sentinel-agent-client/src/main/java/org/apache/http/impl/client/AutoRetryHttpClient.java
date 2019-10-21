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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

/**
 * {@link HttpClient} implementation that can automatically retry the request in case of
 * a non-2xx response using the {@link ServiceUnavailableRetryStrategy} interface.
 *
 * @since 4.2
 *
 * @deprecated (4.3) use {@link HttpClientBuilder}.
 */
@Deprecated
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class AutoRetryHttpClient implements HttpClient {

    private final HttpClient backend;

    private final ServiceUnavailableRetryStrategy retryStrategy;

    private final Log log = LogFactory.getLog(getClass());

    public AutoRetryHttpClient(
            final HttpClient client, final ServiceUnavailableRetryStrategy retryStrategy) {
        super();
        Args.notNull(client, "HttpClient");
        Args.notNull(retryStrategy, "ServiceUnavailableRetryStrategy");
        this.backend = client;
        this.retryStrategy = retryStrategy;
    }

    /**
     * Constructs a {@code AutoRetryHttpClient} with default caching settings that
     * stores cache entries in memory and uses a vanilla
     * {@link DefaultHttpClient} for backend requests.
     */
    public AutoRetryHttpClient() {
        this(new DefaultHttpClient(), new DefaultServiceUnavailableRetryStrategy());
    }

    /**
     * Constructs a {@code AutoRetryHttpClient} with the given caching options that
     * stores cache entries in memory and uses a vanilla
     * {@link DefaultHttpClient} for backend requests.
     *
     * @param config
     *            retry configuration module options
     */
    public AutoRetryHttpClient(final ServiceUnavailableRetryStrategy config) {
        this(new DefaultHttpClient(), config);
    }

    /**
     * Constructs a {@code AutoRetryHttpClient} with default caching settings that
     * stores cache entries in memory and uses the given {@link HttpClient} for
     * backend requests.
     *
     * @param client
     *            used to make origin requests
     */
    public AutoRetryHttpClient(final HttpClient client) {
        this(client, new DefaultServiceUnavailableRetryStrategy());
    }

    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request)
            throws IOException {
        final HttpContext defaultContext = null;
        return execute(target, request, defaultContext);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(target, request, responseHandler, null);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException {
        final HttpResponse resp = execute(target, request, context);
        return responseHandler.handleResponse(resp);
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request) throws IOException {
        final HttpContext context = null;
        return execute(request, context);
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request, final HttpContext context)
            throws IOException {
        final URI uri = request.getURI();
        final HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(),
                uri.getScheme());
        return execute(httpHost, request, context);
    }

    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(request, responseHandler, null);
    }

    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException {
        final HttpResponse resp = execute(request, context);
        return responseHandler.handleResponse(resp);
    }

    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request,
            final HttpContext context) throws IOException {
        for (int c = 1;; c++) {
            final HttpResponse response = backend.execute(target, request, context);
            try {
                if (retryStrategy.retryRequest(response, c, context)) {
                    EntityUtils.consume(response.getEntity());
                    final long nextInterval = retryStrategy.getRetryInterval();
                    try {
                        log.trace("Wait for " + nextInterval);
                        Thread.sleep(nextInterval);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException();
                    }
                } else {
                    return response;
                }
            } catch (final RuntimeException ex) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (final IOException ioex) {
                    log.warn("I/O error consuming response content", ioex);
                }
                throw ex;
            }
        }
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }

    @Override
    public HttpParams getParams() {
        return backend.getParams();
    }

}
