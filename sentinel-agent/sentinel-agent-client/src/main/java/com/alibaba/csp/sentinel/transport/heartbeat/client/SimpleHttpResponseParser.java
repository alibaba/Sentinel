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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * The parser provides functionality to parse raw bytes HTTP response to a {@link SimpleHttpResponse}.
 * </p>
 * <p>
 * Note that this is a very NAIVE parser, {@code Content-Length} must be specified in the
 * HTTP response header, otherwise, the body will be dropped. All other body type such as
 * {@code Transfer-Encoding: chunked}, {@code Transfer-Encoding: deflate} are not supported.
 * </p>
 *
 * @author leyou
 */
public class SimpleHttpResponseParser {

    private static final int MAX_BODY_SIZE = 1024 * 1024 * 4;
    private byte[] buf;

    public SimpleHttpResponseParser(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must > 0");
        }
        this.buf = new byte[maxSize];
    }

    public SimpleHttpResponseParser() {
        this(1024 * 4);
    }

    /**
     * Parse bytes from an input stream to a {@link SimpleHttpResponse}.
     *
     * @param in input stream
     * @return parsed HTTP response entity
     * @throws IOException when an IO error occurs
     */
    public SimpleHttpResponse parse(InputStream in) throws IOException {
        int bg = 0;
        int len;
        String statusLine = null;
        Map<String, String> headers = new HashMap<String, String>();
        Charset charset = Charset.forName("utf-8");
        int contentLength = -1;
        SimpleHttpResponse response;
        while (true) {
            if (bg >= buf.length) {
                throw new IndexOutOfBoundsException("buf index out of range: " + bg + ", buf.length=" + buf.length);
            }
            if ((len = in.read(buf, bg, buf.length - bg)) > 0) {
                bg += len;
                len = bg;
                int idx;
                int parseBg = 0;
                while ((idx = indexOfCRLF(parseBg, len)) >= 0) {
                    String line = new String(buf, parseBg, idx - parseBg, charset);
                    parseBg = idx + 2;
                    if (statusLine == null) {
                        statusLine = line;
                    } else {
                        if (line.isEmpty()) {
                            //When the `Content-Length` is absent, parse the rest of the bytes as body directly.
                            //if (contentLength == -1) {
                            //    contentLength = MAX_BODY_SIZE;
                            //}

                            // Parse HTTP body.
                            // When the `Content-Length` is absent, drop the body, return directly.
                            response = new SimpleHttpResponse(statusLine, headers);
                            if (contentLength <= 0) {
                                return response;
                            }
                            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                            // `Content-Length` is not equal to exact length.
                            if (contentLength < len - parseBg) {
                                throw new IllegalStateException("Invalid content length: " + contentLength);
                            }
                            out.write(buf, parseBg, len - parseBg);
                            if (out.size() > MAX_BODY_SIZE) {
                                throw new IllegalStateException(
                                    "Request body is too big, limit size is " + MAX_BODY_SIZE);
                            }
                            int cap = Math.min(contentLength - out.size(), buf.length);
                            while (cap > 0 && (len = in.read(buf, 0, cap)) > 0) {
                                out.write(buf, 0, len);
                                cap = Math.min(contentLength - out.size(), buf.length);
                            }
                            response.setBody(out.toByteArray());
                            return response;
                        } else if (!line.trim().isEmpty()) {
                            // Parse HTTP header.
                            int idx2 = line.indexOf(":");
                            String key = line.substring(0, idx2).trim();
                            String value = line.substring(idx2 + 1).trim();
                            headers.put(key, value);
                            if ("Content-Length".equalsIgnoreCase(key)) {
                                contentLength = Integer.parseInt(value);
                            }
                        }
                    }
                }
                // Move remaining bytes to the beginning.
                if (parseBg != 0) {
                    System.arraycopy(buf, parseBg, buf, 0, len - parseBg);
                }
                bg = len - parseBg;
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * Get the index of CRLF separator.
     *
     * @param bg begin offset
     * @param ed end offset
     * @return the index, or {@code -1} if no CRLF is found
     */
    private int indexOfCRLF(int bg, int ed) {
        if (ed - bg < 2) {
            return -1;
        }
        for (int i = bg; i < ed - 1; i++) {
            if (buf[i] == '\r' && buf[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
}