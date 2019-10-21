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

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.AbstractPooledConnAdapter;

/**
 * A connection wrapper and callback handler.
 * All connections given out by the manager are wrappers which
 * can be {@link #detach detach}ed to prevent further use on release.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  do not use
 */
@Deprecated
public class BasicPooledConnAdapter extends AbstractPooledConnAdapter {

    /**
     * Creates a new adapter.
     *
     * @param tsccm   the connection manager
     * @param entry   the pool entry for the connection being wrapped
     */
    protected BasicPooledConnAdapter(final ThreadSafeClientConnManager tsccm,
                               final AbstractPoolEntry entry) {
        super(tsccm, entry);
        markReusable();
    }

    @Override
    protected ClientConnectionManager getManager() {
        // override needed only to make method visible in this package
        return super.getManager();
    }

    @Override
    protected AbstractPoolEntry getPoolEntry() {
        // override needed only to make method visible in this package
        return super.getPoolEntry();
    }

    @Override
    protected void detach() {
        // override needed only to make method visible in this package
        super.detach();
    }

}
