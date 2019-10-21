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
package org.apache.http.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A strategy to establish trustworthiness of certificates without consulting the trust manager
 * configured in the actual SSL context. This interface can be used to override the standard
 * JSSE certificate verification process.
 *
 * @since 4.4
 */
public interface TrustStrategy {

    /**
     * Determines whether the certificate chain can be trusted without consulting the trust manager
     * configured in the actual SSL context. This method can be used to override the standard JSSE
     * certificate verification process.
     * <p>
     * Please note that, if this method returns {@code false}, the trust manager configured
     * in the actual SSL context can still clear the certificate as trusted.
     *
     * @param chain the peer certificate chain
     * @param authType the authentication type based on the client certificate
     * @return {@code true} if the certificate can be trusted without verification by
     *   the trust manager, {@code false} otherwise.
     * @throws CertificateException thrown if the certificate is not trusted or invalid.
     */
    boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException;

}
