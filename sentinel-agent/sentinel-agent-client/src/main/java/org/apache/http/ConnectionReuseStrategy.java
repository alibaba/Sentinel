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

package org.apache.http;

import org.apache.http.protocol.HttpContext;

/**
 * Interface for deciding whether a connection can be re-used for
 * subsequent requests and should be kept alive.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 *
 * @since 4.0
 */
public interface ConnectionReuseStrategy {

    /**
     * Decides whether a connection can be kept open after a request.
     * If this method returns {@code false}, the caller MUST
     * close the connection to correctly comply with the HTTP protocol.
     * If it returns {@code true}, the caller SHOULD attempt to
     * keep the connection open for reuse with another request.
     * <p>
     * One can use the HTTP context to retrieve additional objects that
     * may be relevant for the keep-alive strategy: the actual HTTP
     * connection, the original HTTP request, target host if known,
     * number of times the connection has been reused already and so on.
     * </p>
     * <p>
     * If the connection is already closed, {@code false} is returned.
     * The stale connection check MUST NOT be triggered by a
     * connection reuse strategy.
     * </p>
     *
     * @param response
     *          The last response received over that connection.
     * @param context   the context in which the connection is being
     *          used.
     *
     * @return {@code true} if the connection is allowed to be reused, or
     *         {@code false} if it MUST NOT be reused
     */
    boolean keepAlive(HttpResponse response, HttpContext context);

}
