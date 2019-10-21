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
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CommonCookieAttributeHandler;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookiePriorityComparator;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.message.BufferedHeader;
import org.apache.http.message.ParserCursor;
import org.apache.http.message.TokenParser;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 * Cookie management functions shared by RFC C6265 compliant specification.
 *
 * @since 4.5
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class RFC6265CookieSpec implements CookieSpec {

    private final static char PARAM_DELIMITER  = ';';
    private final static char COMMA_CHAR       = ',';
    private final static char EQUAL_CHAR       = '=';
    private final static char DQUOTE_CHAR      = '"';
    private final static char ESCAPE_CHAR      = '\\';

    // IMPORTANT!
    // These private static variables must be treated as immutable and never exposed outside this class
    private static final BitSet TOKEN_DELIMS = TokenParser.INIT_BITSET(EQUAL_CHAR, PARAM_DELIMITER);
    private static final BitSet VALUE_DELIMS = TokenParser.INIT_BITSET(PARAM_DELIMITER);
    private static final BitSet SPECIAL_CHARS = TokenParser.INIT_BITSET(' ',
            DQUOTE_CHAR, COMMA_CHAR, PARAM_DELIMITER, ESCAPE_CHAR);

    private final CookieAttributeHandler[] attribHandlers;
    private final Map<String, CookieAttributeHandler> attribHandlerMap;
    private final TokenParser tokenParser;

    protected RFC6265CookieSpec(final CommonCookieAttributeHandler... handlers) {
        super();
        this.attribHandlers = handlers.clone();
        this.attribHandlerMap = new ConcurrentHashMap<String, CookieAttributeHandler>(handlers.length);
        for (final CommonCookieAttributeHandler handler: handlers) {
            this.attribHandlerMap.put(handler.getAttributeName().toLowerCase(Locale.ROOT), handler);
        }
        this.tokenParser = TokenParser.INSTANCE;
    }

    static String getDefaultPath(final CookieOrigin origin) {
        String defaultPath = origin.getPath();
        int lastSlashIndex = defaultPath.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            if (lastSlashIndex == 0) {
                //Do not remove the very first slash
                lastSlashIndex = 1;
            }
            defaultPath = defaultPath.substring(0, lastSlashIndex);
        }
        return defaultPath;
    }

    static String getDefaultDomain(final CookieOrigin origin) {
        return origin.getHost();
    }

    @Override
    public final List<Cookie> parse(final Header header, final CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        if (!header.getName().equalsIgnoreCase(SM.SET_COOKIE)) {
            throw new MalformedCookieException("Unrecognized cookie header: '" + header.toString() + "'");
        }
        final CharArrayBuffer buffer;
        final ParserCursor cursor;
        if (header instanceof FormattedHeader) {
            buffer = ((FormattedHeader) header).getBuffer();
            cursor = new ParserCursor(((FormattedHeader) header).getValuePos(), buffer.length());
        } else {
            final String s = header.getValue();
            if (s == null) {
                throw new MalformedCookieException("Header value is null");
            }
            buffer = new CharArrayBuffer(s.length());
            buffer.append(s);
            cursor = new ParserCursor(0, buffer.length());
        }
        final String name = tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS);
        if (name.length() == 0) {
            return Collections.emptyList();
        }
        if (cursor.atEnd()) {
            return Collections.emptyList();
        }
        final int valueDelim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (valueDelim != '=') {
            throw new MalformedCookieException("Cookie value is invalid: '" + header.toString() + "'");
        }
        final String value = tokenParser.parseValue(buffer, cursor, VALUE_DELIMS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        final BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(getDefaultPath(origin));
        cookie.setDomain(getDefaultDomain(origin));
        cookie.setCreationDate(new Date());

        final Map<String, String> attribMap = new LinkedHashMap<String, String>();
        while (!cursor.atEnd()) {
            final String paramName = tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS)
                    .toLowerCase(Locale.ROOT);
            String paramValue = null;
            if (!cursor.atEnd()) {
                final int paramDelim = buffer.charAt(cursor.getPos());
                cursor.updatePos(cursor.getPos() + 1);
                if (paramDelim == EQUAL_CHAR) {
                    paramValue = tokenParser.parseToken(buffer, cursor, VALUE_DELIMS);
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.getPos() + 1);
                    }
                }
            }
            cookie.setAttribute(paramName, paramValue);
            attribMap.put(paramName, paramValue);
        }
        // Ignore 'Expires' if 'Max-Age' is present
        if (attribMap.containsKey(ClientCookie.MAX_AGE_ATTR)) {
            attribMap.remove(ClientCookie.EXPIRES_ATTR);
        }

        for (final Map.Entry<String, String> entry: attribMap.entrySet()) {
            final String paramName = entry.getKey();
            final String paramValue = entry.getValue();
            final CookieAttributeHandler handler = this.attribHandlerMap.get(paramName);
            if (handler != null) {
                handler.parse(cookie, paramValue);
            }
        }

        return Collections.<Cookie>singletonList(cookie);
    }

    @Override
    public final void validate(final Cookie cookie, final CookieOrigin origin)
            throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (final CookieAttributeHandler handler: this.attribHandlers) {
            handler.validate(cookie, origin);
        }
    }

    @Override
    public final boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (final CookieAttributeHandler handler: this.attribHandlers) {
            if (!handler.match(cookie, origin)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Header> formatCookies(final List<Cookie> cookies) {
        Args.notEmpty(cookies, "List of cookies");
        final List<? extends Cookie> sortedCookies;
        if (cookies.size() > 1) {
            // Create a mutable copy and sort the copy.
            sortedCookies = new ArrayList<Cookie>(cookies);
            Collections.sort(sortedCookies, CookiePriorityComparator.INSTANCE);
        } else {
            sortedCookies = cookies;
        }
        final CharArrayBuffer buffer = new CharArrayBuffer(20 * sortedCookies.size());
        buffer.append(SM.COOKIE);
        buffer.append(": ");
        for (int n = 0; n < sortedCookies.size(); n++) {
            final Cookie cookie = sortedCookies.get(n);
            if (n > 0) {
                buffer.append(PARAM_DELIMITER);
                buffer.append(' ');
            }
            buffer.append(cookie.getName());
            final String s = cookie.getValue();
            if (s != null) {
                buffer.append(EQUAL_CHAR);
                if (containsSpecialChar(s)) {
                    buffer.append(DQUOTE_CHAR);
                    for (int i = 0; i < s.length(); i++) {
                        final char ch = s.charAt(i);
                        if (ch == DQUOTE_CHAR || ch == ESCAPE_CHAR) {
                            buffer.append(ESCAPE_CHAR);
                        }
                        buffer.append(ch);
                    }
                    buffer.append(DQUOTE_CHAR);
                } else {
                    buffer.append(s);
                }
            }
        }
        final List<Header> headers = new ArrayList<Header>(1);
        headers.add(new BufferedHeader(buffer));
        return headers;
    }

    boolean containsSpecialChar(final CharSequence s) {
        return containsChars(s, SPECIAL_CHARS);
    }

    boolean containsChars(final CharSequence s, final BitSet chars) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (chars.get(ch)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final int getVersion() {
        return 0;
    }

    @Override
    public final Header getVersionHeader() {
        return null;
    }

}
