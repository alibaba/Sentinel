/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.heartbeat.client;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.csp.sentinel.config.SentinelConfig;

/**
 * Test cases for {@link SimpleHttpResponse#getBodyAsString()}, focused on the charset declared by
 * the {@code Content-Type} header (a media type with optional parameters, RFC 7231, 3.1.1.5).
 *
 * <p>The bodies below are encoded with a non-default charset, so that a decode performed with the
 * wrong charset yields replacement characters and fails the assertion.</p>
 */
public class SimpleHttpResponseTest {

    private static final String STATUS_LINE = "HTTP/1.1 200 OK";

    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /** Text with bytes that are valid ISO-8859-1 but not valid UTF-8. */
    private static final String ISO_8859_1_TEXT = "caf\u00e9 \u00e0 la carte";
    private static final byte[] ISO_8859_1_BODY = ISO_8859_1_TEXT.getBytes(ISO_8859_1);

    /** Chinese text requiring multi-byte UTF-8 sequences, to keep the UTF-8 cases honest. */
    private static final String UTF_8_TEXT = "\u4e2d\u6587\u6d4b\u8bd5";
    private static final byte[] UTF_8_BODY = UTF_8_TEXT.getBytes(UTF_8);

    private static Charset defaultCharset() {
        return Charset.forName(SentinelConfig.charset());
    }

    private static SimpleHttpResponse response(String headerName, String headerValue, byte[] body) {
        Map<String, String> headers = new HashMap<String, String>();
        if (headerName != null) {
            headers.put(headerName, headerValue);
        }
        return new SimpleHttpResponse(STATUS_LINE, headers, body);
    }

    private static SimpleHttpResponse response(String contentType, byte[] body) {
        return response("Content-Type", contentType, body);
    }

    @Test
    public void getBodyAsStringShouldUseDefaultCharsetWhenContentTypeHeaderIsAbsent() {
        assertEquals(new String(ISO_8859_1_BODY, defaultCharset()),
            response(null, ISO_8859_1_BODY).getBodyAsString());
        assertEquals(new String(UTF_8_BODY, defaultCharset()),
            response(null, UTF_8_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldUseDefaultCharsetWhenNoCharsetParameterIsGiven() {
        assertEquals(new String(ISO_8859_1_BODY, defaultCharset()),
            response("text/plain", ISO_8859_1_BODY).getBodyAsString());
        assertEquals(new String(UTF_8_BODY, defaultCharset()),
            response("text/html; boundary=xyz", UTF_8_BODY).getBodyAsString());
        assertEquals(new String(ISO_8859_1_BODY, defaultCharset()),
            response("text/plain; foo=bar; baz=qux", ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldHonorCharsetParameterWithoutWhitespaceAfterSemicolon() {
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain;charset=ISO-8859-1", ISO_8859_1_BODY).getBodyAsString());
        // Both the header name and the charset parameter name are case-insensitive.
        assertEquals(ISO_8859_1_TEXT,
            response("content-type", "text/plain;CHARSET=ISO-8859-1", ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldHonorQuotedCharsetParameter() {
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain; charset=\"ISO-8859-1\"", ISO_8859_1_BODY).getBodyAsString());
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain;charset=\"ISO-8859-1\"", ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldHonorCharsetParameterFollowedByOtherParameters() {
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain; charset=ISO-8859-1; boundary=xyz", ISO_8859_1_BODY).getBodyAsString());
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain;charset=ISO-8859-1;boundary=xyz", ISO_8859_1_BODY).getBodyAsString());
        // The charset parameter must be honored whatever its position among the parameters.
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain; boundary=xyz; charset=ISO-8859-1", ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldHonorExplicitUtf8Charset() {
        assertEquals(UTF_8_TEXT,
            response("application/json;charset=UTF-8", UTF_8_BODY).getBodyAsString());
        assertEquals(UTF_8_TEXT,
            response("application/json; charset=utf-8", UTF_8_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldNotPromoteACharsetInsideAQuotedPair() {
        // Here `boundary="a\";charset=US-ASCII"` is one quoted value whose quoted-pair `\"` is a
        // literal quote, so the `charset=US-ASCII` it contains is not a parameter: only the real
        // charset outside the quoted value must be honored.
        assertEquals(ISO_8859_1_TEXT,
            response("multipart/form-data; boundary=\"a\\\";charset=US-ASCII\"; charset=ISO-8859-1",
                ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldTolerateWhitespaceAroundTheEqualsSign() {
        // Tolerate whitespace around the parameter delimiter.
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain; charset = ISO-8859-1", ISO_8859_1_BODY).getBodyAsString());
        assertEquals(ISO_8859_1_TEXT,
            response("text/plain;charset= ISO-8859-1", ISO_8859_1_BODY).getBodyAsString());
    }

    @Test
    public void getBodyAsStringShouldResetToTheDefaultCharsetWhenHeadersAreMutated() {
        SimpleHttpResponse response = response("text/plain; charset=ISO-8859-1", ISO_8859_1_BODY);
        assertEquals(ISO_8859_1_TEXT, response.getBodyAsString());

        response.getHeaders().put("Content-Type", "text/plain");

        assertEquals(new String(ISO_8859_1_BODY, defaultCharset()), response.getBodyAsString());
    }

    @Test(expected = UnsupportedCharsetException.class)
    public void getBodyAsStringShouldThrowOnAnUnsupportedCharsetParameter() {
        response("text/plain; charset=no-such-charset", UTF_8_BODY).getBodyAsString();
    }
}
