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

import java.io.IOException;

/**
 * Interface for releasing a connection. This can be implemented by various
 * "trigger" objects which are associated with a connection, for example
 * a {@link EofSensorInputStream} or the {@link ManagedHttpClientConnection} itself.
 * <p>
 * The methods in this interface can safely be called multiple times.
 * The first invocation releases the connection, subsequent calls
 * are ignored.
 *
 * @since 4.0
 */
public interface ConnectionReleaseTrigger {

    /**
     * Releases the connection with the option of keep-alive. This is a
     * "graceful" release and may cause IO operations for consuming the
     * remainder of a response entity. Use
     * {@link #abortConnection abortConnection} for a hard release. The
     * connection may be reused as specified by the duration.
     *
     * @throws IOException
     *             in case of an IO problem. The connection will be released
     *             anyway.
     */
    void releaseConnection()
        throws IOException;

    /**
     * Releases the connection without the option of keep-alive.
     * This is a "hard" release that implies a shutdown of the connection.
     * Use {@link #releaseConnection()} for a graceful release.
     *
     * @throws IOException      in case of an IO problem.
     *         The connection will be released anyway.
     */
    void abortConnection()
        throws IOException;

}
