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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.Args;

/**
 * A weak reference to a {@link BasicPoolEntry BasicPoolEntry}.
 * This reference explicitly keeps the planned route, so the connection
 * can be reclaimed if it is lost to garbage collection.
 *
 * @since 4.0
 *
 * @deprecated (4.2)  do not use
 */
@Deprecated
public class BasicPoolEntryRef extends WeakReference<BasicPoolEntry> {

    /** The planned route of the entry. */
    private final HttpRoute route; // HttpRoute is @Contract(threading = ThreadingBehavior.IMMUTABLE)


    /**
     * Creates a new reference to a pool entry.
     *
     * @param entry   the pool entry, must not be {@code null}
     * @param queue   the reference queue, or {@code null}
     */
    public BasicPoolEntryRef(final BasicPoolEntry entry,
                             final ReferenceQueue<Object> queue) {
        super(entry, queue);
        Args.notNull(entry, "Pool entry");
        route = entry.getPlannedRoute();
    }


    /**
     * Obtain the planned route for the referenced entry.
     * The planned route is still available, even if the entry is gone.
     *
     * @return      the planned route
     */
    public final HttpRoute getRoute() {
        return this.route;
    }

} // class BasicPoolEntryRef

