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
import org.apache.http.protocol.HttpContext;

/**
 * This interface represents an extended  authentication scheme
 * that requires access to {@link HttpContext} in order to
 * generate an authorization string.
 *
 * TODO: Fix AuthScheme interface in the next major version
 *
 * @since 4.1
 */

public interface ContextAwareAuthScheme extends AuthScheme {

    /**
     * Produces an authorization string for the given set of
     * {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param request The request being authenticated
     * @param context HTTP context
     * @throws AuthenticationException if authorization string cannot
     *   be generated due to an authentication failure
     *
     * @return the authorization string
     */
    Header authenticate(
            Credentials credentials,
            HttpRequest request,
            HttpContext context) throws AuthenticationException;

}
