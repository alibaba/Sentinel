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

import com.alibaba.csp.sentinel.config.SentinelConfig;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple HTTP response representation.
 *
 * @author leyou
 */
public class SimpleHttpResponse {

    private Charset charset = Charset.forName(SentinelConfig.charset());

    private String statusLine;
    private int statusCode;
    private Map<String, String> headers;
    private byte[] body;

    public SimpleHttpResponse(String statusLine, Map<String, String> headers) {
        this.statusLine = statusLine;
        this.headers = headers;
    }

    public SimpleHttpResponse(String statusLine, Map<String, String> headers, byte[] body) {
        this.statusLine = statusLine;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Resolve the charset from the {@code Content-Type} header, which is a media type with optional
     * parameters, e.g. {@code text/plain; charset="UTF-8"} (RFC 7231, section 3.1.1.5). When the
     * header is absent or carries no usable {@code charset} parameter, the default charset is used.
     * An unsupported charset value still fails with {@link java.nio.charset.UnsupportedCharsetException}.
     */
    private void parseCharset() {
        // Reset to the default on every call, as the headers may have been mutated in between.
        charset = Charset.forName(SentinelConfig.charset());
        String contentType = getHeader("Content-Type");
        if (contentType == null) {
            return;
        }
        for (String parameter : splitParameters(contentType)) {
            int idx = parameter.indexOf('=');
            // Skip the media type itself and any parameter that is not a charset.
            if (idx < 0 || !"charset".equalsIgnoreCase(parameter.substring(0, idx).trim())) {
                continue;
            }
            String value = unquote(parameter.substring(idx + 1).trim());
            if (!value.isEmpty()) {
                charset = Charset.forName(value);
            }
            return;
        }
    }

    /**
     * Split the parameters of a media type value on {@code ;}, ignoring separators inside a quoted
     * value, so that e.g. {@code text/plain; boundary="a;b"; charset=UTF-8} is split correctly.
     * A quoted-pair ({@code \} followed by any character) inside a quoted value is kept as-is and
     * never ends the quoted value, per RFC 7231, section 3.1.1.1.
     *
     * @param contentType the media type value
     * @return the media type and its parameters
     */
    private static List<String> splitParameters(String contentType) {
        List<String> parameters = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < contentType.length(); i++) {
            char c = contentType.charAt(i);
            if (quoted && c == '\\' && i + 1 < contentType.length()) {
                // Keep the escaped character verbatim: it may be a quote or a separator.
                current.append(c).append(contentType.charAt(++i));
                continue;
            }
            if (c == '"') {
                quoted = !quoted;
            }
            if (c == ';' && !quoted) {
                parameters.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parameters.add(current.toString());
        return parameters;
    }

    /**
     * Strip the surrounding balanced double quotes of a parameter value, if any.
     *
     * @param value the parameter value
     * @return the unquoted value
     */
    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    private void parseCode() {
        this.statusCode = Integer.parseInt(statusLine.split(" ")[1]);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public Integer getStatusCode() {
        if (statusCode == 0) {
            parseCode();
        }
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get header of the key ignoring case.
     *
     * @param key header key
     * @return header value
     */
    public String getHeader(String key) {
        if (headers == null) {
            return null;
        }
        String value = headers.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getBodyAsString() {
        parseCharset();
        return new String(body, charset);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(statusLine)
                .append("\r\n");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                buf.append(entry.getKey()).append(": ").append(entry.getValue())
                        .append("\r\n");
            }
        }
        buf.append("\r\n");
        buf.append(getBodyAsString());
        return buf.toString();
    }
}
