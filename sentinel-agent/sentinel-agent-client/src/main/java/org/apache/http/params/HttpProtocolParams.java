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

package org.apache.http.params;

import java.nio.charset.CodingErrorAction;

import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

/**
 * Utility class for accessing protocol parameters in {@link HttpParams}.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public final class HttpProtocolParams implements CoreProtocolPNames {

    private HttpProtocolParams() {
        super();
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#HTTP_ELEMENT_CHARSET} parameter.
     * If not set, defaults to {@code US-ASCII}.
     *
     * @param params HTTP parameters.
     * @return HTTP element charset.
     */
    public static String getHttpElementCharset(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        String charset = (String) params.getParameter
            (CoreProtocolPNames.HTTP_ELEMENT_CHARSET);
        if (charset == null) {
            charset = HTTP.DEF_PROTOCOL_CHARSET.name();
        }
        return charset;
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#HTTP_ELEMENT_CHARSET} parameter.
     *
     * @param params HTTP parameters.
     * @param charset HTTP element charset.
     */
    public static void setHttpElementCharset(final HttpParams params, final String charset) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, charset);
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#HTTP_CONTENT_CHARSET} parameter.
     * If not set, defaults to {@code ISO-8859-1}.
     *
     * @param params HTTP parameters.
     * @return HTTP content charset.
     */
    public static String getContentCharset(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        String charset = (String) params.getParameter
            (CoreProtocolPNames.HTTP_CONTENT_CHARSET);
        if (charset == null) {
            charset = HTTP.DEF_CONTENT_CHARSET.name();
        }
        return charset;
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#HTTP_CONTENT_CHARSET} parameter.
     *
     * @param params HTTP parameters.
     * @param charset HTTP content charset.
     */
    public static void setContentCharset(final HttpParams params, final String charset) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset);
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#PROTOCOL_VERSION} parameter.
     * If not set, defaults to {@link HttpVersion#HTTP_1_1}.
     *
     * @param params HTTP parameters.
     * @return HTTP protocol version.
     */
    public static ProtocolVersion getVersion(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        final Object param = params.getParameter
            (CoreProtocolPNames.PROTOCOL_VERSION);
        if (param == null) {
            return HttpVersion.HTTP_1_1;
        }
        return (ProtocolVersion)param;
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#PROTOCOL_VERSION} parameter.
     *
     * @param params HTTP parameters.
     * @param version HTTP protocol version.
     */
    public static void setVersion(final HttpParams params, final ProtocolVersion version) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, version);
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#USER_AGENT} parameter.
     * If not set, returns {@code null}.
     *
     * @param params HTTP parameters.
     * @return User agent string.
     */
    public static String getUserAgent(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return (String) params.getParameter(CoreProtocolPNames.USER_AGENT);
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#USER_AGENT} parameter.
     *
     * @param params HTTP parameters.
     * @param useragent User agent string.
     */
    public static void setUserAgent(final HttpParams params, final String useragent) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.USER_AGENT, useragent);
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#USE_EXPECT_CONTINUE} parameter.
     * If not set, returns {@code false}.
     *
     * @param params HTTP parameters.
     * @return User agent string.
     */
    public static boolean useExpectContinue(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return params.getBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#USE_EXPECT_CONTINUE} parameter.
     *
     * @param params HTTP parameters.
     * @param b expect-continue flag.
     */
    public static void setUseExpectContinue(final HttpParams params, final boolean b) {
        Args.notNull(params, "HTTP parameters");
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, b);
    }

    /**
     * Obtains value of the {@link CoreProtocolPNames#HTTP_MALFORMED_INPUT_ACTION} parameter.
     * @param params HTTP parameters.
     * @return Action to perform upon receiving a malformed input
     *
     * @since 4.2
     */
    public static CodingErrorAction getMalformedInputAction(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        final Object param = params.getParameter(CoreProtocolPNames.HTTP_MALFORMED_INPUT_ACTION);
        if (param == null) {
            // the default CodingErrorAction
            return CodingErrorAction.REPORT;
        }
        return (CodingErrorAction) param;
    }

    /**
     * Sets value of the {@link CoreProtocolPNames#HTTP_MALFORMED_INPUT_ACTION} parameter.
     * @param params HTTP parameters
     * @param action action to perform on malformed inputs
     *
     * @since 4.2
     */
    public static void setMalformedInputAction(final HttpParams params, final CodingErrorAction action) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.HTTP_MALFORMED_INPUT_ACTION, action);
    }

    /**
     * Obtains the value of the  {@link CoreProtocolPNames#HTTP_UNMAPPABLE_INPUT_ACTION} parameter.
     * @param params HTTP parameters
     * @return Action to perform upon receiving a unmapped input
     *
     * @since 4.2
     */
    public static CodingErrorAction getUnmappableInputAction(final HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        final Object param = params.getParameter(CoreProtocolPNames.HTTP_UNMAPPABLE_INPUT_ACTION);
        if (param == null) {
            // the default CodingErrorAction
            return CodingErrorAction.REPORT;
        }
        return (CodingErrorAction) param;
    }

    /**
     * Sets the value of the {@link CoreProtocolPNames#HTTP_UNMAPPABLE_INPUT_ACTION} parameter.
     * @param params HTTP parameters
     * @param action action to perform on un mappable inputs
     *
     * @since 4.2
     */
    public static void setUnmappableInputAction(final HttpParams params, final CodingErrorAction action) {
        Args.notNull(params, "HTTP parameters");
        params.setParameter(CoreProtocolPNames.HTTP_UNMAPPABLE_INPUT_ACTION, action);
    }

}
