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
package org.apache.http.conn;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * Interface for deciding how long a connection can remain
 * idle before being reused.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 *
 * @since 4.0
 */
public interface ConnectionKeepAliveStrategy {

    /**
     * Returns the duration of time which this connection can be safely kept
     * idle. If the connection is left idle for longer than this period of time,
     * it MUST not reused. A value of 0 or less may be returned to indicate that
     * there is no suitable suggestion.
     *
     * When coupled with a {@link org.apache.http.ConnectionReuseStrategy}, if
     * {@link org.apache.http.ConnectionReuseStrategy#keepAlive(
     *   HttpResponse, HttpContext)} returns true, this allows you to control
     * how long the reuse will last. If keepAlive returns false, this should
     * have no meaningful impact
     *
     * @param response
     *            The last response received over the connection.
     * @param context
     *            the context in which the connection is being used.
     *
     * @return the duration in ms for which it is safe to keep the connection
     *         idle, or &lt;=0 if no suggested duration.
     */
    long getKeepAliveDuration(HttpResponse response, HttpContext context);

}
