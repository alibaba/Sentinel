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

package org.apache.http.conn.ssl;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

/**
 * Interface for checking if a hostname matches the names stored inside the
 * server's X.509 certificate.  This interface extends
 * {@link javax.net.ssl.HostnameVerifier}, but it is recommended to use
 * methods added by X509HostnameVerifier.
 *
 * @since 4.0
 *
 * @deprecated (4.4) Use {@link javax.net.ssl.HostnameVerifier}.
 */
@Deprecated
public interface X509HostnameVerifier extends HostnameVerifier {

    /**
     * Verifies that the host name is an acceptable match with the server's
     * authentication scheme based on the given {@link SSLSocket}.
     *
     * @param host the host.
     * @param ssl the SSL socket.
     * @throws IOException if an I/O error occurs or the verification process
     *   fails.
     */
    void verify(String host, SSLSocket ssl) throws IOException;

    /**
     * Verifies that the host name is an acceptable match with the server's
     * authentication scheme based on the given {@link X509Certificate}.
     *
     * @param host the host.
     * @param cert the certificate.
     * @throws SSLException if the verification process fails.
     */
    void verify(String host, X509Certificate cert) throws SSLException;

    /**
     * Checks to see if the supplied hostname matches any of the supplied CNs
     * or "DNS" Subject-Alts.  Most implementations only look at the first CN,
     * and ignore any additional CNs.  Most implementations do look at all of
     * the "DNS" Subject-Alts. The CNs or Subject-Alts may contain wildcards
     * according to RFC 2818.
     *
     * @param cns         CN fields, in order, as extracted from the X.509
     *                    certificate.
     * @param subjectAlts Subject-Alt fields of type 2 ("DNS"), as extracted
     *                    from the X.509 certificate.
     * @param host        The hostname to verify.
     * @throws SSLException if the verification process fails.
     */
    void verify(String host, String[] cns, String[] subjectAlts)
          throws SSLException;

}
