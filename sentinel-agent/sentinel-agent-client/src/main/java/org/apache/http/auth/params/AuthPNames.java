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

package org.apache.http.auth.params;

/**
 * Parameter names for HTTP authentication classes.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.client.config.RequestConfig}
 *   and constructor parameters of
 *   {@link org.apache.http.auth.AuthSchemeProvider}s.
*/
@Deprecated
public interface AuthPNames {

    /**
     * Defines the charset to be used when encoding
     * {@link org.apache.http.auth.Credentials}.
     * <p>
     * This parameter expects a value of type {@link String}.
     */
    public static final String CREDENTIAL_CHARSET = "http.auth.credential-charset";

    /**
     * Defines the order of preference for supported
     *  {@link org.apache.http.auth.AuthScheme}s when authenticating with
     *  the target host.
     * <p>
     * This parameter expects a value of type {@link java.util.Collection}. The
     * collection is expected to contain {@link String} instances representing
     * a name of an authentication scheme as returned by
     * {@link org.apache.http.auth.AuthScheme#getSchemeName()}.
     */
    public static final String TARGET_AUTH_PREF = "http.auth.target-scheme-pref";

    /**
     * Defines the order of preference for supported
     *  {@link org.apache.http.auth.AuthScheme}s when authenticating with the
     *  proxy host.
     * <p>
     * This parameter expects a value of type {@link java.util.Collection}. The
     * collection is expected to contain {@link String} instances representing
     * a name of an authentication scheme as returned by
     * {@link org.apache.http.auth.AuthScheme#getSchemeName()}.
     */
    public static final String PROXY_AUTH_PREF = "http.auth.proxy-scheme-pref";

}
