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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BufferedHeader;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;


/**
 * Cookie specification that strives to closely mimic (mis)behavior of
 * common web browser applications such as Microsoft Internet Explorer
 * and Mozilla FireFox.
 *
 * @deprecated (4.4) use {@link org.apache.http.impl.cookie.DefaultCookieSpec}.
 *
 * @since 4.0
 */
@Deprecated
@Contract(threading = ThreadingBehavior.SAFE)
public class BrowserCompatSpec extends CookieSpecBase {


    private static final String[] DEFAULT_DATE_PATTERNS = new String[] {
        DateUtils.PATTERN_RFC1123,
        DateUtils.PATTERN_RFC1036,
        DateUtils.PATTERN_ASCTIME,
        "EEE, dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MMM-yyyy HH-mm-ss z",
        "EEE, dd MMM yy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH:mm:ss z",
        "EEE dd MMM yyyy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH-mm-ss z",
        "EEE dd-MMM-yy HH:mm:ss z",
        "EEE dd MMM yy HH:mm:ss z",
        "EEE,dd-MMM-yy HH:mm:ss z",
        "EEE,dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MM-yyyy HH:mm:ss z",
    };

    /** Default constructor */
    public BrowserCompatSpec(final String[] datepatterns, final BrowserCompatSpecFactory.SecurityLevel securityLevel) {
        super(new BrowserCompatVersionAttributeHandler(),
                new BasicDomainHandler(),
                securityLevel == BrowserCompatSpecFactory.SecurityLevel.SECURITYLEVEL_IE_MEDIUM ?
                        new BasicPathHandler() {
                            @Override
                            public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
                                // No validation
                            }
                        } : new BasicPathHandler(),
                new BasicMaxAgeHandler(),
                new BasicSecureHandler(),
                new BasicCommentHandler(),
                new BasicExpiresHandler(datepatterns != null ? datepatterns.clone() : DEFAULT_DATE_PATTERNS));
    }

    /** Default constructor */
    public BrowserCompatSpec(final String[] datepatterns) {
        this(datepatterns, BrowserCompatSpecFactory.SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    /** Default constructor */
    public BrowserCompatSpec() {
        this(null, BrowserCompatSpecFactory.SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    @Override
    public List<Cookie> parse(final Header header, final CookieOrigin origin)
            throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        final String headername = header.getName();
        if (!headername.equalsIgnoreCase(SM.SET_COOKIE)) {
            throw new MalformedCookieException("Unrecognized cookie header '"
                    + header.toString() + "'");
        }
        final HeaderElement[] helems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (final HeaderElement helem: helems) {
            if (helem.getParameterByName("version") != null) {
                versioned = true;
            }
            if (helem.getParameterByName("expires") != null) {
               netscape = true;
            }
        }
        if (netscape || !versioned) {
            // Need to parse the header again, because Netscape style cookies do not correctly
            // support multiple header elements (comma cannot be treated as an element separator)
            final NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
            final CharArrayBuffer buffer;
            final ParserCursor cursor;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                cursor = new ParserCursor(
                        ((FormattedHeader) header).getValuePos(),
                        buffer.length());
            } else {
                final String s = header.getValue();
                if (s == null) {
                    throw new MalformedCookieException("Header value is null");
                }
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                cursor = new ParserCursor(0, buffer.length());
            }
            final HeaderElement elem = parser.parseHeader(buffer, cursor);
            final String name = elem.getName();
            final String value = elem.getValue();
            if (name == null || name.isEmpty()) {
                throw new MalformedCookieException("Cookie name may not be empty");
            }
            final BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setPath(getDefaultPath(origin));
            cookie.setDomain(getDefaultDomain(origin));

            // cycle through the parameters
            final NameValuePair[] attribs = elem.getParameters();
            for (int j = attribs.length - 1; j >= 0; j--) {
                final NameValuePair attrib = attribs[j];
                final String s = attrib.getName().toLowerCase(Locale.ROOT);
                cookie.setAttribute(s, attrib.getValue());
                final CookieAttributeHandler handler = findAttribHandler(s);
                if (handler != null) {
                    handler.parse(cookie, attrib.getValue());
                }
            }
            // Override version for Netscape style cookies
            if (netscape) {
                cookie.setVersion(0);
            }
            return Collections.<Cookie>singletonList(cookie);
        } else {
            return parse(helems, origin);
        }
    }

    private static boolean isQuoteEnclosed(final String s) {
        return s != null && s.startsWith("\"") && s.endsWith("\"");
    }

    @Override
    public List<Header> formatCookies(final List<Cookie> cookies) {
        Args.notEmpty(cookies, "List of cookies");
        final CharArrayBuffer buffer = new CharArrayBuffer(20 * cookies.size());
        buffer.append(SM.COOKIE);
        buffer.append(": ");
        for (int i = 0; i < cookies.size(); i++) {
            final Cookie cookie = cookies.get(i);
            if (i > 0) {
                buffer.append("; ");
            }
            final String cookieName = cookie.getName();
            final String cookieValue = cookie.getValue();
            if (cookie.getVersion() > 0 && !isQuoteEnclosed(cookieValue)) {
                BasicHeaderValueFormatter.INSTANCE.formatHeaderElement(
                        buffer,
                        new BasicHeaderElement(cookieName, cookieValue),
                        false);
            } else {
                // Netscape style cookies do not support quoted values
                buffer.append(cookieName);
                buffer.append("=");
                if (cookieValue != null) {
                    buffer.append(cookieValue);
                }
            }
        }
        final List<Header> headers = new ArrayList<Header>(1);
        headers.add(new BufferedHeader(buffer));
        return headers;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public Header getVersionHeader() {
        return null;
    }

    @Override
    public String toString() {
        return "compatibility";
    }

}
