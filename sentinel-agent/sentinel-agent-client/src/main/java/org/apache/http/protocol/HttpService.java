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

package org.apache.http.protocol;

import java.io.IOException;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ProtocolException;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

/**
 * {@code HttpService} is a server side HTTP protocol handler based on
 * the classic (blocking) I/O model.
 * <p>
 * {@code HttpService} relies on {@link HttpProcessor} to generate mandatory
 * protocol headers for all outgoing messages and apply common, cross-cutting
 * message transformations to all incoming and outgoing messages, whereas
 * individual {@link HttpRequestHandler}s are expected to implement
 * application specific content generation and processing.
 * <p>
 * {@code HttpService} uses {@link HttpRequestHandlerMapper} to map
 * matching request handler for a particular request URI of an incoming HTTP
 * request.
 * <p>
 * {@code HttpService} can use optional {@link HttpExpectationVerifier}
 * to ensure that incoming requests meet server's expectations.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpService {

    /**
     * TODO: make all variables final in the next major version
     */
    private volatile HttpParams params = null;
    private volatile HttpProcessor processor = null;
    private volatile HttpRequestHandlerMapper handlerMapper = null;
    private volatile ConnectionReuseStrategy connStrategy = null;
    private volatile HttpResponseFactory responseFactory = null;
    private volatile HttpExpectationVerifier expectationVerifier = null;

    /**
     * Create a new HTTP service.
     *
     * @param processor            the processor to use on requests and responses
     * @param connStrategy         the connection reuse strategy
     * @param responseFactory      the response factory
     * @param handlerResolver      the handler resolver. May be null.
     * @param expectationVerifier  the expectation verifier. May be null.
     * @param params               the HTTP parameters
     *
     * @since 4.1
     * @deprecated (4.3) use {@link HttpService#HttpService(HttpProcessor, ConnectionReuseStrategy,
     *   HttpResponseFactory, HttpRequestHandlerMapper, HttpExpectationVerifier)}
     */
    @Deprecated
    public HttpService(
            final HttpProcessor processor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final HttpRequestHandlerResolver handlerResolver,
            final HttpExpectationVerifier expectationVerifier,
            final HttpParams params) {
        this(processor,
             connStrategy,
             responseFactory,
             new HttpRequestHandlerResolverAdapter(handlerResolver),
             expectationVerifier);
        this.params = params;
    }

    /**
     * Create a new HTTP service.
     *
     * @param processor            the processor to use on requests and responses
     * @param connStrategy         the connection reuse strategy
     * @param responseFactory      the response factory
     * @param handlerResolver      the handler resolver. May be null.
     * @param params               the HTTP parameters
     *
     * @since 4.1
     * @deprecated (4.3) use {@link HttpService#HttpService(HttpProcessor, ConnectionReuseStrategy,
     *   HttpResponseFactory, HttpRequestHandlerMapper)}
     */
    @Deprecated
    public HttpService(
            final HttpProcessor processor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final HttpRequestHandlerResolver handlerResolver,
            final HttpParams params) {
        this(processor,
             connStrategy,
             responseFactory,
             new HttpRequestHandlerResolverAdapter(handlerResolver),
             null);
        this.params = params;
    }

    /**
     * Create a new HTTP service.
     *
     * @param proc             the processor to use on requests and responses
     * @param connStrategy     the connection reuse strategy
     * @param responseFactory  the response factory
     *
     * @deprecated (4.1) use {@link HttpService#HttpService(HttpProcessor,
     *  ConnectionReuseStrategy, HttpResponseFactory, HttpRequestHandlerResolver, HttpParams)}
     */
    @Deprecated
    public HttpService(
            final HttpProcessor proc,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory) {
        super();
        setHttpProcessor(proc);
        setConnReuseStrategy(connStrategy);
        setResponseFactory(responseFactory);
    }

    /**
     * Create a new HTTP service.
     *
     * @param processor the processor to use on requests and responses
     * @param connStrategy the connection reuse strategy. If {@code null}
     *   {@link DefaultConnectionReuseStrategy#INSTANCE} will be used.
     * @param responseFactory  the response factory. If {@code null}
     *   {@link DefaultHttpResponseFactory#INSTANCE} will be used.
     * @param handlerMapper  the handler mapper. May be null.
     * @param expectationVerifier the expectation verifier. May be null.
     *
     * @since 4.3
     */
    public HttpService(
            final HttpProcessor processor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final HttpRequestHandlerMapper handlerMapper,
            final HttpExpectationVerifier expectationVerifier) {
        super();
        this.processor =  Args.notNull(processor, "HTTP processor");
        this.connStrategy = connStrategy != null ? connStrategy :
            DefaultConnectionReuseStrategy.INSTANCE;
        this.responseFactory = responseFactory != null ? responseFactory :
            DefaultHttpResponseFactory.INSTANCE;
        this.handlerMapper = handlerMapper;
        this.expectationVerifier = expectationVerifier;
    }

    /**
     * Create a new HTTP service.
     *
     * @param processor the processor to use on requests and responses
     * @param connStrategy the connection reuse strategy. If {@code null}
     *   {@link DefaultConnectionReuseStrategy#INSTANCE} will be used.
     * @param responseFactory  the response factory. If {@code null}
     *   {@link DefaultHttpResponseFactory#INSTANCE} will be used.
     * @param handlerMapper  the handler mapper. May be null.
     *
     * @since 4.3
     */
    public HttpService(
            final HttpProcessor processor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final HttpRequestHandlerMapper handlerMapper) {
        this(processor, connStrategy, responseFactory, handlerMapper, null);
    }

    /**
     * Create a new HTTP service.
     *
     * @param processor the processor to use on requests and responses
     * @param handlerMapper  the handler mapper. May be null.
     *
     * @since 4.3
     */
    public HttpService(
            final HttpProcessor processor, final HttpRequestHandlerMapper handlerMapper) {
        this(processor, null, null, handlerMapper, null);
    }

    /**
     * @deprecated (4.1) set {@link HttpProcessor} using constructor
     */
    @Deprecated
    public void setHttpProcessor(final HttpProcessor processor) {
        Args.notNull(processor, "HTTP processor");
        this.processor = processor;
    }

    /**
     * @deprecated (4.1) set {@link ConnectionReuseStrategy} using constructor
     */
    @Deprecated
    public void setConnReuseStrategy(final ConnectionReuseStrategy connStrategy) {
        Args.notNull(connStrategy, "Connection reuse strategy");
        this.connStrategy = connStrategy;
    }

    /**
     * @deprecated (4.1) set {@link HttpResponseFactory} using constructor
     */
    @Deprecated
    public void setResponseFactory(final HttpResponseFactory responseFactory) {
        Args.notNull(responseFactory, "Response factory");
        this.responseFactory = responseFactory;
    }

    /**
     * @deprecated (4.1) set {@link HttpResponseFactory} using constructor
     */
    @Deprecated
    public void setParams(final HttpParams params) {
        this.params = params;
    }

    /**
     * @deprecated (4.1) set {@link HttpRequestHandlerResolver} using constructor
     */
    @Deprecated
    public void setHandlerResolver(final HttpRequestHandlerResolver handlerResolver) {
        this.handlerMapper = new HttpRequestHandlerResolverAdapter(handlerResolver);
    }

    /**
     * @deprecated (4.1) set {@link HttpExpectationVerifier} using constructor
     */
    @Deprecated
    public void setExpectationVerifier(final HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
    }

    /**
     * @deprecated (4.3) no longer used.
     */
    @Deprecated
    public HttpParams getParams() {
        return this.params;
    }

    /**
     * Handles receives one HTTP request over the given connection within the
     * given execution context and sends a response back to the client.
     *
     * @param conn the active connection to the client
     * @param context the actual execution context.
     * @throws IOException in case of an I/O error.
     * @throws HttpException in case of HTTP protocol violation or a processing
     *   problem.
     */
    public void handleRequest(
            final HttpServerConnection conn,
            final HttpContext context) throws IOException, HttpException {

        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, conn);

        HttpRequest request = null;
        HttpResponse response = null;

        try {
            request = conn.receiveRequestHeader();
            if (request instanceof HttpEntityEnclosingRequest) {

                if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                    response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_1,
                            HttpStatus.SC_CONTINUE, context);
                    if (this.expectationVerifier != null) {
                        try {
                            this.expectationVerifier.verify(request, response, context);
                        } catch (final HttpException ex) {
                            response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0,
                                    HttpStatus.SC_INTERNAL_SERVER_ERROR, context);
                            handleException(ex, response);
                        }
                    }
                    if (response.getStatusLine().getStatusCode() < 200) {
                        // Send 1xx response indicating the server expections
                        // have been met
                        conn.sendResponseHeader(response);
                        conn.flush();
                        response = null;
                        conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                    }
                } else {
                    conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                }
            }

            context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);

            if (response == null) {
                response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_1,
                        HttpStatus.SC_OK, context);
                this.processor.process(request, context);
                doService(request, response, context);
            }

            // Make sure the request content is fully consumed
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
                EntityUtils.consume(entity);
            }

        } catch (final HttpException ex) {
            response = this.responseFactory.newHttpResponse
                (HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                 context);
            handleException(ex, response);
        }

        context.setAttribute(HttpCoreContext.HTTP_RESPONSE, response);

        this.processor.process(response, context);
        conn.sendResponseHeader(response);
        if (canResponseHaveBody(request, response)) {
            conn.sendResponseEntity(response);
        }
        conn.flush();

        if (!this.connStrategy.keepAlive(response, context)) {
            conn.close();
        }
    }

    private boolean canResponseHaveBody(final HttpRequest request, final HttpResponse response) {
        if (request != null && "HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        final int status = response.getStatusLine().getStatusCode();
        return status >= HttpStatus.SC_OK
                && status != HttpStatus.SC_NO_CONTENT
                && status != HttpStatus.SC_NOT_MODIFIED
                && status != HttpStatus.SC_RESET_CONTENT;
    }

    /**
     * Handles the given exception and generates an HTTP response to be sent
     * back to the client to inform about the exceptional condition encountered
     * in the course of the request processing.
     *
     * @param ex the exception.
     * @param response the HTTP response.
     */
    protected void handleException(final HttpException ex, final HttpResponse response) {
        if (ex instanceof MethodNotSupportedException) {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else if (ex instanceof UnsupportedHttpVersionException) {
            response.setStatusCode(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED);
        } else if (ex instanceof ProtocolException) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        String message = ex.getMessage();
        if (message == null) {
            message = ex.toString();
        }
        final byte[] msg = EncodingUtils.getAsciiBytes(message);
        final ByteArrayEntity entity = new ByteArrayEntity(msg);
        entity.setContentType("text/plain; charset=US-ASCII");
        response.setEntity(entity);
    }

    /**
     * The default implementation of this method attempts to resolve an
     * {@link HttpRequestHandler} for the request URI of the given request
     * and, if found, executes its
     * {@link HttpRequestHandler#handle(HttpRequest, HttpResponse, HttpContext)}
     * method.
     * <p>
     * Super-classes can override this method in order to provide a custom
     * implementation of the request processing logic.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param context the execution context.
     * @throws IOException in case of an I/O error.
     * @throws HttpException in case of HTTP protocol violation or a processing
     *   problem.
     */
    protected void doService(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        HttpRequestHandler handler = null;
        if (this.handlerMapper != null) {
            handler = this.handlerMapper.lookup(request);
        }
        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }
    }

    /**
     * Adaptor class to transition from HttpRequestHandlerResolver to HttpRequestHandlerMapper.
     */
    @Deprecated
    private static class HttpRequestHandlerResolverAdapter implements HttpRequestHandlerMapper {

        private final HttpRequestHandlerResolver resolver;

        public HttpRequestHandlerResolverAdapter(final HttpRequestHandlerResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public HttpRequestHandler lookup(final HttpRequest request) {
            return resolver.lookup(request.getRequestLine().getUri());
        }

    }

}
