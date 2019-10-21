/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.http;

import com.alibaba.acm.shaded.com.aliyuncs.IAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.http.clients.CompatibleUrlConnClient;

public final class X509TrustAll {

    private static boolean ignoreSSLCerts = false;

    @Deprecated
    public static void restoreSSLCertificate() {
        ignoreSSLCerts = false;
        CompatibleUrlConnClient.HttpsCertIgnoreHelper.restoreSSLCertificate();
    }

    public static void ignoreSSLCertificate() {
        ignoreSSLCerts = true;
        CompatibleUrlConnClient.HttpsCertIgnoreHelper.ignoreSSLCertificate();
    }

    public static void restoreSSLCertificate(IAcsClient client) {
        client.restoreSSLCertificate();
    }

    public static void ignoreSSLCertificate(IAcsClient client) {
        client.ignoreSSLCertificate();
    }

    /**
     * to keep compatible while using ApacheHttpClient
     *
     * @return
     */
    public static boolean isIgnoreSSLCerts() {
        return ignoreSSLCerts;
    }
}
