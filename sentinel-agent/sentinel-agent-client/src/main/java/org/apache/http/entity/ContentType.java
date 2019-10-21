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

package org.apache.http.entity;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.TextUtils;

/**
 * Content type information consisting of a MIME type and an optional charset.
 * <p>
 * This class makes no attempts to verify validity of the MIME type.
 * The input parameters of the {@link #create(String, String)} method, however, may not
 * contain characters {@code <">, <;>, <,>} reserved by the HTTP specification.
 *
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class ContentType implements Serializable {

    private static final long serialVersionUID = -7768694718232371896L;

    // constants
    public static final ContentType APPLICATION_ATOM_XML = create(
            "application/atom+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_FORM_URLENCODED = create(
            "application/x-www-form-urlencoded", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create(
            "application/json", Consts.UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = create(
            "application/octet-stream", (Charset) null);
    public static final ContentType APPLICATION_SVG_XML = create(
            "application/svg+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_XHTML_XML = create(
            "application/xhtml+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_XML = create(
            "application/xml", Consts.ISO_8859_1);
    public static final ContentType MULTIPART_FORM_DATA = create(
            "multipart/form-data", Consts.ISO_8859_1);
    public static final ContentType TEXT_HTML = create(
            "text/html", Consts.ISO_8859_1);
    public static final ContentType TEXT_PLAIN = create(
            "text/plain", Consts.ISO_8859_1);
    public static final ContentType TEXT_XML = create(
            "text/xml", Consts.ISO_8859_1);
    public static final ContentType WILDCARD = create(
            "*/*", (Charset) null);


    private static final Map<String, ContentType> CONTENT_TYPE_MAP;
    static {

        final ContentType[] contentTypes = {
            APPLICATION_ATOM_XML,
            APPLICATION_FORM_URLENCODED,
            APPLICATION_JSON,
            APPLICATION_SVG_XML,
            APPLICATION_XHTML_XML,
            APPLICATION_XML,
            MULTIPART_FORM_DATA,
            TEXT_HTML,
            TEXT_PLAIN,
            TEXT_XML };
        final HashMap<String, ContentType> map = new HashMap<String, ContentType>();
        for (ContentType contentType: contentTypes) {
            map.put(contentType.getMimeType(), contentType);
        }
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    // defaults
    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    private final String mimeType;
    private final Charset charset;
    private final NameValuePair[] params;

    ContentType(
            final String mimeType,
            final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = null;
    }

    ContentType(
            final String mimeType,
            final Charset charset,
            final NameValuePair[] params) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = params;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    /**
     * @since 4.3
     */
    public String getParameter(final String name) {
        Args.notEmpty(name, "Parameter name");
        if (this.params == null) {
            return null;
        }
        for (final NameValuePair param: this.params) {
            if (param.getName().equalsIgnoreCase(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a {@code Content-Type} header.
     */
    @Override
    public String toString() {
        final CharArrayBuffer buf = new CharArrayBuffer(64);
        buf.append(this.mimeType);
        if (this.params != null) {
            buf.append("; ");
            BasicHeaderValueFormatter.INSTANCE.formatParameters(buf, this.params, false);
        } else if (this.charset != null) {
            buf.append("; charset=");
            buf.append(this.charset.name());
        }
        return buf.toString();
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';') {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new instance of {@link ContentType}.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param charset charset.
     * @return content type
     */
    public static ContentType create(final String mimeType, final Charset charset) {
        final String normalizedMimeType = Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
        Args.check(valid(normalizedMimeType), "MIME type may not contain reserved characters");
        return new ContentType(normalizedMimeType, charset);
    }

    /**
     * Creates a new instance of {@link ContentType} without a charset.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @return content type
     */
    public static ContentType create(final String mimeType) {
        return create(mimeType, (Charset) null);
    }

    /**
     * Creates a new instance of {@link ContentType}.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param charset charset. It may not contain characters {@code <">, <;>, <,>} reserved by the HTTP
     *        specification. This parameter is optional.
     * @return content type
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType create(
            final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !TextUtils.isBlank(charset) ? Charset.forName(charset) : null);
    }

    private static ContentType create(final HeaderElement helem, final boolean strict) {
        return create(helem.getName(), helem.getParameters(), strict);
    }

    private static ContentType create(final String mimeType, final NameValuePair[] params, final boolean strict) {
        Charset charset = null;
        for (final NameValuePair param: params) {
            if (param.getName().equalsIgnoreCase("charset")) {
                final String s = param.getValue();
                if (!TextUtils.isBlank(s)) {
                    try {
                        charset =  Charset.forName(s);
                    } catch (final UnsupportedCharsetException ex) {
                        if (strict) {
                            throw ex;
                        }
                    }
                }
                break;
            }
        }
        return new ContentType(mimeType, charset, params != null && params.length > 0 ? params : null);
    }

    /**
     * Creates a new instance of {@link ContentType} with the given parameters.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param params parameters.
     * @return content type
     *
     * @since 4.4
     */
    public static ContentType create(
            final String mimeType, final NameValuePair... params) throws UnsupportedCharsetException {
        final String type = Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
        Args.check(valid(type), "MIME type may not contain reserved characters");
        return create(mimeType, params, true);
    }

    /**
     * Parses textual representation of {@code Content-Type} value.
     *
     * @param s text
     * @return content type
     * @throws ParseException if the given text does not represent a valid
     * {@code Content-Type} value.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType parse(
            final String s) throws ParseException, UnsupportedCharsetException {
        Args.notNull(s, "Content type");
        final CharArrayBuffer buf = new CharArrayBuffer(s.length());
        buf.append(s);
        final ParserCursor cursor = new ParserCursor(0, s.length());
        final HeaderElement[] elements = BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor);
        if (elements.length > 0) {
            return create(elements[0], true);
        } else {
            throw new ParseException("Invalid content type: " + s);
        }
    }

    /**
     * Extracts {@code Content-Type} value from {@link HttpEntity} exactly as
     * specified by the {@code Content-Type} header of the entity. Returns {@code null}
     * if not specified.
     *
     * @param entity HTTP entity
     * @return content type
     * @throws ParseException if the given text does not represent a valid
     * {@code Content-Type} value.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType get(
            final HttpEntity entity) throws ParseException, UnsupportedCharsetException {
        if (entity == null) {
            return null;
        }
        final Header header = entity.getContentType();
        if (header != null) {
            final HeaderElement[] elements = header.getElements();
            if (elements.length > 0) {
                return create(elements[0], true);
            }
        }
        return null;
    }

    /**
     * Extracts {@code Content-Type} value from {@link HttpEntity}. Returns {@code null}
     * if not specified or incorrect (could not be parsed)..
     *
     * @param entity HTTP entity
     * @return content type
     *
     * @since 4.4
     *
     */
    public static ContentType getLenient(final HttpEntity entity) {
        if (entity == null) {
            return null;
        }
        final Header header = entity.getContentType();
        if (header != null) {
            try {
                final HeaderElement[] elements = header.getElements();
                if (elements.length > 0) {
                    return create(elements[0], false);
                }
            } catch (final ParseException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extracts {@code Content-Type} value from {@link HttpEntity} or returns the default value
     * {@link #DEFAULT_TEXT} if not explicitly specified.
     *
     * @param entity HTTP entity
     * @return content type
     * @throws ParseException if the given text does not represent a valid
     * {@code Content-Type} value.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType getOrDefault(
            final HttpEntity entity) throws ParseException, UnsupportedCharsetException {
        final ContentType contentType = get(entity);
        return contentType != null ? contentType : DEFAULT_TEXT;
    }

    /**
     * Extracts {@code Content-Type} value from {@link HttpEntity} or returns the default value
     * {@link #DEFAULT_TEXT} if not explicitly specified or incorrect (could not be parsed).
     *
     * @param entity HTTP entity
     * @return content type
     *
     * @since 4.4
     */
    public static ContentType getLenientOrDefault(
            final HttpEntity entity) throws ParseException, UnsupportedCharsetException {
        final ContentType contentType = get(entity);
        return contentType != null ? contentType : DEFAULT_TEXT;
    }


    /**
     * Returns {@code Content-Type} for the given MIME type.
     *
     * @param mimeType MIME type
     * @return content type or {@code null} if not known.
     *
     * @since 4.5
     */
    public static ContentType getByMimeType(final String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return CONTENT_TYPE_MAP.get(mimeType);
    }

    /**
     * Creates a new instance with this MIME type and the given Charset.
     *
     * @param charset charset
     * @return a new instance with this MIME type and the given Charset.
     * @since 4.3
     */
    public ContentType withCharset(final Charset charset) {
        return create(this.getMimeType(), charset);
    }

    /**
     * Creates a new instance with this MIME type and the given Charset name.
     *
     * @param charset name
     * @return a new instance with this MIME type and the given Charset name.
     * @throws UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     * @since 4.3
     */
    public ContentType withCharset(final String charset) {
        return create(this.getMimeType(), charset);
    }

    /**
     * Creates a new instance with this MIME type and the given parameters.
     *
     * @param params
     * @return a new instance with this MIME type and the given parameters.
     * @since 4.4
     */
    public ContentType withParameters(
            final NameValuePair... params) throws UnsupportedCharsetException {
        if (params.length == 0) {
            return this;
        }
        final Map<String, String> paramMap = new LinkedHashMap<String, String>();
        if (this.params != null) {
            for (final NameValuePair param: this.params) {
                paramMap.put(param.getName(), param.getValue());
            }
        }
        for (final NameValuePair param: params) {
            paramMap.put(param.getName(), param.getValue());
        }
        final List<NameValuePair> newParams = new ArrayList<NameValuePair>(paramMap.size() + 1);
        if (this.charset != null && !paramMap.containsKey("charset")) {
            newParams.add(new BasicNameValuePair("charset", this.charset.name()));
        }
        for (final Map.Entry<String, String> entry: paramMap.entrySet()) {
            newParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return create(this.getMimeType(), newParams.toArray(new NameValuePair[newParams.size()]), true);
    }

}
