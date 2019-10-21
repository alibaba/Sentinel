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
import org.apache.http.protocol.HttpContext;

/**
 * Strategy interface that allows API users to plug in their own logic to
 * control whether or not a retry should automatically be done, how many times
 * it should be retried and so on.
 *
 * @since 4.2
 */
public interface ServiceUnavailableRetryStrategy {

    /**
     * Determines if a method should be retried given the response from the target server.
     *
     * @param response the response from the target server
     * @param executionCount the number of times this method has been
     * unsuccessfully executed
     * @param context the context for the request execution

     * @return {@code true} if the method should be retried, {@code false}
     * otherwise
     */
    boolean retryRequest(HttpResponse response, int executionCount, HttpContext context);

    /**
     * @return The interval between the subsequent auto-retries.
     */
    long getRetryInterval();

}
