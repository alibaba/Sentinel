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

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * A client-side request director.
 * The director decides which steps are necessary to execute a request.
 * It establishes connections and optionally processes redirects and
 * authentication challenges. The director may therefore generate and
 * send a sequence of requests in order to execute one initial request.
 *
 * @since 4.0
 *
 * @deprecated (4.3) No longer used
 */
@Deprecated
public interface RequestDirector {


    /**
     * Executes a request.
     * <p>
     * <b>Note:</b> For the time being, a new director is instantiated for each request.
     * This is the same behavior as for {@code HttpMethodDirector}
     * in HttpClient 3.
     * </p>
     *
     * @param target    the target host for the request.
     *                  Implementations may accept {@code null}
     *                  if they can still determine a route, for example
     *                  to a default target or by inspecting the request.
     * @param request   the request to execute
     * @param context   the context for executing the request
     *
     * @return  the final response to the request.
     *          This is never an intermediate response with status code 1xx.
     *
     * @throws HttpException            in case of a problem
     * @throws IOException              in case of an IO problem
     *                                     or if the connection was aborted
     */
    HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
        throws HttpException, IOException;

}
