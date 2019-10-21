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
 * Standard authentication schemes supported by HttpClient.
 *
 * @since 4.3
 */
public final class AuthSchemes {

    /**
     * Basic authentication scheme as defined in RFC 2617 (considered inherently
     * insecure, but most widely supported).
     */
    public static final String BASIC = "Basic";

    /**
     * Digest authentication scheme as defined in RFC 2617.
     */
    public static final String DIGEST = "Digest";

    /**
     * The NTLM authentication scheme is a proprietary Microsoft Windows
     * authentication protocol as defined in [MS-NLMP].
     */
    public static final String NTLM = "NTLM";

    /**
     * SPNEGO authentication scheme as defined in RFC 4559 and RFC 4178
     * (considered to be the most secure among currently supported
     * authentication schemes if Kerberos is selected).
     */
    public static final String SPNEGO = "Negotiate";

    /**
     * Kerberos authentication scheme as defined in RFC 4120
     * (considered to be the most secure among currently supported
     * authentication schemes).
     */
    public static final String KERBEROS = "Kerberos";

    private AuthSchemes() {
    }

}
