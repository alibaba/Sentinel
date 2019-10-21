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

/**
 * The point of access to the statistics of an {@link HttpConnection}.
 *
 * @since 4.0
 */
public interface HttpConnectionMetrics {

    /**
     * Returns the number of requests transferred over the connection,
     * 0 if not available.
     */
    long getRequestCount();

    /**
     * Returns the number of responses transferred over the connection,
     * 0 if not available.
     */
    long getResponseCount();

    /**
     * Returns the number of bytes transferred over the connection,
     * 0 if not available.
     */
    long getSentBytesCount();

    /**
     * Returns the number of bytes transferred over the connection,
     * 0 if not available.
     */
    long getReceivedBytesCount();

    /**
     * Return the value for the specified metric.
     *
     *@param metricName the name of the metric to query.
     *
     *@return the object representing the metric requested,
     *        {@code null} if the metric cannot not found.
     */
    Object getMetric(String metricName);

    /**
     * Resets the counts
     *
     */
    void reset();

}
