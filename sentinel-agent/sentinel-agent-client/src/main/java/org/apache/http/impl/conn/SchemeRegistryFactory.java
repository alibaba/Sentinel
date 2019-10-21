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
package org.apache.http.impl.conn;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * @since 4.1
 *
 * @deprecated (4.3) use {@link org.apache.http.impl.client.HttpClientBuilder}.
 */
@Contract(threading = ThreadingBehavior.SAFE)
@Deprecated
public final class SchemeRegistryFactory {

    /**
     * Initializes default scheme registry based on JSSE defaults. System properties will
     * not be taken into consideration.
     */
    public static SchemeRegistry createDefault() {
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        return registry;
    }

    /**
     * Initializes default scheme registry using system properties as described in
     * <a href="http://download.oracle.com/javase/1,5.0/docs/guide/security/jsse/JSSERefGuide.html">
     * "JavaTM Secure Socket Extension (JSSE) Reference Guide for the JavaTM 2 Platform
     * Standard Edition 5</a>
     * <p>
     * The following system properties are taken into account by this method:
     * <ul>
     *  <li>ssl.TrustManagerFactory.algorithm</li>
     *  <li>javax.net.ssl.trustStoreType</li>
     *  <li>javax.net.ssl.trustStore</li>
     *  <li>javax.net.ssl.trustStoreProvider</li>
     *  <li>javax.net.ssl.trustStorePassword</li>
     *  <li>java.home</li>
     *  <li>ssl.KeyManagerFactory.algorithm</li>
     *  <li>javax.net.ssl.keyStoreType</li>
     *  <li>javax.net.ssl.keyStore</li>
     *  <li>javax.net.ssl.keyStoreProvider</li>
     *  <li>javax.net.ssl.keyStorePassword</li>
     * </ul>
     * <p>
     *
     * @since 4.2
     */
    public static SchemeRegistry createSystemDefault() {
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(
                new Scheme("https", 443, SSLSocketFactory.getSystemSocketFactory()));
        return registry;
    }
}

