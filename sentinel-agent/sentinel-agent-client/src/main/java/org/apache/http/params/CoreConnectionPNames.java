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
 * Defines parameter names for connections in HttpCore.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use configuration classes provided 'org.apache.http.config'
 *  and 'org.apache.http.client.config'
 */
@Deprecated
public interface CoreConnectionPNames {

    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * A timeout value of zero is interpreted as an infinite timeout.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     * @see java.net.SocketOptions#SO_TIMEOUT
     */
    public static final String SO_TIMEOUT = "http.socket.timeout";

    /**
     * Determines whether Nagle's algorithm is to be used. The Nagle's algorithm
     * tries to conserve bandwidth by minimizing the number of segments that are
     * sent. When applications wish to decrease network latency and increase
     * performance, they can disable Nagle's algorithm (that is enable
     * TCP_NODELAY). Data will be sent earlier, at the cost of an increase
     * in bandwidth consumption.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     * @see java.net.SocketOptions#TCP_NODELAY
     */
    public static final String TCP_NODELAY = "http.tcp.nodelay";

    /**
     * Determines the size of the internal socket buffer used to buffer data
     * while receiving / transmitting HTTP messages.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String SOCKET_BUFFER_SIZE = "http.socket.buffer-size";

    /**
     * Sets SO_LINGER with the specified linger time in seconds. The maximum
     * timeout value is platform specific. Value {@code 0} implies that
     * the option is disabled. Value {@code -1} implies that the JRE
     * default is used. The setting only affects the socket close operation.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     * @see java.net.SocketOptions#SO_LINGER
     */
    public static final String SO_LINGER = "http.socket.linger";

    /**
     * Defines whether the socket can be bound even though a previous connection is
     * still in a timeout state.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     * @see java.net.Socket#setReuseAddress(boolean)
     *
     * @since 4.1
     */
    public static final String SO_REUSEADDR = "http.socket.reuseaddr";

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     * <p>
     * Please note this parameter can only be applied to connections that
     * are bound to a particular local address.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String CONNECTION_TIMEOUT = "http.connection.timeout";

    /**
     * Determines whether stale connection check is to be used. The stale
     * connection check can cause up to 30 millisecond overhead per request and
     * should be used only when appropriate. For performance critical
     * operations this check should be disabled.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String STALE_CONNECTION_CHECK = "http.connection.stalecheck";

    /**
     * Determines the maximum line length limit. If set to a positive value,
     * any HTTP line exceeding this limit will cause an IOException. A negative
     * or zero value will effectively disable the check.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String MAX_LINE_LENGTH = "http.connection.max-line-length";

    /**
     * Determines the maximum HTTP header count allowed. If set to a positive
     * value, the number of HTTP headers received from the data stream exceeding
     * this limit will cause an IOException. A negative or zero value will
     * effectively disable the check.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String MAX_HEADER_COUNT = "http.connection.max-header-count";

    /**
     * Defines the size limit below which data chunks should be buffered in a session I/O buffer
     * in order to minimize native method invocations on the underlying network socket.
     * The optimal value of this parameter can be platform specific and defines a trade-off
     * between performance of memory copy operations and that of native method invocation.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     *
     * @since 4.1
     */
    public static final String MIN_CHUNK_LIMIT = "http.connection.min-chunk-limit";


    /**
     * Defines whether or not TCP is to send automatically a keepalive probe to the peer
     * after an interval of inactivity (no data exchanged in either direction) between this
     * host and the peer. The purpose of this option is to detect if the peer host crashes.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     * @see java.net.SocketOptions#SO_KEEPALIVE
     * @since 4.2
     */
    public static final String SO_KEEPALIVE = "http.socket.keepalive";

}
