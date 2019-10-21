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

/**
 * Defines parameter names for protocol execution in HttpCore.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public interface CoreProtocolPNames {

    /**
     * Defines the {@link org.apache.http.ProtocolVersion} used per default.
     * <p>
     * This parameter expects a value of type {@link org.apache.http.ProtocolVersion}.
     * </p>
     */
    public static final String PROTOCOL_VERSION = "http.protocol.version";

    /**
     * Defines the charset to be used for encoding HTTP protocol elements.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    public static final String HTTP_ELEMENT_CHARSET = "http.protocol.element-charset";

    /**
     * Defines the charset to be used per default for encoding content body.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    public static final String HTTP_CONTENT_CHARSET = "http.protocol.content-charset";

    /**
     * Defines the content of the {@code User-Agent} header.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    public static final String USER_AGENT = "http.useragent";

    /**
     * Defines the content of the {@code Server} header.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    public static final String ORIGIN_SERVER = "http.origin-server";

    /**
     * Defines whether responses with an invalid {@code Transfer-Encoding}
     * header should be rejected.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String STRICT_TRANSFER_ENCODING = "http.protocol.strict-transfer-encoding";

    /**
     * <p>
     * Activates 'Expect: 100-Continue' handshake for the
     * entity enclosing methods. The purpose of the 'Expect: 100-Continue'
     * handshake is to allow a client that is sending a request message with
     * a request body to determine if the origin server is willing to
     * accept the request (based on the request headers) before the client
     * sends the request body.
     * </p>
     *
     * <p>
     * The use of the 'Expect: 100-continue' handshake can result in
     * a noticeable performance improvement for entity enclosing requests
     * (such as POST and PUT) that require the target server's
     * authentication.
     * </p>
     *
     * <p>
     * 'Expect: 100-continue' handshake should be used with
     * caution, as it may cause problems with HTTP servers and
     * proxies that do not support HTTP/1.1 protocol.
     * </p>
     *
     * This parameter expects a value of type {@link Boolean}.
     */
    public static final String USE_EXPECT_CONTINUE = "http.protocol.expect-continue";

    /**
     * <p>
     * Defines the maximum period of time in milliseconds the client should spend
     * waiting for a 100-continue response.
     * </p>
     *
     * This parameter expects a value of type {@link Integer}.
     */
    public static final String WAIT_FOR_CONTINUE = "http.protocol.wait-for-continue";

    /**
     * <p>
     * Defines the action to perform upon receiving a malformed input. If the input byte sequence
     * is not legal for this charset then the input is said to be malformed
     * </p>
     *
     * This parameter expects a value of type {@link java.nio.charset.CodingErrorAction}
     *
     * @since 4.2
     */
    public static final String HTTP_MALFORMED_INPUT_ACTION = "http.malformed.input.action";

    /**
     * <p>
     * Defines the action to perform upon receiving an unmappable input. If the input byte sequence
     * is legal but cannot be mapped to a valid Unicode character then the input is said to be
     * unmappable
     * </p>
     *
     * This parameter expects a value of type {@link java.nio.charset.CodingErrorAction}
     *
     * @since 4.2
     */
    public static final String HTTP_UNMAPPABLE_INPUT_ACTION = "http.unmappable.input.action";

}
