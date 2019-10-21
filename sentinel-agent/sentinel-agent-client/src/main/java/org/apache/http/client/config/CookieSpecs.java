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

package org.apache.http.client.config;

/**
 * Standard cookie specifications supported by HttpClient.
 *
 * @since 4.3
 */
public final class CookieSpecs {

    /**
     * The policy that provides high degree of compatibility
     * with common cookie management of popular HTTP agents.
     *
     * @deprecated (4.4) use {link #DEFAULT}.
     */
    @Deprecated
    public static final String BROWSER_COMPATIBILITY = "compatibility";

    /**
     * The Netscape cookie draft compliant policy.
     */
    public static final String NETSCAPE = "netscape";

    /**
     * The RFC 6265 compliant policy (interoprability profile).
     */
    public static final String STANDARD = "standard";

    /**
     * The RFC 6265 compliant policy (strict profile).
     *
     * @since 4.4
     */
    public static final String STANDARD_STRICT = "standard-strict";

    /**
     * The default 'best match' policy.
     *
     * @deprecated (4.4) use {link #DEFAULT}.
     */
    @Deprecated
    public static final String BEST_MATCH = "best-match";

    /**
     * The default policy. This policy provides a higher degree of compatibility
     * with common cookie management of popular HTTP agents for non-standard
     * (Netscape style) cookies.
     */
    public static final String DEFAULT = "default";

    /**
     * The policy that ignores cookies.
     */
    public static final String IGNORE_COOKIES = "ignoreCookies";

    private CookieSpecs() {
    }

}
