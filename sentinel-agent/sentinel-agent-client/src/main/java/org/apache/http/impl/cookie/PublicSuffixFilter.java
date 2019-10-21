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

import java.util.Collection;

import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

/**
 * Wraps a CookieAttributeHandler and leverages its match method
 * to never match a suffix from a black list. May be used to provide
 * additional security for cross-site attack types by preventing
 * cookies from apparent domains that are not publicly available.
 * An uptodate list of suffixes can be obtained from
 * <a href="http://publicsuffix.org/">publicsuffix.org</a>
 *
 * @deprecated (4.4) use {@link org.apache.http.impl.cookie.PublicSuffixDomainFilter}
 *
 * @since 4.0
 */
@Deprecated
public class PublicSuffixFilter implements CookieAttributeHandler {
    private final CookieAttributeHandler wrapped;
    private Collection<String> exceptions;
    private Collection<String> suffixes;
    private PublicSuffixMatcher matcher;

    public PublicSuffixFilter(final CookieAttributeHandler wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Sets the suffix blacklist patterns.
     * A pattern can be "com", "*.jp"
     * TODO add support for patterns like "lib.*.us"
     * @param suffixes
     */
    public void setPublicSuffixes(final Collection<String> suffixes) {
        this.suffixes = suffixes;
        this.matcher = null;
    }

    /**
     * Sets the exceptions from the blacklist. Exceptions can not be patterns.
     * TODO add support for patterns
     * @param exceptions
     */
    public void setExceptions(final Collection<String> exceptions) {
        this.exceptions = exceptions;
        this.matcher = null;
    }

    /**
     * Never matches if the cookie's domain is from the blacklist.
     */
    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        if (isForPublicSuffix(cookie)) {
            return false;
        }
        return wrapped.match(cookie, origin);
    }

    @Override
    public void parse(final SetCookie cookie, final String value) throws MalformedCookieException {
        wrapped.parse(cookie, value);
    }

    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
        wrapped.validate(cookie, origin);
    }

    private boolean isForPublicSuffix(final Cookie cookie) {
        if (matcher == null) {
            matcher = new PublicSuffixMatcher(this.suffixes, this.exceptions);
        }
        return matcher.matches(cookie.getDomain());
    }
}
