package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;

import java.util.*;

public class ClusterClientConfig {
    public static final int REDIS_DISTRIBUTED_SINGLE = 1;
    public static final int REDIS_DISTRIBUTED_SENTINEL = 2;
    public static final int REDIS_DISTRIBUTED_CLUSTER = 3;
    public static final Set<Integer> validDistributedType
            = new HashSet<>(Arrays.asList(REDIS_DISTRIBUTED_SINGLE, REDIS_DISTRIBUTED_SENTINEL, REDIS_DISTRIBUTED_CLUSTER));
    public int distributedType;
    private String masterName;
    public List<HostAndPort> hostAndPorts;

    private Integer maxWaitMillis = ClientConstants.DEFAULT_MAX_WAIT_MILLIS;
    private Integer maxActive = ClientConstants.DEFAULT_MAX_ACTIVE;
    private Integer maxIdle = ClientConstants.DEFAULT_MAX_IDLE;
    private Integer minIdle = ClientConstants.DEFAULT_MIN_IDLE;
    private Integer connectTimeout = ClientConstants.DEFAULT_CONNECT_TIMEOUT;
    private Integer maxAttempts = ClientConstants.DEFAULT_MAX_ATTEMPTS;
    private String password;

    public static ClusterClientConfig ofSingle(HostAndPort hostAndPort) {
        ClusterClientConfig config = new ClusterClientConfig();
        List<HostAndPort> hostAndPorts = new ArrayList<>();
        hostAndPorts.add(hostAndPort);
        config.hostAndPorts = hostAndPorts;
        config.distributedType = REDIS_DISTRIBUTED_SINGLE;
        return config;
    }

    public static ClusterClientConfig ofSentinel(List<HostAndPort> hostAndPorts, String masterName) {
        ClusterClientConfig config = new ClusterClientConfig();
        config.hostAndPorts = hostAndPorts;
        config.masterName = masterName;
        config.distributedType = REDIS_DISTRIBUTED_SENTINEL;
        return  config;
    }

    public static ClusterClientConfig ofCluster(List<HostAndPort> hostAndPorts) {
        ClusterClientConfig config = new ClusterClientConfig();
        config.hostAndPorts = hostAndPorts;
        config.distributedType = REDIS_DISTRIBUTED_CLUSTER;
        return  config;
    }

    public Integer getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public ClusterClientConfig setMaxWaitMillis(Integer maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
        return this;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public ClusterClientConfig setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
        return this;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public ClusterClientConfig setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public ClusterClientConfig setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
        return this;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public ClusterClientConfig setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public static int getRedisDistributedSingle() {
        return REDIS_DISTRIBUTED_SINGLE;
    }

    public static int getRedisDistributedSentinel() {
        return REDIS_DISTRIBUTED_SENTINEL;
    }

    public static int getRedisDistributedCluster() {
        return REDIS_DISTRIBUTED_CLUSTER;
    }

    public int getDistributedType() {
        return distributedType;
    }

    public List<HostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }

    public String getMasterName() {
        return masterName;
    }

    public String getPassword() {
        return password;
    }

    public ClusterClientConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public ClusterClientConfig setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }
}
