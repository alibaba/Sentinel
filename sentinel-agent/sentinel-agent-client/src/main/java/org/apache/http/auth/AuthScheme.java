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

import org.apache.http.Header;
import org.apache.http.HttpRequest;

/**
 * This interface represents an abstract challenge-response oriented
 * authentication scheme.
 * <p>
 * An authentication scheme should be able to support the following
 * functions:
 * <ul>
 *   <li>Parse and process the challenge sent by the target server
 *       in response to request for a protected resource
 *   <li>Provide its textual designation
 *   <li>Provide its parameters, if available
 *   <li>Provide the realm this authentication scheme is applicable to,
 *       if available
 *   <li>Generate authorization string for the given set of credentials
 *       and the HTTP request in response to the authorization challenge.
 * </ul>
 * <p>
 * Authentication schemes may be stateful involving a series of
 * challenge-response exchanges.
 * <p>
 * IMPORTANT: implementations of this interface MUST also implement {@link ContextAwareAuthScheme}
 * interface in order to remain API compatible with newer versions of HttpClient.
 *
 * @since 4.0
 */

public interface AuthScheme {

    /**
     * Processes the given challenge token. Some authentication schemes
     * may involve multiple challenge-response exchanges. Such schemes must be able
     * to maintain the state information when dealing with sequential challenges
     *
     * @param header the challenge header
     */
    void processChallenge(final Header header) throws MalformedChallengeException;

    /**
     * Returns textual designation of the given authentication scheme.
     *
     * @return the name of the given authentication scheme
     */
    String getSchemeName();

    /**
     * Returns authentication parameter with the given name, if available.
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    String getParameter(final String name);

    /**
     * Returns authentication realm. If the concept of an authentication
     * realm is not applicable to the given authentication scheme, returns
     * {@code null}.
     *
     * @return the authentication realm
     */
    String getRealm();

    /**
     * Tests if the authentication scheme is provides authorization on a per
     * connection basis instead of usual per request basis
     *
     * @return {@code true} if the scheme is connection based, {@code false}
     * if the scheme is request based.
     */
    boolean isConnectionBased();

    /**
     * Authentication process may involve a series of challenge-response exchanges.
     * This method tests if the authorization process has been completed, either
     * successfully or unsuccessfully, that is, all the required authorization
     * challenges have been processed in their entirety.
     *
     * @return {@code true} if the authentication process has been completed,
     * {@code false} otherwise.
     */
    boolean isComplete();

    /**
     * Produces an authorization string for the given set of {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param request The request being authenticated
     * @throws AuthenticationException if authorization string cannot
     *   be generated due to an authentication failure
     *
     * @return the authorization string
     *
     * @deprecated (4.1)  Use {@link ContextAwareAuthScheme#authenticate(Credentials, HttpRequest, org.apache.http.protocol.HttpContext)}
     */
    @Deprecated
    Header authenticate(Credentials credentials, HttpRequest request)
            throws AuthenticationException;

}
