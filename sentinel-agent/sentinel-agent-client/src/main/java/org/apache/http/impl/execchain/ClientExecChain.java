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

package org.apache.http.impl.execchain;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;

/**
 * This interface represents an element in the HTTP request execution chain. Each element can
 * either be a decorator around another element that implements a cross cutting aspect or
 * a self-contained executor capable of producing a response for the given request.
 * <p>
 * Important: please note it is required for decorators that implement post execution aspects
 * or response post-processing of any sort to release resources associated with the response
 * by calling {@link CloseableHttpResponse#close()} methods in case of an I/O, protocol or
 * runtime exception, or in case the response is not propagated to the caller.
 * </p>
 *
 * @since 4.3
 */
public interface ClientExecChain {

    /**
     * Executes th request either by transmitting it to the target server or
     * by passing it onto the next executor in the request execution chain.
     *
     * @param route connection route.
     * @param request current request.
     * @param clientContext current HTTP context.
     * @param execAware receiver of notifications of blocking I/O operations.
     * @return HTTP response either received from the opposite endpoint
     *   or generated locally.
     * @throws IOException in case of a I/O error.
     *   (this type of exceptions are potentially recoverable).
     * @throws HttpException in case of an HTTP protocol error
     *   (usually this type of exceptions are non-recoverable).
     */
    CloseableHttpResponse execute(
            HttpRoute route,
            HttpRequestWrapper request,
            HttpClientContext clientContext,
            HttpExecutionAware execAware) throws IOException, HttpException;

}
