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
package org.apache.http.pool;

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;

import java.io.Serializable;

/**
 * Pool statistics.
 * <p>
 * The total number of connections in the pool is equal to {@code available} plus {@code leased}.
 * </p>
 *
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class PoolStats implements Serializable {

    private static final long serialVersionUID = -2807686144795228544L;

    private final int leased;
    private final int pending;
    private final int available;
    private final int max;

    public PoolStats(final int leased, final int pending, final int free, final int max) {
        super();
        this.leased = leased;
        this.pending = pending;
        this.available = free;
        this.max = max;
    }

    /**
     * Gets the number of persistent connections tracked by the connection manager currently being used to execute
     * requests.
     * <p>
     * The total number of connections in the pool is equal to {@code available} plus {@code leased}.
     * </p>
     *
     * @return the number of persistent connections.
     */
    public int getLeased() {
        return this.leased;
    }

    /**
     * Gets the number of connection requests being blocked awaiting a free connection. This can happen only if there
     * are more worker threads contending for fewer connections.
     *
     * @return the number of connection requests being blocked awaiting a free connection.
     */
    public int getPending() {
        return this.pending;
    }

    /**
     * Gets the number idle persistent connections.
     * <p>
     * The total number of connections in the pool is equal to {@code available} plus {@code leased}.
     * </p>
     *
     * @return number idle persistent connections.
     */
    public int getAvailable() {
        return this.available;
    }

    /**
     * Gets the maximum number of allowed persistent connections.
     *
     * @return the maximum number of allowed persistent connections.
     */
    public int getMax() {
        return this.max;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("[leased: ");
        buffer.append(this.leased);
        buffer.append("; pending: ");
        buffer.append(this.pending);
        buffer.append("; available: ");
        buffer.append(this.available);
        buffer.append("; max: ");
        buffer.append(this.max);
        buffer.append("]");
        return buffer.toString();
    }

}
