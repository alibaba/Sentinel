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

package org.apache.http.client.params;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.http.HttpHost;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 * @deprecated (4.3) provided for compatibility with {@link HttpParams}. Do not use.
 *
 * @since 4.3
 */
@Deprecated
public final class HttpClientParamConfig {

    private HttpClientParamConfig() {
    }

    @SuppressWarnings("unchecked")
    public static RequestConfig getRequestConfig(final HttpParams params) {
        return getRequestConfig(params, RequestConfig.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public static RequestConfig getRequestConfig(final HttpParams params, final RequestConfig defaultConfig) {
        final RequestConfig.Builder builder = RequestConfig.copy(defaultConfig)
                .setSocketTimeout(params.getIntParameter(
                        CoreConnectionPNames.SO_TIMEOUT, defaultConfig.getSocketTimeout()))
                .setStaleConnectionCheckEnabled(params.getBooleanParameter(
                        CoreConnectionPNames.STALE_CONNECTION_CHECK, defaultConfig.isStaleConnectionCheckEnabled()))
                .setConnectTimeout(params.getIntParameter(
                        CoreConnectionPNames.CONNECTION_TIMEOUT, defaultConfig.getConnectTimeout()))
                .setExpectContinueEnabled(params.getBooleanParameter(
                        CoreProtocolPNames.USE_EXPECT_CONTINUE, defaultConfig.isExpectContinueEnabled()))
                .setAuthenticationEnabled(params.getBooleanParameter(
                        ClientPNames.HANDLE_AUTHENTICATION, defaultConfig.isAuthenticationEnabled()))
                .setCircularRedirectsAllowed(params.getBooleanParameter(
                        ClientPNames.ALLOW_CIRCULAR_REDIRECTS, defaultConfig.isCircularRedirectsAllowed()))
                .setConnectionRequestTimeout((int) params.getLongParameter(
                        ClientPNames.CONN_MANAGER_TIMEOUT, defaultConfig.getConnectionRequestTimeout()))
                .setMaxRedirects(params.getIntParameter(
                        ClientPNames.MAX_REDIRECTS, defaultConfig.getMaxRedirects()))
                .setRedirectsEnabled(params.getBooleanParameter(
                        ClientPNames.HANDLE_REDIRECTS, defaultConfig.isRedirectsEnabled()))
                .setRelativeRedirectsAllowed(!params.getBooleanParameter(
                        ClientPNames.REJECT_RELATIVE_REDIRECT, !defaultConfig.isRelativeRedirectsAllowed()));

        final HttpHost proxy = (HttpHost) params.getParameter(ConnRoutePNames.DEFAULT_PROXY);
        if (proxy != null) {
            builder.setProxy(proxy);
        }
        final InetAddress localAddress = (InetAddress) params.getParameter(ConnRoutePNames.LOCAL_ADDRESS);
        if (localAddress != null) {
            builder.setLocalAddress(localAddress);
        }
        final Collection<String> targetAuthPrefs = (Collection<String>) params.getParameter(AuthPNames.TARGET_AUTH_PREF);
        if (targetAuthPrefs != null) {
            builder.setTargetPreferredAuthSchemes(targetAuthPrefs);
        }
        final Collection<String> proxySuthPrefs = (Collection<String>) params.getParameter(AuthPNames.PROXY_AUTH_PREF);
        if (proxySuthPrefs != null) {
            builder.setProxyPreferredAuthSchemes(proxySuthPrefs);
        }
        final String cookiePolicy = (String) params.getParameter(ClientPNames.COOKIE_POLICY);
        if (cookiePolicy != null) {
            builder.setCookieSpec(cookiePolicy);
        }
        return builder.build();
    }

}
