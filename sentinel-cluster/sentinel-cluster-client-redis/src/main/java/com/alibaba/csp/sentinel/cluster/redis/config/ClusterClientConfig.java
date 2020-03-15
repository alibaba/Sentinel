package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.util.ClientConstants;

import java.util.*;

public class ClusterClientConfig {
    public static final int REDIS_SINGLE = 1;
    public static final int REDIS_SENTINEL = 2;
    public static final int REDIS_CLUSTER = 3;
    public static final Set<Integer> VALID_CLUSTER_TYPE
            = new HashSet<>(Arrays.asList(REDIS_SINGLE, REDIS_SENTINEL, REDIS_CLUSTER));
    private int clusterType;
    private String masterName;
    private List<HostAndPort> hostAndPorts;
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
        config.clusterType = REDIS_SINGLE;
        return config;
    }

    public static ClusterClientConfig ofSentinel(List<HostAndPort> hostAndPorts, String masterName) {
        ClusterClientConfig config = new ClusterClientConfig();
        config.hostAndPorts = hostAndPorts;
        config.masterName = masterName;
        config.clusterType = REDIS_SENTINEL;
        return  config;
    }

    public static ClusterClientConfig ofCluster(List<HostAndPort> hostAndPorts) {
        ClusterClientConfig config = new ClusterClientConfig();
        config.hostAndPorts = hostAndPorts;
        config.clusterType = REDIS_CLUSTER;
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

    public static int getRedisSingle() {
        return REDIS_SINGLE;
    }

    public static int getRedisSentinel() {
        return REDIS_SENTINEL;
    }

    public static int getRedisCluster() {
        return REDIS_CLUSTER;
    }

    public int getClusterType() {
        return clusterType;
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

    @Override
    public String toString() {
        return "ClusterClientConfig{" +
                "clusterType=" + clusterType +
                ", masterName='" + masterName + '\'' +
                ", hostAndPorts=" + hostAndPorts +
                ", maxWaitMillis=" + maxWaitMillis +
                ", maxActive=" + maxActive +
                ", maxIdle=" + maxIdle +
                ", minIdle=" + minIdle +
                ", connectTimeout=" + connectTimeout +
                ", maxAttempts=" + maxAttempts +
                ", password='" + password + '\'' +
                '}';
    }
}
