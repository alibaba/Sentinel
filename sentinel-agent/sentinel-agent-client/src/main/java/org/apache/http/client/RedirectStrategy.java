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

package org.apache.http.client;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

/**
 * A strategy for determining if an HTTP request should be redirected to
 * a new location in response to an HTTP response received from the target
 * server.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 *
 * @since 4.1
 */
public interface RedirectStrategy {

    /**
     * Determines if a request should be redirected to a new location
     * given the response from the target server.
     *
     * @param request the executed request
     * @param response the response received from the target server
     * @param context the context for the request execution
     *
     * @return {@code true} if the request should be redirected, {@code false}
     * otherwise
     */
    boolean isRedirected(
            HttpRequest request,
            HttpResponse response,
            HttpContext context) throws ProtocolException;

    /**
     * Determines the redirect location given the response from the target
     * server and the current request execution context and generates a new
     * request to be sent to the location.
     *
     * @param request the executed request
     * @param response the response received from the target server
     * @param context the context for the request execution
     *
     * @return redirected request
     */
    HttpUriRequest getRedirect(
            HttpRequest request,
            HttpResponse response,
            HttpContext context) throws ProtocolException;

}
