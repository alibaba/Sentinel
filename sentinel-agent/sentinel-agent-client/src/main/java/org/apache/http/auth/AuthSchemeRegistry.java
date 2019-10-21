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
package org.apache.http.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequest;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.config.Lookup;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/**
 * Authentication scheme registry that can be used to obtain the corresponding
 * authentication scheme implementation for a given type of authorization challenge.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.config.Registry}
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public final class AuthSchemeRegistry implements Lookup<AuthSchemeProvider> {

    private final ConcurrentHashMap<String,AuthSchemeFactory> registeredSchemes;

    public AuthSchemeRegistry() {
        super();
        this.registeredSchemes = new ConcurrentHashMap<String,AuthSchemeFactory>();
    }

    /**
     * Registers a {@link AuthSchemeFactory} with  the given identifier. If a factory with the
     * given name already exists it will be overridden. This name is the same one used to
     * retrieve the {@link AuthScheme authentication scheme} from {@link #getAuthScheme}.
     *
     * <p>
     * Please note that custom authentication preferences, if used, need to be updated accordingly
     * for the new {@link AuthScheme authentication scheme} to take effect.
     * </p>
     *
     * @param name the identifier for this scheme
     * @param factory the {@link AuthSchemeFactory} class to register
     *
     * @see #getAuthScheme
     */
    public void register(
            final String name,
            final AuthSchemeFactory factory) {
         Args.notNull(name, "Name");
        Args.notNull(factory, "Authentication scheme factory");
        registeredSchemes.put(name.toLowerCase(Locale.ENGLISH), factory);
    }

    /**
     * Unregisters the class implementing an {@link AuthScheme authentication scheme} with
     * the given name.
     *
     * @param name the identifier of the class to unregister
     */
    public void unregister(final String name) {
         Args.notNull(name, "Name");
        registeredSchemes.remove(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the {@link AuthScheme authentication scheme} with the given name.
     *
     * @param name the {@link AuthScheme authentication scheme} identifier
     * @param params the {@link HttpParams HTTP parameters} for the authentication
     *  scheme.
     *
     * @return {@link AuthScheme authentication scheme}
     *
     * @throws IllegalStateException if a scheme with the given name cannot be found
     */
    public AuthScheme getAuthScheme(final String name, final HttpParams params)
        throws IllegalStateException {

        Args.notNull(name, "Name");
        final AuthSchemeFactory factory = registeredSchemes.get(name.toLowerCase(Locale.ENGLISH));
        if (factory != null) {
            return factory.newInstance(params);
        } else {
            throw new IllegalStateException("Unsupported authentication scheme: " + name);
        }
    }

    /**
     * Obtains a list containing the names of all registered {@link AuthScheme authentication
     * schemes}
     *
     * @return list of registered scheme names
     */
    public List<String> getSchemeNames() {
        return new ArrayList<String>(registeredSchemes.keySet());
    }

    /**
     * Populates the internal collection of registered {@link AuthScheme authentication schemes}
     * with the content of the map passed as a parameter.
     *
     * @param map authentication schemes
     */
    public void setItems(final Map<String, AuthSchemeFactory> map) {
        if (map == null) {
            return;
        }
        registeredSchemes.clear();
        registeredSchemes.putAll(map);
    }

    @Override
    public AuthSchemeProvider lookup(final String name) {
        return new AuthSchemeProvider() {

            @Override
            public AuthScheme create(final HttpContext context) {
                final HttpRequest request = (HttpRequest) context.getAttribute(
                        ExecutionContext.HTTP_REQUEST);
                return getAuthScheme(name, request.getParams());
            }

        };
    }

}
