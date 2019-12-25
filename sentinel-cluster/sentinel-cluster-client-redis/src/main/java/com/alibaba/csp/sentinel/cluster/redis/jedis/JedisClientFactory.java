package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisClientFactory implements RedisClientFactory {
    private JedisPool pool;

    public JedisClientFactory(ClusterClientConfig clientConfig) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(clientConfig.getMaxWaitMillis());
        poolConfig.setMaxTotal(clientConfig.getMaxActive());
        poolConfig.setMaxIdle(clientConfig.getMaxIdle());
        poolConfig.setMinIdle(clientConfig.getMinIdle());

        HostAndPort hostAndPort = clientConfig.getHostAndPorts().iterator().next();

        pool = new JedisPool(poolConfig, hostAndPort.getHostText(), hostAndPort.getPort(),
                clientConfig.getConnectTimeout(),
                clientConfig.getPassword());

    }

    @Override
    public RedisClient getClient() {
        return new JedisClient(pool.getResource());
    }

    @Override
    public void destroy() {
        pool.close();
    }
}
