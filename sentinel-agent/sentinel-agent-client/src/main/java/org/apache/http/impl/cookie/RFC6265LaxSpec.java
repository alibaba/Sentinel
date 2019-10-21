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

package org.apache.http.impl.cookie;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.CommonCookieAttributeHandler;

/**
 * Standard {@link org.apache.http.cookie.CookieSpec} implementation that enforces a more relaxed
 * interpretation of the HTTP state management specification (RFC 6265, section 5)
 * for interoperability with existing servers that do not conform to the well behaved profile
 * (RFC 6265, section 4).
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class RFC6265LaxSpec extends RFC6265CookieSpecBase {

    public RFC6265LaxSpec() {
        super(new BasicPathHandler(),
                new BasicDomainHandler(),
                new LaxMaxAgeHandler(),
                new BasicSecureHandler(),
                new LaxExpiresHandler());
    }

    RFC6265LaxSpec(final CommonCookieAttributeHandler... handlers) {
        super(handlers);
    }

    @Override
    public String toString() {
        return "rfc6265-lax";
    }

}
