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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A trust strategy that accepts self-signed certificates as trusted. Verification of all other
 * certificates is done by the trust manager configured in the SSL context.
 *
 * @since 4.1
 */
public class TrustSelfSignedStrategy implements TrustStrategy {

    public static final TrustSelfSignedStrategy INSTANCE = new TrustSelfSignedStrategy();

    @Override
    public boolean isTrusted(
            final X509Certificate[] chain, final String authType) throws CertificateException {
        return chain.length == 1;
    }

}
