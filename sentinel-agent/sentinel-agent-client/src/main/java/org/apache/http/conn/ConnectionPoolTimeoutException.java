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

/**
 * A timeout while waiting for an available connection
 * from a connection manager.
 *
 *
 * @since 4.0
 */
public class ConnectionPoolTimeoutException extends ConnectTimeoutException {

    private static final long serialVersionUID = -7898874842020245128L;

    /**
     * Creates a ConnectTimeoutException with a {@code null} detail message.
     */
    public ConnectionPoolTimeoutException() {
        super();
    }

    /**
     * Creates a ConnectTimeoutException with the specified detail message.
     *
     * @param message The exception detail message
     */
    public ConnectionPoolTimeoutException(final String message) {
        super(message);
    }

}
