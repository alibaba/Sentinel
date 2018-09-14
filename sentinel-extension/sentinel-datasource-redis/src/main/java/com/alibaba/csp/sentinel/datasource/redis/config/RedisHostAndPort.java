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

package com.alibaba.csp.sentinel.datasource.redis.config;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * An immutable representation of a host and port.
 *
 * @author tiger
 */
public class RedisHostAndPort {

    private static final int NO_PORT = -1;

    public final String host;
    public final int port;

    /**
     * @param host must not be empty or {@literal null}.
     * @param port
     */
    private RedisHostAndPort(String host, int port) {
        AssertUtil.notNull(host, "host must not be null");

        this.host = host;
        this.port = port;
    }

    /**
     * Create a {@link RedisHostAndPort} of {@code host} and {@code port}
     *
     * @param host the hostname
     * @param port a valid port
     * @return the {@link RedisHostAndPort} of {@code host} and {@code port}
     */
    public static RedisHostAndPort of(String host, int port) {
        AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));
        return new RedisHostAndPort(host, port);
    }

    /**
     * @return {@literal true} if has a port.
     */
    public boolean hasPort() {
        return port != NO_PORT;
    }

    /**
     * @return the host text.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port.
     */
    public int getPort() {
        if (!hasPort()) {
            throw new IllegalStateException("No port present.");
        }
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisHostAndPort)) {
            return false;
        }
        RedisHostAndPort that = (RedisHostAndPort)o;
        return port == that.port && (host != null ? host.equals(that.host) : that.host == null);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    /**
     * @param port the port number
     * @return {@literal true} for valid port numbers.
     */
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (hasPort()) {
            sb.append(':').append(port);
        }
        return sb.toString();
    }
}
