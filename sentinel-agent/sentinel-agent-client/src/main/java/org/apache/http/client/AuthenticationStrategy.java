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

package org.apache.http.client;

import java.util.Map;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.protocol.HttpContext;

/**
/**
 * A handler for determining if an HTTP response represents an authentication challenge that was
 * sent back to the client as a result of authentication failure.
 * <p>
 * Implementations of this interface must be thread-safe. Access to shared data must be
 * synchronized as methods of this interface may be executed from multiple threads.
 *
 * @since 4.2
 */
public interface AuthenticationStrategy {

    /**
     * Determines if the given HTTP response response represents
     * an authentication challenge that was sent back as a result
     * of authentication failure.
     *
     * @param authhost authentication host.
     * @param response HTTP response.
     * @param context HTTP context.
     * @return {@code true} if user authentication is required,
     *   {@code false} otherwise.
     */
    boolean isAuthenticationRequested(
            HttpHost authhost,
            HttpResponse response,
            HttpContext context);

    /**
     * Extracts from the given HTTP response a collection of authentication
     * challenges, each of which represents an authentication scheme supported
     * by the authentication host.
     *
     * @param authhost authentication host.
     * @param response HTTP response.
     * @param context HTTP context.
     * @return a collection of challenges keyed by names of corresponding
     * authentication schemes.
     * @throws MalformedChallengeException if one of the authentication
     *  challenges is not valid or malformed.
     */
    Map<String, Header> getChallenges(
            HttpHost authhost,
            HttpResponse response,
            HttpContext context) throws MalformedChallengeException;

    /**
     * Selects one authentication challenge out of all available and
     * creates and generates {@link AuthOption} instance capable of
     * processing that challenge.
     *
     * @param challenges collection of challenges.
     * @param authhost authentication host.
     * @param response HTTP response.
     * @param context HTTP context.
     * @return authentication auth schemes that can be used for authentication. Can be empty.
     * @throws MalformedChallengeException if one of the authentication
     *  challenges is not valid or malformed.
     */
    Queue<AuthOption> select(
            Map<String, Header> challenges,
            HttpHost authhost,
            HttpResponse response,
            HttpContext context) throws MalformedChallengeException;

    /**
     * Callback invoked in case of successful authentication.
     *
     * @param authhost authentication host.
     * @param authScheme authentication scheme used.
     * @param context HTTP context.
     */
    void authSucceeded(
            HttpHost authhost,
            AuthScheme authScheme,
            HttpContext context);

    /**
     * Callback invoked in case of unsuccessful authentication.
     *
     * @param authhost authentication host.
     * @param authScheme authentication scheme used.
     * @param context HTTP context.
     */
    void authFailed(
            HttpHost authhost,
            AuthScheme authScheme,
            HttpContext context);

}
