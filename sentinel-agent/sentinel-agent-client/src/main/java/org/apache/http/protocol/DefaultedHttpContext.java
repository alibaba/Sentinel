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

package org.apache.http.protocol;

import org.apache.http.util.Args;

/**
 * {@link HttpContext} implementation that delegates resolution of an attribute
 * to the given default {@link HttpContext} instance if the attribute is not
 * present in the local one. The state of the local context can be mutated,
 * whereas the default context is treated as read-only.
 *
 * @since 4.0
 *
 * @deprecated (4.3) no longer used.
 */
@Deprecated
public final class DefaultedHttpContext implements HttpContext {

    private final HttpContext local;
    private final HttpContext defaults;

    public DefaultedHttpContext(final HttpContext local, final HttpContext defaults) {
        super();
        this.local = Args.notNull(local, "HTTP context");
        this.defaults = defaults;
    }

    public Object getAttribute(final String id) {
        final Object obj = this.local.getAttribute(id);
        if (obj == null) {
            return this.defaults.getAttribute(id);
        } else {
            return obj;
        }
    }

    public Object removeAttribute(final String id) {
        return this.local.removeAttribute(id);
    }

    public void setAttribute(final String id, final Object obj) {
        this.local.setAttribute(id, obj);
    }

    public HttpContext getDefaults() {
        return this.defaults;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[local: ").append(this.local);
        buf.append("defaults: ").append(this.defaults);
        buf.append("]");
        return buf.toString();
    }

}
