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

import java.util.HashMap;
import java.util.Map;

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.util.Args;

/**
 * Maintains a map of objects keyed by a request URI pattern.
 * <br>
 * Patterns may have three formats:
 * <ul>
 *   <li>{@code *}</li>
 *   <li>{@code *&lt;uri&gt;}</li>
 *   <li>{@code &lt;uri&gt;*}</li>
 * </ul>
 * <br>
 * This class can be used to resolve an object matching a particular request
 * URI.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class UriPatternMatcher<T> {

    private final Map<String, T> map;

    public UriPatternMatcher() {
        super();
        this.map = new HashMap<String, T>();
    }

    /**
     * Registers the given object for URIs matching the given pattern.
     *
     * @param pattern the pattern to register the handler for.
     * @param obj the object.
     */
    public synchronized void register(final String pattern, final T obj) {
        Args.notNull(pattern, "URI request pattern");
        this.map.put(pattern, obj);
    }

    /**
     * Removes registered object, if exists, for the given pattern.
     *
     * @param pattern the pattern to unregister.
     */
    public synchronized void unregister(final String pattern) {
        if (pattern == null) {
            return;
        }
        this.map.remove(pattern);
    }

    /**
     * @deprecated (4.1) do not use
     */
    @Deprecated
    public synchronized void setHandlers(final Map<String, T> map) {
        Args.notNull(map, "Map of handlers");
        this.map.clear();
        this.map.putAll(map);
    }

    /**
     * @deprecated (4.1) do not use
     */
    @Deprecated
    public synchronized void setObjects(final Map<String, T> map) {
        Args.notNull(map, "Map of handlers");
        this.map.clear();
        this.map.putAll(map);
    }

    /**
     * @deprecated (4.1) do not use
     */
    @Deprecated
    public synchronized Map<String, T> getObjects() {
        return this.map;
    }

    /**
     * Looks up an object matching the given request path.
     *
     * @param path the request path
     * @return object or {@code null} if no match is found.
     */
    public synchronized T lookup(final String path) {
        Args.notNull(path, "Request path");
        // direct match?
        T obj = this.map.get(path);
        if (obj == null) {
            // pattern match?
            String bestMatch = null;
            for (final String pattern : this.map.keySet()) {
                if (matchUriRequestPattern(pattern, path)) {
                    // we have a match. is it any better?
                    if (bestMatch == null
                            || (bestMatch.length() < pattern.length())
                            || (bestMatch.length() == pattern.length() && pattern.endsWith("*"))) {
                        obj = this.map.get(pattern);
                        bestMatch = pattern;
                    }
                }
            }
        }
        return obj;
    }

    /**
     * Tests if the given request path matches the given pattern.
     *
     * @param pattern the pattern
     * @param path the request path
     * @return {@code true} if the request URI matches the pattern,
     *   {@code false} otherwise.
     */
    protected boolean matchUriRequestPattern(final String pattern, final String path) {
        if (pattern.equals("*")) {
            return true;
        } else {
            return
            (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1))) ||
            (pattern.startsWith("*") && path.endsWith(pattern.substring(1, pattern.length())));
        }
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}
