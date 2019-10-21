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

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

/**
 * The ALLOW_ALL HostnameVerifier essentially turns hostname verification
 * off. This implementation is a no-op, and never throws the SSLException.
 *
 *
 * @since 4.0
 *
 * @deprecated (4.4) Use {@link org.apache.http.conn.ssl.NoopHostnameVerifier}
 */
@Deprecated
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class AllowAllHostnameVerifier extends AbstractVerifier {

    public static final AllowAllHostnameVerifier INSTANCE = new AllowAllHostnameVerifier();

    @Override
    public final void verify(
            final String host,
            final String[] cns,
            final String[] subjectAlts) {
        // Allow everything - so never blowup.
    }

    @Override
    public final String toString() {
        return "ALLOW_ALL";
    }

}
