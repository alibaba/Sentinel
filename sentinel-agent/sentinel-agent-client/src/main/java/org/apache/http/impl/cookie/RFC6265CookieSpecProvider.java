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
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.protocol.HttpContext;

/**
 * {@link org.apache.http.cookie.CookieSpecProvider} implementation that provides an instance of
 * RFC 6265 conformant cookie policy. The instance returned by this factory can be shared by
 * multiple threads.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class RFC6265CookieSpecProvider implements CookieSpecProvider {

    public enum CompatibilityLevel {
        STRICT,
        RELAXED,
        IE_MEDIUM_SECURITY
    }

    private final CompatibilityLevel compatibilityLevel;
    private final PublicSuffixMatcher publicSuffixMatcher;

    private volatile CookieSpec cookieSpec;

    public RFC6265CookieSpecProvider(
            final CompatibilityLevel compatibilityLevel,
            final PublicSuffixMatcher publicSuffixMatcher) {
        super();
        this.compatibilityLevel = compatibilityLevel != null ? compatibilityLevel : CompatibilityLevel.RELAXED;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC6265CookieSpecProvider(final PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.RELAXED, publicSuffixMatcher);
    }

    public RFC6265CookieSpecProvider() {
        this(CompatibilityLevel.RELAXED, null);
    }

    @Override
    public CookieSpec create(final HttpContext context) {
        if (cookieSpec == null) {
            synchronized (this) {
                if (cookieSpec == null) {
                    switch (this.compatibilityLevel) {
                        case STRICT:
                            this.cookieSpec = new RFC6265StrictSpec(
                                    new BasicPathHandler(),
                                    PublicSuffixDomainFilter.decorate(
                                            new BasicDomainHandler(), this.publicSuffixMatcher),
                                    new BasicMaxAgeHandler(),
                                    new BasicSecureHandler(),
                                    new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        case IE_MEDIUM_SECURITY:
                            this.cookieSpec = new RFC6265LaxSpec(
                                    new BasicPathHandler() {
                                        @Override
                                        public void validate(
                                                final Cookie cookie,
                                                final CookieOrigin origin) throws MalformedCookieException {
                                            // No validation
                                        }
                                    },
                                    PublicSuffixDomainFilter.decorate(
                                            new BasicDomainHandler(), this.publicSuffixMatcher),
                                    new BasicMaxAgeHandler(),
                                    new BasicSecureHandler(),
                                    new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        default:
                            this.cookieSpec = new RFC6265LaxSpec(
                                    new BasicPathHandler(),
                                    PublicSuffixDomainFilter.decorate(
                                            new BasicDomainHandler(), this.publicSuffixMatcher),
                                    new LaxMaxAgeHandler(),
                                    new BasicSecureHandler(),
                                    new LaxExpiresHandler());
                    }
                }
            }
        }
        return this.cookieSpec;
    }

}
