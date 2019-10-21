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
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

/**
 * An entity that releases a {@link ManagedClientConnection connection}.
 * A {@link ManagedClientConnection} will
 * typically <i>not</i> return a managed entity, but you can replace
 * the unmanaged entity in the response with a managed one.
 *
 * @since 4.0
 *
 * @deprecated (4.3) do not use.
 */
@Deprecated
public class BasicManagedEntity extends HttpEntityWrapper
    implements ConnectionReleaseTrigger, EofSensorWatcher {

    /** The connection to release. */
    protected ManagedClientConnection managedConn;

    /** Whether to keep the connection alive. */
    protected final boolean attemptReuse;

    /**
     * Creates a new managed entity that can release a connection.
     *
     * @param entity    the entity of which to wrap the content.
     *                  Note that the argument entity can no longer be used
     *                  afterwards, since the content will be taken by this
     *                  managed entity.
     * @param conn      the connection to release
     * @param reuse     whether the connection should be re-used
     */
    public BasicManagedEntity(final HttpEntity entity,
                              final ManagedClientConnection conn,
                              final boolean reuse) {
        super(entity);
        Args.notNull(conn, "Connection");
        this.managedConn = conn;
        this.attemptReuse = reuse;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(wrappedEntity.getContent(), this);
    }

    private void ensureConsumed() throws IOException {
        if (managedConn == null) {
            return;
        }

        try {
            if (attemptReuse) {
                // this will not trigger a callback from EofSensorInputStream
                EntityUtils.consume(wrappedEntity);
                managedConn.markReusable();
            } else {
                managedConn.unmarkReusable();
            }
        } finally {
            releaseManagedConnection();
        }
    }

    /**
     * @deprecated (4.1) Use {@link EntityUtils#consume(HttpEntity)}
     */
    @Deprecated
    @Override
    public void consumeContent() throws IOException {
        ensureConsumed();
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(outstream);
        ensureConsumed();
    }

    @Override
    public void releaseConnection() throws IOException {
        ensureConsumed();
    }

    @Override
    public void abortConnection() throws IOException {

        if (managedConn != null) {
            try {
                managedConn.abortConnection();
            } finally {
                managedConn = null;
            }
        }
    }

    @Override
    public boolean eofDetected(final InputStream wrapped) throws IOException {
        try {
            if (managedConn != null) {
                if (attemptReuse) {
                    // there may be some cleanup required, such as
                    // reading trailers after the response body:
                    wrapped.close();
                    managedConn.markReusable();
                } else {
                    managedConn.unmarkReusable();
                }
            }
        } finally {
            releaseManagedConnection();
        }
        return false;
    }

    @Override
    public boolean streamClosed(final InputStream wrapped) throws IOException {
        try {
            if (managedConn != null) {
                if (attemptReuse) {
                    final boolean valid = managedConn.isOpen();
                    // this assumes that closing the stream will
                    // consume the remainder of the response body:
                    try {
                        wrapped.close();
                        managedConn.markReusable();
                    } catch (final SocketException ex) {
                        if (valid) {
                            throw ex;
                        }
                    }
                } else {
                    managedConn.unmarkReusable();
                }
            }
        } finally {
            releaseManagedConnection();
        }
        return false;
    }

    @Override
    public boolean streamAbort(final InputStream wrapped) throws IOException {
        if (managedConn != null) {
            managedConn.abortConnection();
        }
        return false;
    }

    /**
     * Releases the connection gracefully.
     * The connection attribute will be nullified.
     * Subsequent invocations are no-ops.
     *
     * @throws IOException      in case of an IO problem.
     *         The connection attribute will be nullified anyway.
     */
    protected void releaseManagedConnection()
        throws IOException {

        if (managedConn != null) {
            try {
                managedConn.releaseConnection();
            } finally {
                managedConn = null;
            }
        }
    }

}
