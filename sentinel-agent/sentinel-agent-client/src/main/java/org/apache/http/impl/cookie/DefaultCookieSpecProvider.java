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
 * {@link org.apache.http.impl.cookie.DefaultCookieSpec}. The instance returned by this factory can
 * be shared by multiple threads.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultCookieSpecProvider implements CookieSpecProvider {

    public enum CompatibilityLevel {
        DEFAULT,
        IE_MEDIUM_SECURITY
    }

    private final CompatibilityLevel compatibilityLevel;
    private final PublicSuffixMatcher publicSuffixMatcher;
    private final String[] datepatterns;
    private final boolean oneHeader;

    private volatile CookieSpec cookieSpec;

    public DefaultCookieSpecProvider(
            final CompatibilityLevel compatibilityLevel,
            final PublicSuffixMatcher publicSuffixMatcher,
            final String[] datepatterns,
            final boolean oneHeader) {
        super();
        this.compatibilityLevel = compatibilityLevel != null ? compatibilityLevel : CompatibilityLevel.DEFAULT;
        this.publicSuffixMatcher = publicSuffixMatcher;
        this.datepatterns = datepatterns;
        this.oneHeader = oneHeader;
    }

    public DefaultCookieSpecProvider(
            final CompatibilityLevel compatibilityLevel,
            final PublicSuffixMatcher publicSuffixMatcher) {
        this(compatibilityLevel, publicSuffixMatcher, null, false);
    }

    public DefaultCookieSpecProvider(final PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.DEFAULT, publicSuffixMatcher, null, false);
    }

    public DefaultCookieSpecProvider() {
        this(CompatibilityLevel.DEFAULT, null, null, false);
    }

    @Override
    public CookieSpec create(final HttpContext context) {
        if (cookieSpec == null) {
            synchronized (this) {
                if (cookieSpec == null) {
                    final RFC2965Spec strict = new RFC2965Spec(this.oneHeader,
                            new RFC2965VersionAttributeHandler(),
                            new BasicPathHandler(),
                            PublicSuffixDomainFilter.decorate(
                                    new RFC2965DomainAttributeHandler(), this.publicSuffixMatcher),
                            new RFC2965PortAttributeHandler(),
                            new BasicMaxAgeHandler(),
                            new BasicSecureHandler(),
                            new BasicCommentHandler(),
                            new RFC2965CommentUrlAttributeHandler(),
                            new RFC2965DiscardAttributeHandler());
                    final RFC2109Spec obsoleteStrict = new RFC2109Spec(this.oneHeader,
                            new RFC2109VersionHandler(),
                            new BasicPathHandler(),
                            PublicSuffixDomainFilter.decorate(
                                    new RFC2109DomainHandler(), this.publicSuffixMatcher),
                            new BasicMaxAgeHandler(),
                            new BasicSecureHandler(),
                            new BasicCommentHandler());
                    final NetscapeDraftSpec netscapeDraft = new NetscapeDraftSpec(
                            PublicSuffixDomainFilter.decorate(
                                    new BasicDomainHandler(), this.publicSuffixMatcher),
                            this.compatibilityLevel == CompatibilityLevel.IE_MEDIUM_SECURITY ?
                                    new BasicPathHandler() {
                                        @Override
                                        public void validate(
                                                final Cookie cookie,
                                                final CookieOrigin origin) throws MalformedCookieException {
                                            // No validation
                                        }
                                    } : new BasicPathHandler(),
                            new BasicSecureHandler(),
                            new BasicCommentHandler(),
                            new BasicExpiresHandler(this.datepatterns != null ? this.datepatterns.clone() :
                                    new String[]{NetscapeDraftSpec.EXPIRES_PATTERN}));
                    this.cookieSpec = new DefaultCookieSpec(strict, obsoleteStrict, netscapeDraft);
                }
            }
        }
        return this.cookieSpec;
    }

}
