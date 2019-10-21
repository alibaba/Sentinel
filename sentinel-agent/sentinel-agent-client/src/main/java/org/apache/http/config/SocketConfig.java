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

package org.apache.http.config;

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.util.Args;

/**
 * Socket configuration.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class SocketConfig implements Cloneable {

    public static final SocketConfig DEFAULT = new Builder().build();

    private final int soTimeout;
    private final boolean soReuseAddress;
    private final int soLinger;
    private final boolean soKeepAlive;
    private final boolean tcpNoDelay;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int backlogSize;

    SocketConfig(
            final int soTimeout,
            final boolean soReuseAddress,
            final int soLinger,
            final boolean soKeepAlive,
            final boolean tcpNoDelay,
            final int sndBufSize,
            final int rcvBufSize,
            final int backlogSize) {
        super();
        this.soTimeout = soTimeout;
        this.soReuseAddress = soReuseAddress;
        this.soLinger = soLinger;
        this.soKeepAlive = soKeepAlive;
        this.tcpNoDelay = tcpNoDelay;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.backlogSize = backlogSize;
    }

    /**
     * Determines the default socket timeout value for non-blocking I/O operations.
     * <p>
     * Default: {@code 0} (no timeout)
     * </p>
     *
     * @return the default socket timeout value for non-blocking I/O operations.
     * @see java.net.SocketOptions#SO_TIMEOUT
     */
    public int getSoTimeout() {
        return soTimeout;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#SO_REUSEADDR} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code false}
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#SO_REUSEADDR} parameter.
     * @see java.net.SocketOptions#SO_REUSEADDR
     */
    public boolean isSoReuseAddress() {
        return soReuseAddress;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#SO_LINGER} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code -1}
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#SO_LINGER} parameter.
     * @see java.net.SocketOptions#SO_LINGER
     */
    public int getSoLinger() {
        return soLinger;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#SO_KEEPALIVE} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code -1}
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#SO_KEEPALIVE} parameter.
     * @see java.net.SocketOptions#SO_KEEPALIVE
     */
    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#TCP_NODELAY} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code false}
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#TCP_NODELAY} parameter.
     * @see java.net.SocketOptions#TCP_NODELAY
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#SO_SNDBUF} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code 0} (system default)
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#SO_SNDBUF} parameter.
     * @see java.net.SocketOptions#SO_SNDBUF
     * @since 4.4
     */
    public int getSndBufSize() {
        return sndBufSize;
    }

    /**
     * Determines the default value of the {@link java.net.SocketOptions#SO_RCVBUF} parameter
     * for newly created sockets.
     * <p>
     * Default: {@code 0} (system default)
     * </p>
     *
     * @return the default value of the {@link java.net.SocketOptions#SO_RCVBUF} parameter.
     * @see java.net.SocketOptions#SO_RCVBUF
     * @since 4.4
     */
    public int getRcvBufSize() {
        return rcvBufSize;
    }

    /**
     * Determines the maximum queue length for incoming connection indications
     * (a request to connect) also known as server socket backlog.
     * <p>
     * Default: {@code 0} (system default)
     * </p>
     * @return the maximum queue length for incoming connection indications
     * @since 4.4
     */
    public int getBacklogSize() {
        return backlogSize;
    }

    @Override
    protected SocketConfig clone() throws CloneNotSupportedException {
        return (SocketConfig) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[soTimeout=").append(this.soTimeout)
                .append(", soReuseAddress=").append(this.soReuseAddress)
                .append(", soLinger=").append(this.soLinger)
                .append(", soKeepAlive=").append(this.soKeepAlive)
                .append(", tcpNoDelay=").append(this.tcpNoDelay)
                .append(", sndBufSize=").append(this.sndBufSize)
                .append(", rcvBufSize=").append(this.rcvBufSize)
                .append(", backlogSize=").append(this.backlogSize)
                .append("]");
        return builder.toString();
    }

    public static SocketConfig.Builder custom() {
        return new Builder();
    }

    public static SocketConfig.Builder copy(final SocketConfig config) {
        Args.notNull(config, "Socket config");
        return new Builder()
            .setSoTimeout(config.getSoTimeout())
            .setSoReuseAddress(config.isSoReuseAddress())
            .setSoLinger(config.getSoLinger())
            .setSoKeepAlive(config.isSoKeepAlive())
            .setTcpNoDelay(config.isTcpNoDelay())
            .setSndBufSize(config.getSndBufSize())
            .setRcvBufSize(config.getRcvBufSize())
            .setBacklogSize(config.getBacklogSize());
    }

    public static class Builder {

        private int soTimeout;
        private boolean soReuseAddress;
        private int soLinger;
        private boolean soKeepAlive;
        private boolean tcpNoDelay;
        private int sndBufSize;
        private int rcvBufSize;
        private int backlogSize;

        Builder() {
            this.soLinger = -1;
            this.tcpNoDelay = true;
        }

        public Builder setSoTimeout(final int soTimeout) {
            this.soTimeout = soTimeout;
            return this;
        }

        public Builder setSoReuseAddress(final boolean soReuseAddress) {
            this.soReuseAddress = soReuseAddress;
            return this;
        }

        public Builder setSoLinger(final int soLinger) {
            this.soLinger = soLinger;
            return this;
        }

        public Builder setSoKeepAlive(final boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder setTcpNoDelay(final boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        /**
         * @since 4.4
         */
        public Builder setSndBufSize(final int sndBufSize) {
            this.sndBufSize = sndBufSize;
            return this;
        }

        /**
         * @since 4.4
         */
        public Builder setRcvBufSize(final int rcvBufSize) {
            this.rcvBufSize = rcvBufSize;
            return this;
        }

        /**
         * @since 4.4
         */
        public Builder setBacklogSize(final int backlogSize) {
            this.backlogSize = backlogSize;
            return this;
        }

        public SocketConfig build() {
            return new SocketConfig(soTimeout, soReuseAddress, soLinger, soKeepAlive, tcpNoDelay,
                    sndBufSize, rcvBufSize, backlogSize);
        }

    }

}
