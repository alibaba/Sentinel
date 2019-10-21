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
import java.io.InputStream;

import org.apache.http.util.Args;

/**
 * Basic implementation of {@link EofSensorWatcher}. The underlying connection
 * is released on close or EOF.
 *
 * @since 4.0
 *
 * @deprecated (4.3) do not use.
 */
@Deprecated
public class BasicEofSensorWatcher implements EofSensorWatcher {

    /** The connection to auto-release. */
    protected final ManagedClientConnection managedConn;

    /** Whether to keep the connection alive. */
    protected final boolean attemptReuse;

    /**
     * Creates a new watcher for auto-releasing a connection.
     *
     * @param conn      the connection to auto-release
     * @param reuse     whether the connection should be re-used
     */
    public BasicEofSensorWatcher(final ManagedClientConnection conn,
                                 final boolean reuse) {
        Args.notNull(conn, "Connection");
        managedConn = conn;
        attemptReuse = reuse;
    }

    @Override
    public boolean eofDetected(final InputStream wrapped)
        throws IOException {

        try {
            if (attemptReuse) {
                // there may be some cleanup required, such as
                // reading trailers after the response body:
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            managedConn.releaseConnection();
        }
        return false;
    }

    @Override
    public boolean streamClosed(final InputStream wrapped)
        throws IOException {

        try {
            if (attemptReuse) {
                // this assumes that closing the stream will
                // consume the remainder of the response body:
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            managedConn.releaseConnection();
        }
        return false;
    }

    @Override
    public boolean streamAbort(final InputStream wrapped)
        throws IOException {

        managedConn.abortConnection();
        return false;
    }

}
