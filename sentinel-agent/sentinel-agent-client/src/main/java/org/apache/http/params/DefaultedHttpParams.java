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

package org.apache.http.params;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.util.Args;

/**
 * {@link HttpParams} implementation that delegates resolution of a parameter
 * to the given default {@link HttpParams} instance if the parameter is not
 * present in the local one. The state of the local collection can be mutated,
 * whereas the default collection is treated as read-only.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public final class DefaultedHttpParams extends AbstractHttpParams {

    private final HttpParams local;
    private final HttpParams defaults;

    /**
     * Create the defaulted set of HttpParams.
     *
     * @param local the mutable set of HttpParams
     * @param defaults the default set of HttpParams, not mutated by this class
     */
    public DefaultedHttpParams(final HttpParams local, final HttpParams defaults) {
        super();
        this.local = Args.notNull(local, "Local HTTP parameters");
        this.defaults = defaults;
    }

    /**
     * Creates a copy of the local collection with the same default
     */
    public HttpParams copy() {
        final HttpParams clone = this.local.copy();
        return new DefaultedHttpParams(clone, this.defaults);
    }

    /**
     * Retrieves the value of the parameter from the local collection and, if the
     * parameter is not set locally, delegates its resolution to the default
     * collection.
     */
    public Object getParameter(final String name) {
        Object obj = this.local.getParameter(name);
        if (obj == null && this.defaults != null) {
            obj = this.defaults.getParameter(name);
        }
        return obj;
    }

    /**
     * Attempts to remove the parameter from the local collection. This method
     * <i>does not</i> modify the default collection.
     */
    public boolean removeParameter(final String name) {
        return this.local.removeParameter(name);
    }

    /**
     * Sets the parameter in the local collection. This method <i>does not</i>
     * modify the default collection.
     */
    public HttpParams setParameter(final String name, final Object value) {
        return this.local.setParameter(name, value);
    }

    /**
     *
     * @return the default HttpParams collection
     */
    public HttpParams getDefaults() {
        return this.defaults;
    }

    /**
     * Returns the current set of names
     * from both the local and default HttpParams instances.
     *
     * Changes to the underlying HttpParams intances are not reflected
     * in the set - it is a snapshot.
     *
     * @return the combined set of names, as a Set&lt;String&gt;
     * @since 4.2
     * @throws UnsupportedOperationException if either the local or default HttpParams instances do not implement HttpParamsNames
     */
    @Override
    public Set<String> getNames() {
        final Set<String> combined = new HashSet<String>(getNames(defaults));
        combined.addAll(getNames(this.local));
        return combined ;
    }

    /**
     * Returns the current set of defaults names.
     *
     * Changes to the underlying HttpParams are not reflected
     * in the set - it is a snapshot.
     *
     * @return the names, as a Set&lt;String&gt;
     * @since 4.2
     * @throws UnsupportedOperationException if the default HttpParams instance does not implement HttpParamsNames
     */
    public Set<String> getDefaultNames() {
        return new HashSet<String>(getNames(this.defaults));
    }

    /**
     * Returns the current set of local names.
     *
     * Changes to the underlying HttpParams are not reflected
     * in the set - it is a snapshot.
     *
     * @return the names, as a Set&lt;String&gt;
     * @since 4.2
     * @throws UnsupportedOperationException if the local HttpParams instance does not implement HttpParamsNames
     */
    public Set<String> getLocalNames() {
        return new HashSet<String>(getNames(this.local));
    }

    // Helper method
    private Set<String> getNames(final HttpParams params) {
        if (params instanceof HttpParamsNames) {
            return ((HttpParamsNames) params).getNames();
        }
        throw new UnsupportedOperationException("HttpParams instance does not implement HttpParamsNames");
    }

}
