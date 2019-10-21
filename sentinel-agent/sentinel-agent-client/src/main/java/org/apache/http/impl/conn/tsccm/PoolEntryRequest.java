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
package org.apache.http.impl.conn.tsccm;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ConnectionPoolTimeoutException;

/**
 * Encapsulates a request for a {@link BasicPoolEntry}.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  use {@link java.util.concurrent.Future}
 */
@Deprecated
public interface PoolEntryRequest {

    /**
     * Obtains a pool entry with a connection within the given timeout.
     * If {@link #abortRequest()} is called before this completes
     * an {@link InterruptedException} is thrown.
     *
     * @param timeout   the timeout, 0 or negative for no timeout
     * @param tunit     the unit for the {@code timeout},
     *                  may be {@code null} only if there is no timeout
     *
     * @return  pool entry holding a connection for the route
     *
     * @throws ConnectionPoolTimeoutException
     *         if the timeout expired
     * @throws InterruptedException
     *         if the calling thread was interrupted or the request was aborted
     */
    BasicPoolEntry getPoolEntry(
            long timeout,
            TimeUnit tunit) throws InterruptedException, ConnectionPoolTimeoutException;

    /**
     * Aborts the active or next call to
     * {@link #getPoolEntry(long, TimeUnit)}.
     */
    void abortRequest();

}
