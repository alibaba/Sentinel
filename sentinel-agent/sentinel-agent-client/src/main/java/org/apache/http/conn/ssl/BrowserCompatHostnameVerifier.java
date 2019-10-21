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

import javax.net.ssl.SSLException;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * The HostnameVerifier that works the same way as Curl and Firefox.
 * <p>
 * The hostname must match either the first CN, or any of the subject-alts.
 * A wildcard can occur in the CN, and in any of the subject-alts.
 * </p>
 * <p>
 * The only difference between BROWSER_COMPATIBLE and STRICT is that a wildcard
 * (such as "*.foo.com") with BROWSER_COMPATIBLE matches all subdomains,
 * including "a.b.foo.com".
 * </p>
 *
 * @since 4.0
 *
 * @deprecated (4.4) Use {@link org.apache.http.conn.ssl.DefaultHostnameVerifier}
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public class BrowserCompatHostnameVerifier extends AbstractVerifier {

    public static final BrowserCompatHostnameVerifier INSTANCE = new BrowserCompatHostnameVerifier();

    @Override
    public final void verify(
            final String host,
            final String[] cns,
            final String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, false);
    }

    @Override
    public final String toString() {
        return "BROWSER_COMPATIBLE";
    }

}
