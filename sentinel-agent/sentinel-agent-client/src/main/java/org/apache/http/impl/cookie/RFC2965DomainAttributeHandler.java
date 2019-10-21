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

import java.util.Locale;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CommonCookieAttributeHandler;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

/**
 * {@code "Domain"} cookie attribute handler for RFC 2965 cookie spec.
 *
 *
 * @since 3.1
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RFC2965DomainAttributeHandler implements CommonCookieAttributeHandler {

    public RFC2965DomainAttributeHandler() {
        super();
    }

    /**
     * Parse cookie domain attribute.
     */
    @Override
    public void parse(
            final SetCookie cookie, final String domain) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (domain == null) {
            throw new MalformedCookieException(
                    "Missing value for domain attribute");
        }
        if (domain.trim().isEmpty()) {
            throw new MalformedCookieException(
                    "Blank value for domain attribute");
        }
        String s = domain;
        s = s.toLowerCase(Locale.ROOT);
        if (!domain.startsWith(".")) {
            // Per RFC 2965 section 3.2.2
            // "... If an explicitly specified value does not start with
            // a dot, the user agent supplies a leading dot ..."
            // That effectively implies that the domain attribute
            // MAY NOT be an IP address of a host name
            s = '.' + s;
        }
        cookie.setDomain(s);
    }

    /**
     * Performs domain-match as defined by the RFC2965.
     * <p>
     * Host A's name domain-matches host B's if
     * </p>
     * <ol>
     *   <li>their host name strings string-compare equal; or</li>
     *   <li>A is a HDN string and has the form NB, where N is a non-empty
     *       name string, B has the form .B', and B' is a HDN string.  (So,
     *       x.y.com domain-matches .Y.com but not Y.com.)</li>
     * </ol>
     *
     * @param host host name where cookie is received from or being sent to.
     * @param domain The cookie domain attribute.
     * @return true if the specified host matches the given domain.
     */
    public boolean domainMatch(final String host, final String domain) {
        final boolean match = host.equals(domain)
                        || (domain.startsWith(".") && host.endsWith(domain));

        return match;
    }

    /**
     * Validate cookie domain attribute.
     */
    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin)
            throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        final String host = origin.getHost().toLowerCase(Locale.ROOT);
        if (cookie.getDomain() == null) {
            throw new CookieRestrictionViolationException("Invalid cookie state: " +
                                               "domain not specified");
        }
        final String cookieDomain = cookie.getDomain().toLowerCase(Locale.ROOT);

        if (cookie instanceof ClientCookie
                && ((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
            // Domain attribute must start with a dot
            if (!cookieDomain.startsWith(".")) {
                throw new CookieRestrictionViolationException("Domain attribute \"" +
                    cookie.getDomain() + "\" violates RFC 2109: domain must start with a dot");
            }

            // Domain attribute must contain at least one embedded dot,
            // or the value must be equal to .local.
            final int dotIndex = cookieDomain.indexOf('.', 1);
            if (((dotIndex < 0) || (dotIndex == cookieDomain.length() - 1))
                && (!cookieDomain.equals(".local"))) {
                throw new CookieRestrictionViolationException(
                        "Domain attribute \"" + cookie.getDomain()
                        + "\" violates RFC 2965: the value contains no embedded dots "
                        + "and the value is not .local");
            }

            // The effective host name must domain-match domain attribute.
            if (!domainMatch(host, cookieDomain)) {
                throw new CookieRestrictionViolationException(
                        "Domain attribute \"" + cookie.getDomain()
                        + "\" violates RFC 2965: effective host name does not "
                        + "domain-match domain attribute.");
            }

            // effective host name minus domain must not contain any dots
            final String effectiveHostWithoutDomain = host.substring(
                    0, host.length() - cookieDomain.length());
            if (effectiveHostWithoutDomain.indexOf('.') != -1) {
                throw new CookieRestrictionViolationException("Domain attribute \""
                                                   + cookie.getDomain() + "\" violates RFC 2965: "
                                                   + "effective host minus domain may not contain any dots");
            }
        } else {
            // Domain was not specified in header. In this case, domain must
            // string match request host (case-insensitive).
            if (!cookie.getDomain().equals(host)) {
                throw new CookieRestrictionViolationException("Illegal domain attribute: \""
                                                   + cookie.getDomain() + "\"."
                                                   + "Domain of origin: \""
                                                   + host + "\"");
            }
        }
    }

    /**
     * Match cookie domain attribute.
     */
    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        final String host = origin.getHost().toLowerCase(Locale.ROOT);
        final String cookieDomain = cookie.getDomain();

        // The effective host name MUST domain-match the Domain
        // attribute of the cookie.
        if (!domainMatch(host, cookieDomain)) {
            return false;
        }
        // effective host name minus domain must not contain any dots
        final String effectiveHostWithoutDomain = host.substring(
                0, host.length() - cookieDomain.length());
        return effectiveHostWithoutDomain.indexOf('.') == -1;
    }

    @Override
    public String getAttributeName() {
        return ClientCookie.DOMAIN_ATTR;
    }

}
