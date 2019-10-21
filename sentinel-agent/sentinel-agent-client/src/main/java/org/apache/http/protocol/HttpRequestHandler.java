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

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * HttpRequestHandler represents a routine for processing of a specific group
 * of HTTP requests. Protocol handlers are designed to take care of protocol
 * specific aspects, whereas individual request handlers are expected to take
 * care of application specific HTTP processing. The main purpose of a request
 * handler is to generate a response object with a content entity to be sent
 * back to the client in response to the given request
 *
 * @since 4.0
 */
public interface HttpRequestHandler {

    /**
     * Handles the request and produces a response to be sent back to
     * the client.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param context the HTTP execution context.
     * @throws IOException in case of an I/O error.
     * @throws HttpException in case of HTTP protocol violation or a processing
     *   problem.
     */
    void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException;

}
