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

package org.apache.http.client.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.message.TokenParser;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 * A collection of utilities for encoding URLs.
 *
 * @since 4.0
 */
public class URLEncodedUtils {

    /**
     * The default HTML form content type.
     */
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final char QP_SEP_A = '&';
    private static final char QP_SEP_S = ';';
    private static final String NAME_VALUE_SEPARATOR = "=";

    /**
     * @deprecated 4.5 Use {@link #parse(URI, Charset)}
     */
    public static List <NameValuePair> parse(final URI uri, final String charsetName) {
        return parse(uri, charsetName != null ? Charset.forName(charsetName) : null);
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as built from the URI's query portion. For example, a URI
     * of {@code http://example.org/path/to/file?a=1&b=2&c=3} would return a list of three NameValuePairs, one for a=1,
     * one for b=2, and one for c=3. By convention, {@code '&'} and {@code ';'} are accepted as parameter separators.
     * <p>
     * This is typically useful while parsing an HTTP PUT.
     *
     * This API is currently only used for testing.
     *
     * @param uri
     *        URI to parse
     * @param charset
     *        Charset to use while parsing the query
     * @return a list of {@link NameValuePair} as built from the URI's query portion.
     *
     * @since 4.5
     */
    public static List <NameValuePair> parse(final URI uri, final Charset charset) {
        Args.notNull(uri, "URI");
        final String query = uri.getRawQuery();
        if (query != null && !query.isEmpty()) {
            return parse(query, charset);
        }
        return Collections.emptyList();
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as parsed from an {@link HttpEntity}.
     * The encoding is taken from the entity's Content-Encoding header.
     * <p>
     * This is typically used while parsing an HTTP POST.
     *
     * @param entity
     *            The entity to parse
     * @return a list of {@link NameValuePair} as built from the URI's query portion.
     * @throws IOException
     *             If there was an exception getting the entity's data.
     */
    public static List <NameValuePair> parse(
            final HttpEntity entity) throws IOException {
        Args.notNull(entity, "HTTP entity");
        final ContentType contentType = ContentType.get(entity);
        if (contentType == null || !contentType.getMimeType().equalsIgnoreCase(CONTENT_TYPE)) {
            return Collections.emptyList();
        }
        final long len = entity.getContentLength();
        Args.check(len <= Integer.MAX_VALUE, "HTTP entity is too large");
        final Charset charset = contentType.getCharset() != null ? contentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return Collections.emptyList();
        }
        final CharArrayBuffer buf;
        try {
            buf = new CharArrayBuffer(len > 0 ? (int) len : 1024);
            final Reader reader = new InputStreamReader(instream, charset);
            final char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                buf.append(tmp, 0, l);
            }

        } finally {
            instream.close();
        }
        if (buf.length() == 0) {
            return Collections.emptyList();
        }
        return parse(buf, charset, QP_SEP_A);
    }

    /**
     * Returns true if the entity's Content-Type header is
     * {@code application/x-www-form-urlencoded}.
     */
    public static boolean isEncoded(final HttpEntity entity) {
        Args.notNull(entity, "HTTP entity");
        final Header h = entity.getContentType();
        if (h != null) {
            final HeaderElement[] elems = h.getElements();
            if (elems.length > 0) {
                final String contentType = elems[0].getName();
                return contentType.equalsIgnoreCase(CONTENT_TYPE);
            }
        }
        return false;
    }

    /**
     * Adds all parameters within the Scanner to the list of {@code parameters}, as encoded by
     * {@code encoding}. For example, a scanner containing the string {@code a=1&b=2&c=3} would add the
     * {@link NameValuePair NameValuePairs} a=1, b=2, and c=3 to the list of parameters. By convention, {@code '&'} and
     * {@code ';'} are accepted as parameter separators.
     *
     * @param parameters
     *            List to add parameters to.
     * @param scanner
     *            Input that contains the parameters to parse.
     * @param charset
     *            Encoding to use when decoding the parameters.
     *
     * @deprecated (4.4) use {@link #parse(String, java.nio.charset.Charset)}
     */
    @Deprecated
    public static void parse(
            final List<NameValuePair> parameters,
            final Scanner scanner,
            final String charset) {
        parse(parameters, scanner, "[" + QP_SEP_A + QP_SEP_S + "]", charset);
    }

    /**
     * Adds all parameters within the Scanner to the list of
     * {@code parameters}, as encoded by {@code encoding}. For
     * example, a scanner containing the string {@code a=1&b=2&c=3} would
     * add the {@link NameValuePair NameValuePairs} a=1, b=2, and c=3 to the
     * list of parameters.
     *
     * @param parameters
     *            List to add parameters to.
     * @param scanner
     *            Input that contains the parameters to parse.
     * @param parameterSepartorPattern
     *            The Pattern string for parameter separators, by convention {@code "[&;]"}
     * @param charset
     *            Encoding to use when decoding the parameters.
     *
     * @deprecated (4.4) use {@link #parse(org.apache.http.util.CharArrayBuffer, java.nio.charset.Charset, char...)}
     */
    @Deprecated
    public static void parse(
            final List <NameValuePair> parameters,
            final Scanner scanner,
            final String parameterSepartorPattern,
            final String charset) {
        scanner.useDelimiter(parameterSepartorPattern);
        while (scanner.hasNext()) {
            final String name;
            final String value;
            final String token = scanner.next();
            final int i = token.indexOf(NAME_VALUE_SEPARATOR);
            if (i != -1) {
                name = decodeFormFields(token.substring(0, i).trim(), charset);
                value = decodeFormFields(token.substring(i + 1).trim(), charset);
            } else {
                name = decodeFormFields(token.trim(), charset);
                value = null;
            }
            parameters.add(new BasicNameValuePair(name, value));
        }
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as parsed from the given string using the given character
     * encoding. By convention, {@code '&'} and {@code ';'} are accepted as parameter separators.
     *
     * @param s
     *            text to parse.
     * @param charset
     *            Encoding to use when decoding the parameters.
     * @return a list of {@link NameValuePair} as built from the URI's query portion.
     *
     * @since 4.2
     */
    public static List<NameValuePair> parse(final String s, final Charset charset) {
        if (s == null) {
            return Collections.emptyList();
        }
        final CharArrayBuffer buffer = new CharArrayBuffer(s.length());
        buffer.append(s);
        return parse(buffer, charset, QP_SEP_A, QP_SEP_S);
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as parsed from the given string using the given character
     * encoding.
     *
     * @param s
     *            text to parse.
     * @param charset
     *            Encoding to use when decoding the parameters.
     * @param separators
     *            element separators.
     * @return a list of {@link NameValuePair} as built from the URI's query portion.
     *
     * @since 4.3
     */
    public static List<NameValuePair> parse(final String s, final Charset charset, final char... separators) {
        if (s == null) {
            return Collections.emptyList();
        }
        final CharArrayBuffer buffer = new CharArrayBuffer(s.length());
        buffer.append(s);
        return parse(buffer, charset, separators);
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as parsed from the given string using
     * the given character encoding.
     *
     * @param buf
     *            text to parse.
     * @param charset
     *            Encoding to use when decoding the parameters.
     * @param separators
     *            element separators.
     * @return a list of {@link NameValuePair} as built from the URI's query portion.
     *
     * @since 4.4
     */
    public static List<NameValuePair> parse(
            final CharArrayBuffer buf, final Charset charset, final char... separators) {
        Args.notNull(buf, "Char array buffer");
        final TokenParser tokenParser = TokenParser.INSTANCE;
        final BitSet delimSet = new BitSet();
        for (final char separator: separators) {
            delimSet.set(separator);
        }
        final ParserCursor cursor = new ParserCursor(0, buf.length());
        final List<NameValuePair> list = new ArrayList<NameValuePair>();
        while (!cursor.atEnd()) {
            delimSet.set('=');
            final String name = tokenParser.parseToken(buf, cursor, delimSet);
            String value = null;
            if (!cursor.atEnd()) {
                final int delim = buf.charAt(cursor.getPos());
                cursor.updatePos(cursor.getPos() + 1);
                if (delim == '=') {
                    delimSet.clear('=');
                    value = tokenParser.parseValue(buf, cursor, delimSet);
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.getPos() + 1);
                    }
                }
            }
            if (!name.isEmpty()) {
                list.add(new BasicNameValuePair(
                        decodeFormFields(name, charset),
                        decodeFormFields(value, charset)));
            }
        }
        return list;
    }

    /**
     * Returns a String that is suitable for use as an {@code application/x-www-form-urlencoded}
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param charset The encoding to use.
     * @return An {@code application/x-www-form-urlencoded} string
     */
    public static String format(
            final List <? extends NameValuePair> parameters,
            final String charset) {
        return format(parameters, QP_SEP_A, charset);
    }

    /**
     * Returns a String that is suitable for use as an {@code application/x-www-form-urlencoded}
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param parameterSeparator The parameter separator, by convention, {@code '&'} or {@code ';'}.
     * @param charset The encoding to use.
     * @return An {@code application/x-www-form-urlencoded} string
     *
     * @since 4.3
     */
    public static String format(
            final List <? extends NameValuePair> parameters,
            final char parameterSeparator,
            final String charset) {
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            final String encodedName = encodeFormFields(parameter.getName(), charset);
            final String encodedValue = encodeFormFields(parameter.getValue(), charset);
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    /**
     * Returns a String that is suitable for use as an {@code application/x-www-form-urlencoded}
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param charset The encoding to use.
     * @return An {@code application/x-www-form-urlencoded} string
     *
     * @since 4.2
     */
    public static String format(
            final Iterable<? extends NameValuePair> parameters,
            final Charset charset) {
        return format(parameters, QP_SEP_A, charset);
    }

    /**
     * Returns a String that is suitable for use as an {@code application/x-www-form-urlencoded}
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param parameterSeparator The parameter separator, by convention, {@code '&'} or {@code ';'}.
     * @param charset The encoding to use.
     * @return An {@code application/x-www-form-urlencoded} string
     *
     * @since 4.3
     */
    public static String format(
            final Iterable<? extends NameValuePair> parameters,
            final char parameterSeparator,
            final Charset charset) {
        Args.notNull(parameters, "Parameters");
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            final String encodedName = encodeFormFields(parameter.getName(), charset);
            final String encodedValue = encodeFormFields(parameter.getValue(), charset);
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    /**
     * Unreserved characters, i.e. alphanumeric, plus: {@code _ - ! . ~ ' ( ) *}
     * <p>
     *  This list is the same as the {@code unreserved} list in
     *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
     */
    private static final BitSet UNRESERVED   = new BitSet(256);
    /**
     * Punctuation characters: , ; : $ & + =
     * <p>
     * These are the additional characters allowed by userinfo.
     */
    private static final BitSet PUNCT        = new BitSet(256);
    /** Characters which are safe to use in userinfo,
     * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation */
    private static final BitSet USERINFO     = new BitSet(256);
    /** Characters which are safe to use in a path,
     * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation plus / @ */
    private static final BitSet PATHSAFE     = new BitSet(256);
    /** Characters which are safe to use in a query or a fragment,
     * i.e. {@link #RESERVED} plus {@link #UNRESERVED} */
    private static final BitSet URIC     = new BitSet(256);

    /**
     * Reserved characters, i.e. {@code ;/?:@&=+$,[]}
     * <p>
     *  This list is the same as the {@code reserved} list in
     *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
     *  as augmented by
     *  <a href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a>
     */
    private static final BitSet RESERVED     = new BitSet(256);


    /**
     * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
     * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
     */
    private static final BitSet URLENCODER   = new BitSet(256);

    static {
        // unreserved chars
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set('_'); // these are the charactes of the "mark" list
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('*');
        URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
        UNRESERVED.set('!');
        UNRESERVED.set('~');
        UNRESERVED.set('\'');
        UNRESERVED.set('(');
        UNRESERVED.set(')');
        // punct chars
        PUNCT.set(',');
        PUNCT.set(';');
        PUNCT.set(':');
        PUNCT.set('$');
        PUNCT.set('&');
        PUNCT.set('+');
        PUNCT.set('=');
        // Safe for userinfo
        USERINFO.or(UNRESERVED);
        USERINFO.or(PUNCT);

        // URL path safe
        PATHSAFE.or(UNRESERVED);
        PATHSAFE.set('/'); // segment separator
        PATHSAFE.set(';'); // param separator
        PATHSAFE.set(':'); // rest as per list in 2396, i.e. : @ & = + $ ,
        PATHSAFE.set('@');
        PATHSAFE.set('&');
        PATHSAFE.set('=');
        PATHSAFE.set('+');
        PATHSAFE.set('$');
        PATHSAFE.set(',');

        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
        RESERVED.set('['); // added by RFC 2732
        RESERVED.set(']'); // added by RFC 2732

        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
    }

    private static final int RADIX = 16;

    private static String urlEncode(
            final String content,
            final Charset charset,
            final BitSet safechars,
            final boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        final ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            final int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }

    /**
     * Decode/unescape a portion of a URL, to use with the query part ensure {@code plusAsBlank} is true.
     *
     * @param content the portion to decode
     * @param charset the charset to use
     * @param plusAsBlank if {@code true}, then convert '+' to space (e.g. for www-url-form-encoded content), otherwise leave as is.
     * @return encoded string
     */
    private static String urlDecode(
            final String content,
            final Charset charset,
            final boolean plusAsBlank) {
        if (content == null) {
            return null;
        }
        final ByteBuffer bb = ByteBuffer.allocate(content.length());
        final CharBuffer cb = CharBuffer.wrap(content);
        while (cb.hasRemaining()) {
            final char c = cb.get();
            if (c == '%' && cb.remaining() >= 2) {
                final char uc = cb.get();
                final char lc = cb.get();
                final int u = Character.digit(uc, 16);
                final int l = Character.digit(lc, 16);
                if (u != -1 && l != -1) {
                    bb.put((byte) ((u << 4) + l));
                } else {
                    bb.put((byte) '%');
                    bb.put((byte) uc);
                    bb.put((byte) lc);
                }
            } else if (plusAsBlank && c == '+') {
                bb.put((byte) ' ');
            } else {
                bb.put((byte) c);
            }
        }
        bb.flip();
        return charset.decode(bb).toString();
    }

    /**
     * Decode/unescape www-url-form-encoded content.
     *
     * @param content the content to decode, will decode '+' as space
     * @param charset the charset to use
     * @return encoded string
     */
    private static String decodeFormFields (final String content, final String charset) {
        if (content == null) {
            return null;
        }
        return urlDecode(content, charset != null ? Charset.forName(charset) : Consts.UTF_8, true);
    }

    /**
     * Decode/unescape www-url-form-encoded content.
     *
     * @param content the content to decode, will decode '+' as space
     * @param charset the charset to use
     * @return encoded string
     */
    private static String decodeFormFields (final String content, final Charset charset) {
        if (content == null) {
            return null;
        }
        return urlDecode(content, charset != null ? charset : Consts.UTF_8, true);
    }

    /**
     * Encode/escape www-url-form-encoded content.
     * <p>
     * Uses the {@link #URLENCODER} set of characters, rather than
     * the {@link #UNRESERVED} set; this is for compatibilty with previous
     * releases, URLEncoder.encode() and most browsers.
     *
     * @param content the content to encode, will convert space to '+'
     * @param charset the charset to use
     * @return encoded string
     */
    private static String encodeFormFields(final String content, final String charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? Charset.forName(charset) : Consts.UTF_8, URLENCODER, true);
    }

    /**
     * Encode/escape www-url-form-encoded content.
     * <p>
     * Uses the {@link #URLENCODER} set of characters, rather than
     * the {@link #UNRESERVED} set; this is for compatibilty with previous
     * releases, URLEncoder.encode() and most browsers.
     *
     * @param content the content to encode, will convert space to '+'
     * @param charset the charset to use
     * @return encoded string
     */
    private static String encodeFormFields (final String content, final Charset charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? charset : Consts.UTF_8, URLENCODER, true);
    }

    /**
     * Encode a String using the {@link #USERINFO} set of characters.
     * <p>
     * Used by URIBuilder to encode the userinfo segment.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    static String encUserInfo(final String content, final Charset charset) {
        return urlEncode(content, charset, USERINFO, false);
    }

    /**
     * Encode a String using the {@link #URIC} set of characters.
     * <p>
     * Used by URIBuilder to encode the query and fragment segments.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    static String encUric(final String content, final Charset charset) {
        return urlEncode(content, charset, URIC, false);
    }

    /**
     * Encode a String using the {@link #PATHSAFE} set of characters.
     * <p>
     * Used by URIBuilder to encode path segments.
     *
     * @param content the string to encode, does not convert space to '+'
     * @param charset the charset to use
     * @return the encoded string
     */
    static String encPath(final String content, final Charset charset) {
        return urlEncode(content, charset, PATHSAFE, false);
    }

}
