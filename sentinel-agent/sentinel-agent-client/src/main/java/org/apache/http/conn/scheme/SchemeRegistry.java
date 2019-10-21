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
package org.apache.http.conn.scheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;

/**
 * A set of supported protocol {@link Scheme}s.
 * Schemes are identified by lowercase names.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.config.Registry}
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public final class SchemeRegistry {

    /** The available schemes in this registry. */
    private final ConcurrentHashMap<String,Scheme> registeredSchemes;

    /**
     * Creates a new, empty scheme registry.
     */
    public SchemeRegistry() {
        super();
        registeredSchemes = new ConcurrentHashMap<String,Scheme>();
    }

    /**
     * Obtains a scheme by name.
     *
     * @param name      the name of the scheme to look up (in lowercase)
     *
     * @return  the scheme, never {@code null}
     *
     * @throws IllegalStateException
     *          if the scheme with the given name is not registered
     */
    public final Scheme getScheme(final String name) {
        final Scheme found = get(name);
        if (found == null) {
            throw new IllegalStateException
                ("Scheme '"+name+"' not registered.");
        }
        return found;
    }

    /**
     * Obtains the scheme for a host.
     * Convenience method for {@code getScheme(host.getSchemeName())}
     *
     * @param host the host for which to obtain the scheme
     *
     * @return the scheme for the given host, never {@code null}
     *
     * @throws IllegalStateException
     *          if a scheme with the respective name is not registered
     */
    public final Scheme getScheme(final HttpHost host) {
        Args.notNull(host, "Host");
        return getScheme(host.getSchemeName());
    }

    /**
     * Obtains a scheme by name, if registered.
     *
     * @param name      the name of the scheme to look up (in lowercase)
     *
     * @return  the scheme, or
     *          {@code null} if there is none by this name
     */
    public final Scheme get(final String name) {
        Args.notNull(name, "Scheme name");
        // leave it to the caller to use the correct name - all lowercase
        //name = name.toLowerCase(Locale.ENGLISH);
        final Scheme found = registeredSchemes.get(name);
        return found;
    }

    /**
     * Registers a scheme.
     * The scheme can later be retrieved by its name
     * using {@link #getScheme(String) getScheme} or {@link #get get}.
     *
     * @param sch       the scheme to register
     *
     * @return  the scheme previously registered with that name, or
     *          {@code null} if none was registered
     */
    public final Scheme register(final Scheme sch) {
        Args.notNull(sch, "Scheme");
        final Scheme old = registeredSchemes.put(sch.getName(), sch);
        return old;
    }

    /**
     * Unregisters a scheme.
     *
     * @param name      the name of the scheme to unregister (in lowercase)
     *
     * @return  the unregistered scheme, or
     *          {@code null} if there was none
     */
    public final Scheme unregister(final String name) {
        Args.notNull(name, "Scheme name");
        // leave it to the caller to use the correct name - all lowercase
        //name = name.toLowerCase(Locale.ENGLISH);
        final Scheme gone = registeredSchemes.remove(name);
        return gone;
    }

    /**
     * Obtains the names of the registered schemes.
     *
     * @return  List containing registered scheme names.
     */
    public final List<String> getSchemeNames() {
        return new ArrayList<String>(registeredSchemes.keySet());
    }

    /**
     * Populates the internal collection of registered {@link Scheme protocol schemes}
     * with the content of the map passed as a parameter.
     *
     * @param map protocol schemes
     */
    public void setItems(final Map<String, Scheme> map) {
        if (map == null) {
            return;
        }
        registeredSchemes.clear();
        registeredSchemes.putAll(map);
    }

}

