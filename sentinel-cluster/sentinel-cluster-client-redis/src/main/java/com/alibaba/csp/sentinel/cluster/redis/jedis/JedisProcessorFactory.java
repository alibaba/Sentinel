package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessor;
import com.alibaba.csp.sentinel.cluster.redis.RedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisProcessorFactory implements RedisProcessorFactory {
    private JedisPool pool;

    public JedisProcessorFactory(ClusterClientConfig clientConfig) {
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
    public RedisProcessor getProcessor() {
        return new JedisProcessor(pool.getResource());
    }

    @Override
    public void destroy() {
        pool.close();
    }
}
