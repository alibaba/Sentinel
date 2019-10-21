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

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;

/**
 * Builder for {@link HttpProcessor} instances.
 *
 * @since 4.3
 */
public class HttpProcessorBuilder {

    private ChainBuilder<HttpRequestInterceptor> requestChainBuilder;
    private ChainBuilder<HttpResponseInterceptor> responseChainBuilder;

    public static HttpProcessorBuilder create() {
        return new HttpProcessorBuilder();
    }

    HttpProcessorBuilder() {
        super();
    }

    private ChainBuilder<HttpRequestInterceptor> getRequestChainBuilder() {
        if (requestChainBuilder == null) {
            requestChainBuilder = new ChainBuilder<HttpRequestInterceptor>();
        }
        return requestChainBuilder;
    }

    private ChainBuilder<HttpResponseInterceptor> getResponseChainBuilder() {
        if (responseChainBuilder == null) {
            responseChainBuilder = new ChainBuilder<HttpResponseInterceptor>();
        }
        return responseChainBuilder;
    }

    public HttpProcessorBuilder addFirst(final HttpRequestInterceptor e) {
        if (e == null) {
            return this;
        }
        getRequestChainBuilder().addFirst(e);
        return this;
    }

    public HttpProcessorBuilder addLast(final HttpRequestInterceptor e) {
        if (e == null) {
            return this;
        }
        getRequestChainBuilder().addLast(e);
        return this;
    }

    public HttpProcessorBuilder add(final HttpRequestInterceptor e) {
        return addLast(e);
    }

    public HttpProcessorBuilder addAllFirst(final HttpRequestInterceptor... e) {
        if (e == null) {
            return this;
        }
        getRequestChainBuilder().addAllFirst(e);
        return this;
    }

    public HttpProcessorBuilder addAllLast(final HttpRequestInterceptor... e) {
        if (e == null) {
            return this;
        }
        getRequestChainBuilder().addAllLast(e);
        return this;
    }

    public HttpProcessorBuilder addAll(final HttpRequestInterceptor... e) {
        return addAllLast(e);
    }

    public HttpProcessorBuilder addFirst(final HttpResponseInterceptor e) {
        if (e == null) {
            return this;
        }
        getResponseChainBuilder().addFirst(e);
        return this;
    }

    public HttpProcessorBuilder addLast(final HttpResponseInterceptor e) {
        if (e == null) {
            return this;
        }
        getResponseChainBuilder().addLast(e);
        return this;
    }

    public HttpProcessorBuilder add(final HttpResponseInterceptor e) {
        return addLast(e);
    }

    public HttpProcessorBuilder addAllFirst(final HttpResponseInterceptor... e) {
        if (e == null) {
            return this;
        }
        getResponseChainBuilder().addAllFirst(e);
        return this;
    }

    public HttpProcessorBuilder addAllLast(final HttpResponseInterceptor... e) {
        if (e == null) {
            return this;
        }
        getResponseChainBuilder().addAllLast(e);
        return this;
    }

    public HttpProcessorBuilder addAll(final HttpResponseInterceptor... e) {
        return addAllLast(e);
    }

    public HttpProcessor build() {
        return new ImmutableHttpProcessor(
                requestChainBuilder != null ? requestChainBuilder.build() : null,
                responseChainBuilder != null ? responseChainBuilder.build() : null);
    }

}
