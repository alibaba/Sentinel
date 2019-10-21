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
import java.util.StringTokenizer;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;
import org.apache.http.util.TextUtils;

/**
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class NetscapeDomainHandler extends BasicDomainHandler {

    public NetscapeDomainHandler() {
        super();
    }

    @Override
    public void parse(final SetCookie cookie, final String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (TextUtils.isBlank(value)) {
            throw new MalformedCookieException("Blank or null value for domain attribute");
        }
        cookie.setDomain(value);
    }

    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin)
            throws MalformedCookieException {
        final String host = origin.getHost();
        final String domain = cookie.getDomain();
        if (!host.equals(domain) && !BasicDomainHandler.domainMatch(domain, host)) {
            throw new CookieRestrictionViolationException(
                    "Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
        }
        if (host.contains(".")) {
            final int domainParts = new StringTokenizer(domain, ".").countTokens();

            if (isSpecialDomain(domain)) {
                if (domainParts < 2) {
                    throw new CookieRestrictionViolationException("Domain attribute \""
                        + domain
                        + "\" violates the Netscape cookie specification for "
                        + "special domains");
                }
            } else {
                if (domainParts < 3) {
                    throw new CookieRestrictionViolationException("Domain attribute \""
                        + domain
                        + "\" violates the Netscape cookie specification");
                }
            }
        }
    }

   /**
    * Checks if the given domain is in one of the seven special
    * top level domains defined by the Netscape cookie specification.
    * @param domain The domain.
    * @return True if the specified domain is "special"
    */
   private static boolean isSpecialDomain(final String domain) {
       final String ucDomain = domain.toUpperCase(Locale.ROOT);
       return ucDomain.endsWith(".COM")
               || ucDomain.endsWith(".EDU")
               || ucDomain.endsWith(".NET")
               || ucDomain.endsWith(".GOV")
               || ucDomain.endsWith(".MIL")
               || ucDomain.endsWith(".ORG")
               || ucDomain.endsWith(".INT");
   }

   @Override
   public boolean match(final Cookie cookie, final CookieOrigin origin) {
       Args.notNull(cookie, "Cookie");
       Args.notNull(origin, "Cookie origin");
       final String host = origin.getHost();
       final String domain = cookie.getDomain();
       if (domain == null) {
           return false;
       }
       return host.endsWith(domain);
   }

    @Override
    public String getAttributeName() {
        return ClientCookie.DOMAIN_ATTR;
    }

}
