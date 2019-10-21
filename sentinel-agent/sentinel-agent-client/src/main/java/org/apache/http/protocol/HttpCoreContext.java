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

import org.apache.http.HttpConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.util.Args;

/**
 * Implementation of {@link HttpContext} that provides convenience
 * setters for user assignable attributes and getter for readable attributes.
 *
 * @since 4.3
 */
public class HttpCoreContext implements HttpContext {

    /**
     * Attribute name of a {@link org.apache.http.HttpConnection} object that
     * represents the actual HTTP connection.
     */
    public static final String HTTP_CONNECTION  = "http.connection";

    /**
     * Attribute name of a {@link org.apache.http.HttpRequest} object that
     * represents the actual HTTP request.
     */
    public static final String HTTP_REQUEST     = "http.request";

    /**
     * Attribute name of a {@link org.apache.http.HttpResponse} object that
     * represents the actual HTTP response.
     */
    public static final String HTTP_RESPONSE    = "http.response";

    /**
     * Attribute name of a {@link org.apache.http.HttpHost} object that
     * represents the connection target.
     */
    public static final String HTTP_TARGET_HOST = "http.target_host";

    /**
     * Attribute name of a {@link Boolean} object that represents the
     * the flag indicating whether the actual request has been fully transmitted
     * to the target host.
     */
    public static final String HTTP_REQ_SENT    = "http.request_sent";

    public static HttpCoreContext create() {
        return new HttpCoreContext(new BasicHttpContext());
    }

    public static HttpCoreContext adapt(final HttpContext context) {
        Args.notNull(context, "HTTP context");
        if (context instanceof HttpCoreContext) {
            return (HttpCoreContext) context;
        } else {
            return new HttpCoreContext(context);
        }
    }

    private final HttpContext context;

    public HttpCoreContext(final HttpContext context) {
        super();
        this.context = context;
    }

    public HttpCoreContext() {
        super();
        this.context = new BasicHttpContext();
    }

    @Override
    public Object getAttribute(final String id) {
        return context.getAttribute(id);
    }

    @Override
    public void setAttribute(final String id, final Object obj) {
        context.setAttribute(id, obj);
    }

    @Override
    public Object removeAttribute(final String id) {
        return context.removeAttribute(id);
    }

    public <T> T getAttribute(final String attribname, final Class<T> clazz) {
        Args.notNull(clazz, "Attribute class");
        final Object obj = getAttribute(attribname);
        if (obj == null) {
            return null;
        }
        return clazz.cast(obj);
    }

    public <T extends HttpConnection> T getConnection(final Class<T> clazz) {
        return getAttribute(HTTP_CONNECTION, clazz);
    }

    public HttpConnection getConnection() {
        return getAttribute(HTTP_CONNECTION, HttpConnection.class);
    }

    public HttpRequest getRequest() {
        return getAttribute(HTTP_REQUEST, HttpRequest.class);
    }

    public boolean isRequestSent() {
        final Boolean b = getAttribute(HTTP_REQ_SENT, Boolean.class);
        return b != null && b.booleanValue();
    }

    public HttpResponse getResponse() {
        return getAttribute(HTTP_RESPONSE, HttpResponse.class);
    }

    public void setTargetHost(final HttpHost host) {
        setAttribute(HTTP_TARGET_HOST, host);
    }

    public HttpHost getTargetHost() {
        return getAttribute(HTTP_TARGET_HOST, HttpHost.class);
    }

}
