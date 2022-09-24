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
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;

/**
 * This class provide a builder to build redis client connection config.
 *
 * @author tiger
 */
public class RedisConnectionConfig {

    /**
     * The default redisSentinel port.
     */
    public static final int DEFAULT_SENTINEL_PORT = 26379;

    /**
     * The default redisCluster port.
     */
    public static final int DEFAULT_CLUSTER_PORT = 6379;

    /**
     * The default redis port.
     */
    public static final int DEFAULT_REDIS_PORT = 6379;

    /**
     * Default timeout: 60 sec
     */
    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000;

    private String host;
    private String redisSentinelMasterId;
    private int port;
    private int database;
    private String clientName;
    private char[] password;
    private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;
    private final List<RedisConnectionConfig> redisSentinels = new ArrayList<RedisConnectionConfig>();
    private final List<RedisConnectionConfig> redisClusters = new ArrayList<RedisConnectionConfig>();

    /**
     * Default empty constructor.
     */
    public RedisConnectionConfig() {
    }

    /**
     * Constructor with host/port and timeout.
     *
     * @param host    the host
     * @param port    the port
     * @param timeout timeout value . unit is mill seconds
     */
    public RedisConnectionConfig(String host, int port, long timeout) {

        AssertUtil.notEmpty(host, "Host must not be empty");
        AssertUtil.notNull(timeout, "Timeout duration must not be null");
        AssertUtil.isTrue(timeout >= 0, "Timeout duration must be greater or equal to zero");

        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    /**
     * Returns a new {@link RedisConnectionConfig.Builder} to construct a {@link RedisConnectionConfig}.
     *
     * @return a new {@link RedisConnectionConfig.Builder} to construct a {@link RedisConnectionConfig}.
     */
    public static RedisConnectionConfig.Builder builder() {
        return new RedisConnectionConfig.Builder();
    }

    /**
     * Returns the host.
     *
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the Redis host.
     *
     * @param host the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the Sentinel Master Id.
     *
     * @return the Sentinel Master Id.
     */
    public String getRedisSentinelMasterId() {
        return redisSentinelMasterId;
    }

    /**
     * Sets the Sentinel Master Id.
     *
     * @param redisSentinelMasterId the Sentinel Master Id.
     */
    public void setRedisSentinelMasterId(String redisSentinelMasterId) {
        this.redisSentinelMasterId = redisSentinelMasterId;
    }

    /**
     * Returns the Redis port.
     *
     * @return the Redis port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the Redis port. Defaults to {@link #DEFAULT_REDIS_PORT}.
     *
     * @param port the Redis port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * Sets the password. Use empty string to skip authentication.
     *
     * @param password the password, must not be {@literal null}.
     */
    public void setPassword(String password) {

        AssertUtil.notNull(password, "Password must not be null");
        this.password = password.toCharArray();
    }

    /**
     * Sets the password. Use empty char array to skip authentication.
     *
     * @param password the password, must not be {@literal null}.
     */
    public void setPassword(char[] password) {

        AssertUtil.notNull(password, "Password must not be null");
        this.password = Arrays.copyOf(password, password.length);
    }

    /**
     * Returns the command timeout for synchronous command execution.
     *
     * @return the Timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the command timeout for synchronous command execution.
     *
     * @param timeout the command timeout for synchronous command execution.
     */
    public void setTimeout(Long timeout) {

        AssertUtil.notNull(timeout, "Timeout must not be null");
        AssertUtil.isTrue(timeout >= 0, "Timeout must be greater or equal 0");

        this.timeout = timeout;
    }

    /**
     * Returns the Redis database number. Databases are only available for Redis Standalone and Redis Master/Slave.
     *
     * @return database
     */
    public int getDatabase() {
        return database;
    }

    /**
     * Sets the Redis database number. Databases are only available for Redis Standalone and Redis Master/Slave.
     *
     * @param database the Redis database number.
     */
    public void setDatabase(int database) {

        AssertUtil.isTrue(database >= 0, "Invalid database number: " + database);

        this.database = database;
    }

    /**
     * Returns the client name.
     *
     * @return
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Sets the client name to be applied on Redis connections.
     *
     * @param clientName the client name.
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return the list of {@link RedisConnectionConfig Redis Sentinel URIs}.
     */
    public List<RedisConnectionConfig> getRedisSentinels() {
        return redisSentinels;
    }

    /**
     * @return the list of {@link RedisConnectionConfig Redis Cluster URIs}.
     */
    public List<RedisConnectionConfig> getRedisClusters() {
        return redisClusters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());

        sb.append(" [");

        if (host != null) {
            sb.append("host='").append(host).append('\'');
            sb.append(", port=").append(port);
        }
        if (redisSentinelMasterId != null) {
            sb.append("redisSentinels=").append(getRedisSentinels());
            sb.append(", redisSentinelMasterId=").append(redisSentinelMasterId);
        }

        if (redisClusters.size() > 0) {
            sb.append("redisClusters=").append(getRedisClusters());
        }

        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisConnectionConfig)) {
            return false;
        }
        RedisConnectionConfig redisURI = (RedisConnectionConfig)o;

        if (port != redisURI.port) {
            return false;
        }
        if (database != redisURI.database) {
            return false;
        }
        if (host != null ? !host.equals(redisURI.host) : redisURI.host != null) {
            return false;
        }
        if (redisSentinelMasterId != null ? !redisSentinelMasterId.equals(redisURI.redisSentinelMasterId)
            : redisURI.redisSentinelMasterId != null) {
            return false;
        }
        if (redisClusters != null ? !redisClusters.equals(redisURI.redisClusters)
            : redisURI.redisClusters != null) {
            return false;
        }
        return !(redisSentinels != null ? !redisSentinels.equals(redisURI.redisSentinels)
            : redisURI.redisSentinels != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (redisSentinelMasterId != null ? redisSentinelMasterId.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + database;
        result = 31 * result + (redisSentinels != null ? redisSentinels.hashCode() : 0);
        result = 31 * result + (redisClusters != null ? redisClusters.hashCode() : 0);
        return result;
    }

    /**
     * Builder for Redis RedisConnectionConfig.
     */
    public static class Builder {

        private String host;
        private String redisSentinelMasterId;
        private int port;
        private int database;
        private String clientName;
        private char[] password;
        private long timeout = DEFAULT_TIMEOUT_MILLISECONDS;
        private final List<RedisHostAndPort> redisSentinels = new ArrayList<RedisHostAndPort>();
        private final List<RedisHostAndPort> redisClusters = new ArrayList<RedisHostAndPort>();

        private Builder() {
        }

        /**
         * Set Redis host. Creates a new builder.
         *
         * @param host the host name
         * @return New builder with Redis host/port.
         */
        public static RedisConnectionConfig.Builder redis(String host) {
            return redis(host, DEFAULT_REDIS_PORT);
        }

        /**
         * Set Redis host and port. Creates a new builder
         *
         * @param host the host name
         * @param port the port
         * @return New builder with Redis host/port.
         */
        public static RedisConnectionConfig.Builder redis(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            Builder builder = RedisConnectionConfig.builder();
            return builder.withHost(host).withPort(port);
        }

        /**
         * Set Sentinel host. Creates a new builder.
         *
         * @param host the host name
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host) {

            AssertUtil.notEmpty(host, "Host must not be empty");

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host);
        }

        /**
         * Set Sentinel host and port. Creates a new builder.
         *
         * @param host the host name
         * @param port the port
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisSentinel(host, port);
        }

        /**
         * Set Sentinel host and master id. Creates a new builder.
         *
         * @param host     the host name
         * @param masterId redisSentinel master id
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, String masterId) {
            return redisSentinel(host, DEFAULT_SENTINEL_PORT, masterId);
        }

        /**
         * Set Sentinel host, port and master id. Creates a new builder.
         *
         * @param host     the host name
         * @param port     the port
         * @param masterId redisSentinel master id
         * @return New builder with Sentinel host/port.
         */
        public static RedisConnectionConfig.Builder redisSentinel(String host, int port, String masterId) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withSentinelMasterId(masterId).withRedisSentinel(host, port);
        }

        /**
         * Add a withRedisSentinel host to the existing builder.
         *
         * @param host the host name
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisSentinel(String host) {
            return withRedisSentinel(host, DEFAULT_SENTINEL_PORT);
        }

        /**
         * Add a withRedisSentinel host/port to the existing builder.
         *
         * @param host the host name
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisSentinel(String host, int port) {

            AssertUtil.assertState(this.host == null, "Cannot use with Redis mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            redisSentinels.add(RedisHostAndPort.of(host, port));
            return this;
        }

        /**
         * Set Cluster host. Creates a new builder.
         *
         * @param host the host name
         * @return New builder with Cluster host/port.
         */
        public static RedisConnectionConfig.Builder redisCluster(String host) {

            AssertUtil.notEmpty(host, "Host must not be empty");

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisCluster(host);
        }

        /**
         * Set Cluster host and port. Creates a new builder.
         *
         * @param host the host name
         * @param port the port
         * @return New builder with Cluster host/port.
         */
        public static RedisConnectionConfig.Builder redisCluster(String host, int port) {

            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
            return builder.withRedisCluster(host, port);
        }

        /**
         * Add a withRedisCluster host to the existing builder.
         *
         * @param host the host name
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisCluster(String host) {
            return withRedisCluster(host, DEFAULT_CLUSTER_PORT);
        }

        /**
         * Add a withRedisCluster host/port to the existing builder.
         *
         * @param host the host name
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withRedisCluster(String host, int port) {

            AssertUtil.assertState(this.host == null, "Cannot use with Redis mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            redisClusters.add(RedisHostAndPort.of(host, port));
            return this;
        }

        /**
         * Adds host information to the builder. Does only affect Redis URI, cannot be used with Sentinel connections.
         *
         * @param host the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withHost(String host) {

            AssertUtil.assertState(this.redisSentinels.isEmpty(),
                "Sentinels are non-empty. Cannot use in Sentinel mode.");
            AssertUtil.notEmpty(host, "Host must not be empty");

            this.host = host;
            return this;
        }

        /**
         * Adds port information to the builder. Does only affect Redis URI, cannot be used with Sentinel connections.
         *
         * @param port the port
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPort(int port) {

            AssertUtil.assertState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            AssertUtil.isTrue(isValidPort(port), String.format("Port out of range: %s", port));

            this.port = port;
            return this;
        }

        /**
         * Configures the database number.
         *
         * @param database the database number
         * @return the builder
         */
        public RedisConnectionConfig.Builder withDatabase(int database) {

            AssertUtil.isTrue(database >= 0, "Invalid database number: " + database);

            this.database = database;
            return this;
        }

        /**
         * Configures a client name.
         *
         * @param clientName the client name
         * @return the builder
         */
        public RedisConnectionConfig.Builder withClientName(String clientName) {

            AssertUtil.notNull(clientName, "Client name must not be null");

            this.clientName = clientName;
            return this;
        }

        /**
         * Configures authentication.
         *
         * @param password the password
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPassword(String password) {

            AssertUtil.notNull(password, "Password must not be null");

            return withPassword(password.toCharArray());
        }

        /**
         * Configures authentication.
         *
         * @param password the password
         * @return the builder
         */
        public RedisConnectionConfig.Builder withPassword(char[] password) {

            AssertUtil.notNull(password, "Password must not be null");

            this.password = Arrays.copyOf(password, password.length);
            return this;
        }

        /**
         * Configures a timeout.
         *
         * @param timeout must not be {@literal null} or negative.
         * @return the builder
         */
        public RedisConnectionConfig.Builder withTimeout(long timeout) {

            AssertUtil.notNull(timeout, "Timeout must not be null");
            AssertUtil.notNull(timeout >= 0, "Timeout must be greater or equal 0");

            this.timeout = timeout;
            return this;
        }

        /**
         * Configures a redisSentinel master Id.
         *
         * @param sentinelMasterId redisSentinel master id, must not be empty or {@literal null}
         * @return the builder
         */
        public RedisConnectionConfig.Builder withSentinelMasterId(String sentinelMasterId) {

            AssertUtil.notEmpty(sentinelMasterId, "Sentinel master id must not empty");

            this.redisSentinelMasterId = sentinelMasterId;
            return this;
        }

        /**
         * @return the RedisConnectionConfig.
         */
        public RedisConnectionConfig build() {

            if (redisSentinels.isEmpty() && redisClusters.isEmpty() && StringUtil.isEmpty(host)) {
                throw new IllegalStateException(
                    "Cannot build a RedisConnectionConfig. One of the following must be provided Host, Socket, Cluster or "
                        + "Sentinel");
            }

            RedisConnectionConfig redisURI = new RedisConnectionConfig();
            redisURI.setHost(host);
            redisURI.setPort(port);

            if (password != null) {
                redisURI.setPassword(password);
            }

            redisURI.setDatabase(database);
            redisURI.setClientName(clientName);

            redisURI.setRedisSentinelMasterId(redisSentinelMasterId);

            for (RedisHostAndPort sentinel : redisSentinels) {
                redisURI.getRedisSentinels().add(
                    new RedisConnectionConfig(sentinel.getHost(), sentinel.getPort(), timeout));
            }

            for (RedisHostAndPort sentinel : redisClusters) {
                redisURI.getRedisClusters().add(
                    new RedisConnectionConfig(sentinel.getHost(), sentinel.getPort(), timeout));
            }

            redisURI.setTimeout(timeout);

            return redisURI;
        }
    }

    /**
     * Return true for valid port numbers.
     */
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
