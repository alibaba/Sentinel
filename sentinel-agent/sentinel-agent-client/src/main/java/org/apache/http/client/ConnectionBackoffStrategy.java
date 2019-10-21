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

import org.apache.http.HttpResponse;

/**
 * When managing a dynamic number of connections for a given route, this
 * strategy assesses whether a given request execution outcome should
 * result in a backoff signal or not, based on either examining the
 * {@code Throwable} that resulted or by examining the resulting
 * response (e.g. for its status code).
 *
 * @since 4.2
 *
 */
public interface ConnectionBackoffStrategy {

    /**
     * Determines whether seeing the given {@code Throwable} as
     * a result of request execution should result in a backoff
     * signal.
     * @param t the {@code Throwable} that happened
     * @return {@code true} if a backoff signal should be
     *   given
     */
    boolean shouldBackoff(Throwable t);

    /**
     * Determines whether receiving the given {@link HttpResponse} as
     * a result of request execution should result in a backoff
     * signal. Implementations MUST restrict themselves to examining
     * the response header and MUST NOT consume any of the response
     * body, if any.
     * @param resp the {@code HttpResponse} that was received
     * @return {@code true} if a backoff signal should be
     *   given
     */
    boolean shouldBackoff(HttpResponse resp);
}
