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

package org.apache.http.impl;

import java.util.Locale;

import org.apache.http.HttpStatus;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.util.Args;

/**
 * English reason phrases for HTTP status codes.
 * All status codes defined in RFC1945 (HTTP/1.0), RFC2616 (HTTP/1.1), and
 * RFC2518 (WebDAV) are supported.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class EnglishReasonPhraseCatalog implements ReasonPhraseCatalog {

    // static array with english reason phrases defined below

    /**
     * The default instance of this catalog.
     * This catalog is thread safe, so there typically
     * is no need to create other instances.
     */
    public final static EnglishReasonPhraseCatalog INSTANCE = new EnglishReasonPhraseCatalog();


    /**
     * Restricted default constructor, for derived classes.
     * If you need an instance of this class, use {@link #INSTANCE INSTANCE}.
     */
    protected EnglishReasonPhraseCatalog() {
        // no body
    }


    /**
     * Obtains the reason phrase for a status code.
     *
     * @param status    the status code, in the range 100-599
     * @param loc       ignored
     *
     * @return  the reason phrase, or {@code null}
     */
    @Override
    public String getReason(final int status, final Locale loc) {
        Args.check(status >= 100 && status < 600, "Unknown category for status code " + status);
        final int category = status / 100;
        final int subcode  = status - 100*category;

        String reason = null;
        if (REASON_PHRASES[category].length > subcode) {
            reason = REASON_PHRASES[category][subcode];
        }

        return reason;
    }


    /** Reason phrases lookup table. */
    private static final String[][] REASON_PHRASES = new String[][]{
        null,
        new String[3],  // 1xx
        new String[8],  // 2xx
        new String[8],  // 3xx
        new String[25], // 4xx
        new String[8]   // 5xx
    };



    /**
     * Stores the given reason phrase, by status code.
     * Helper method to initialize the static lookup table.
     *
     * @param status    the status code for which to define the phrase
     * @param reason    the reason phrase for this status code
     */
    private static void setReason(final int status, final String reason) {
        final int category = status / 100;
        final int subcode  = status - 100*category;
        REASON_PHRASES[category][subcode] = reason;
    }


    // ----------------------------------------------------- Static Initializer

    /** Set up status code to "reason phrase" map. */
    static {
        // HTTP 1.0 Server status codes -- see RFC 1945
        setReason(HttpStatus.SC_OK,
                  "OK");
        setReason(HttpStatus.SC_CREATED,
                  "Created");
        setReason(HttpStatus.SC_ACCEPTED,
                  "Accepted");
        setReason(HttpStatus.SC_NO_CONTENT,
                  "No Content");
        setReason(HttpStatus.SC_MOVED_PERMANENTLY,
                  "Moved Permanently");
        setReason(HttpStatus.SC_MOVED_TEMPORARILY,
                  "Moved Temporarily");
        setReason(HttpStatus.SC_NOT_MODIFIED,
                  "Not Modified");
        setReason(HttpStatus.SC_BAD_REQUEST,
                  "Bad Request");
        setReason(HttpStatus.SC_UNAUTHORIZED,
                  "Unauthorized");
        setReason(HttpStatus.SC_FORBIDDEN,
                  "Forbidden");
        setReason(HttpStatus.SC_NOT_FOUND,
                  "Not Found");
        setReason(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                  "Internal Server Error");
        setReason(HttpStatus.SC_NOT_IMPLEMENTED,
                  "Not Implemented");
        setReason(HttpStatus.SC_BAD_GATEWAY,
                  "Bad Gateway");
        setReason(HttpStatus.SC_SERVICE_UNAVAILABLE,
                  "Service Unavailable");

        // HTTP 1.1 Server status codes -- see RFC 2048
        setReason(HttpStatus.SC_CONTINUE,
                  "Continue");
        setReason(HttpStatus.SC_TEMPORARY_REDIRECT,
                  "Temporary Redirect");
        setReason(HttpStatus.SC_METHOD_NOT_ALLOWED,
                  "Method Not Allowed");
        setReason(HttpStatus.SC_CONFLICT,
                  "Conflict");
        setReason(HttpStatus.SC_PRECONDITION_FAILED,
                  "Precondition Failed");
        setReason(HttpStatus.SC_REQUEST_TOO_LONG,
                  "Request Too Long");
        setReason(HttpStatus.SC_REQUEST_URI_TOO_LONG,
                  "Request-URI Too Long");
        setReason(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE,
                  "Unsupported Media Type");
        setReason(HttpStatus.SC_MULTIPLE_CHOICES,
                  "Multiple Choices");
        setReason(HttpStatus.SC_SEE_OTHER,
                  "See Other");
        setReason(HttpStatus.SC_USE_PROXY,
                  "Use Proxy");
        setReason(HttpStatus.SC_PAYMENT_REQUIRED,
                  "Payment Required");
        setReason(HttpStatus.SC_NOT_ACCEPTABLE,
                  "Not Acceptable");
        setReason(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED,
                  "Proxy Authentication Required");
        setReason(HttpStatus.SC_REQUEST_TIMEOUT,
                  "Request Timeout");

        setReason(HttpStatus.SC_SWITCHING_PROTOCOLS,
                  "Switching Protocols");
        setReason(HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION,
                  "Non Authoritative Information");
        setReason(HttpStatus.SC_RESET_CONTENT,
                  "Reset Content");
        setReason(HttpStatus.SC_PARTIAL_CONTENT,
                  "Partial Content");
        setReason(HttpStatus.SC_GATEWAY_TIMEOUT,
                  "Gateway Timeout");
        setReason(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED,
                  "Http Version Not Supported");
        setReason(HttpStatus.SC_GONE,
                  "Gone");
        setReason(HttpStatus.SC_LENGTH_REQUIRED,
                  "Length Required");
        setReason(HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE,
                  "Requested Range Not Satisfiable");
        setReason(HttpStatus.SC_EXPECTATION_FAILED,
                  "Expectation Failed");

        // WebDAV Server-specific status codes
        setReason(HttpStatus.SC_PROCESSING,
                  "Processing");
        setReason(HttpStatus.SC_MULTI_STATUS,
                  "Multi-Status");
        setReason(HttpStatus.SC_UNPROCESSABLE_ENTITY,
                  "Unprocessable Entity");
        setReason(HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                  "Insufficient Space On Resource");
        setReason(HttpStatus.SC_METHOD_FAILURE,
                  "Method Failure");
        setReason(HttpStatus.SC_LOCKED,
                  "Locked");
        setReason(HttpStatus.SC_INSUFFICIENT_STORAGE,
                  "Insufficient Storage");
        setReason(HttpStatus.SC_FAILED_DEPENDENCY,
                  "Failed Dependency");
    }


}
