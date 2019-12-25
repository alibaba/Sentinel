package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class JedisSentinelFactory  implements RedisClientFactory {
    private JedisSentinelPool sentinelPool;

    public JedisSentinelFactory(ClusterClientConfig clientConfig) {
        Set sentinelNodes = new HashSet();
        for (HostAndPort hostAndPort : clientConfig.getHostAndPorts()) {
            sentinelNodes.add(new redis.clients.jedis.HostAndPort(hostAndPort.getHostText(), hostAndPort.getPort()).toString());
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(clientConfig.getMaxWaitMillis());
        poolConfig.setMaxTotal(clientConfig.getMaxActive());
        poolConfig.setMaxIdle(clientConfig.getMaxIdle());
        poolConfig.setMinIdle(clientConfig.getMinIdle());

        sentinelPool = new JedisSentinelPool(clientConfig.getMasterName(), sentinelNodes,
                poolConfig, clientConfig.getConnectTimeout(), clientConfig.getPassword());
    }

    @Override
    public RedisClient getClient() {
        return new JedisClient(sentinelPool.getResource());
    }

    @Override
    public void destroy() {
        sentinelPool.close();
    }
}
