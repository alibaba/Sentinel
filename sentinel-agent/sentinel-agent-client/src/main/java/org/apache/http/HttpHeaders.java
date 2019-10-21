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

package org.apache.http;

/**
 * Constants enumerating the HTTP headers. All headers defined in RFC1945 (HTTP/1.0), RFC2616 (HTTP/1.1), and RFC2518
 * (WebDAV) are listed.
 *
 * @since 4.1
 */
public final class HttpHeaders {

    private HttpHeaders() {
    }

    /** RFC 2616 (HTTP/1.1) Section 14.1 */
    public static final String ACCEPT = "Accept";

    /** RFC 2616 (HTTP/1.1) Section 14.2 */
    public static final String ACCEPT_CHARSET = "Accept-Charset";

    /** RFC 2616 (HTTP/1.1) Section 14.3 */
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.4 */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    /** RFC 2616 (HTTP/1.1) Section 14.5 */
    public static final String ACCEPT_RANGES = "Accept-Ranges";

    /** RFC 2616 (HTTP/1.1) Section 14.6 */
    public static final String AGE = "Age";

    /** RFC 1945 (HTTP/1.0) Section 10.1, RFC 2616 (HTTP/1.1) Section 14.7 */
    public static final String ALLOW = "Allow";

    /** RFC 1945 (HTTP/1.0) Section 10.2, RFC 2616 (HTTP/1.1) Section 14.8 */
    public static final String AUTHORIZATION = "Authorization";

    /** RFC 2616 (HTTP/1.1) Section 14.9 */
    public static final String CACHE_CONTROL = "Cache-Control";

    /** RFC 2616 (HTTP/1.1) Section 14.10 */
    public static final String CONNECTION = "Connection";

    /** RFC 1945 (HTTP/1.0) Section 10.3, RFC 2616 (HTTP/1.1) Section 14.11 */
    public static final String CONTENT_ENCODING = "Content-Encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.12 */
    public static final String CONTENT_LANGUAGE = "Content-Language";

    /** RFC 1945 (HTTP/1.0) Section 10.4, RFC 2616 (HTTP/1.1) Section 14.13 */
    public static final String CONTENT_LENGTH = "Content-Length";

    /** RFC 2616 (HTTP/1.1) Section 14.14 */
    public static final String CONTENT_LOCATION = "Content-Location";

    /** RFC 2616 (HTTP/1.1) Section 14.15 */
    public static final String CONTENT_MD5 = "Content-MD5";

    /** RFC 2616 (HTTP/1.1) Section 14.16 */
    public static final String CONTENT_RANGE = "Content-Range";

    /** RFC 1945 (HTTP/1.0) Section 10.5, RFC 2616 (HTTP/1.1) Section 14.17 */
    public static final String CONTENT_TYPE = "Content-Type";

    /** RFC 1945 (HTTP/1.0) Section 10.6, RFC 2616 (HTTP/1.1) Section 14.18 */
    public static final String DATE = "Date";

    /** RFC 2518 (WevDAV) Section 9.1 */
    public static final String DAV = "Dav";

    /** RFC 2518 (WevDAV) Section 9.2 */
    public static final String DEPTH = "Depth";

    /** RFC 2518 (WevDAV) Section 9.3 */
    public static final String DESTINATION = "Destination";

    /** RFC 2616 (HTTP/1.1) Section 14.19 */
    public static final String ETAG = "ETag";

    /** RFC 2616 (HTTP/1.1) Section 14.20 */
    public static final String EXPECT = "Expect";

    /** RFC 1945 (HTTP/1.0) Section 10.7, RFC 2616 (HTTP/1.1) Section 14.21 */
    public static final String EXPIRES = "Expires";

    /** RFC 1945 (HTTP/1.0) Section 10.8, RFC 2616 (HTTP/1.1) Section 14.22 */
    public static final String FROM = "From";

    /** RFC 2616 (HTTP/1.1) Section 14.23 */
    public static final String HOST = "Host";

    /** RFC 2518 (WevDAV) Section 9.4 */
    public static final String IF = "If";

    /** RFC 2616 (HTTP/1.1) Section 14.24 */
    public static final String IF_MATCH = "If-Match";

    /** RFC 1945 (HTTP/1.0) Section 10.9, RFC 2616 (HTTP/1.1) Section 14.25 */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /** RFC 2616 (HTTP/1.1) Section 14.26 */
    public static final String IF_NONE_MATCH = "If-None-Match";

    /** RFC 2616 (HTTP/1.1) Section 14.27 */
    public static final String IF_RANGE = "If-Range";

    /** RFC 2616 (HTTP/1.1) Section 14.28 */
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /** RFC 1945 (HTTP/1.0) Section 10.10, RFC 2616 (HTTP/1.1) Section 14.29 */
    public static final String LAST_MODIFIED = "Last-Modified";

    /** RFC 1945 (HTTP/1.0) Section 10.11, RFC 2616 (HTTP/1.1) Section 14.30 */
    public static final String LOCATION = "Location";

    /** RFC 2518 (WevDAV) Section 9.5 */
    public static final String LOCK_TOKEN = "Lock-Token";

    /** RFC 2616 (HTTP/1.1) Section 14.31 */
    public static final String MAX_FORWARDS = "Max-Forwards";

    /** RFC 2518 (WevDAV) Section 9.6 */
    public static final String OVERWRITE = "Overwrite";

    /** RFC 1945 (HTTP/1.0) Section 10.12, RFC 2616 (HTTP/1.1) Section 14.32 */
    public static final String PRAGMA = "Pragma";

    /** RFC 2616 (HTTP/1.1) Section 14.33 */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /** RFC 2616 (HTTP/1.1) Section 14.34 */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /** RFC 2616 (HTTP/1.1) Section 14.35 */
    public static final String RANGE = "Range";

    /** RFC 1945 (HTTP/1.0) Section 10.13, RFC 2616 (HTTP/1.1) Section 14.36 */
    public static final String REFERER = "Referer";

    /** RFC 2616 (HTTP/1.1) Section 14.37 */
    public static final String RETRY_AFTER = "Retry-After";

    /** RFC 1945 (HTTP/1.0) Section 10.14, RFC 2616 (HTTP/1.1) Section 14.38 */
    public static final String SERVER = "Server";

    /** RFC 2518 (WevDAV) Section 9.7 */
    public static final String STATUS_URI = "Status-URI";

    /** RFC 2616 (HTTP/1.1) Section 14.39 */
    public static final String TE = "TE";

    /** RFC 2518 (WevDAV) Section 9.8 */
    public static final String TIMEOUT = "Timeout";

    /** RFC 2616 (HTTP/1.1) Section 14.40 */
    public static final String TRAILER = "Trailer";

    /** RFC 2616 (HTTP/1.1) Section 14.41 */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /** RFC 2616 (HTTP/1.1) Section 14.42 */
    public static final String UPGRADE = "Upgrade";

    /** RFC 1945 (HTTP/1.0) Section 10.15, RFC 2616 (HTTP/1.1) Section 14.43 */
    public static final String USER_AGENT = "User-Agent";

    /** RFC 2616 (HTTP/1.1) Section 14.44 */
    public static final String VARY = "Vary";

    /** RFC 2616 (HTTP/1.1) Section 14.45 */
    public static final String VIA = "Via";

    /** RFC 2616 (HTTP/1.1) Section 14.46 */
    public static final String WARNING = "Warning";

    /** RFC 1945 (HTTP/1.0) Section 10.16, RFC 2616 (HTTP/1.1) Section 14.47 */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

}
