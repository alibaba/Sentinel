package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessor;
import com.alibaba.csp.sentinel.cluster.redis.RedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


import java.util.HashSet;
import java.util.Set;

public class JedisClusterProcessorFactory implements RedisProcessorFactory {
    private JedisCluster jedisCluster;
    public JedisClusterProcessorFactory(ClusterClientConfig config) {
        Set<redis.clients.jedis.HostAndPort> jedisNodes = new HashSet<>();
        for (HostAndPort hostAndPort : config.getHostAndPorts()) {
            jedisNodes.add(new redis.clients.jedis.HostAndPort(hostAndPort.getHostText(), hostAndPort.getPort()));
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxIdle(config.getMaxIdle());
        poolConfig.setMinIdle(config.getMinIdle());
        poolConfig.setMaxTotal(config.getMaxActive());
        poolConfig.setMaxWaitMillis(config.getMaxWaitMillis());

        jedisCluster = new JedisCluster(jedisNodes,
                config.getConnectTimeout(),config.getConnectTimeout(), config.getMaxAttempts(), config.getPassword(),
                poolConfig);
    }

    @Override
    public RedisProcessor getProcessor() {
        return new JedisClusterProcessor(jedisCluster);
    }

    @Override
    public void destroy() {
        jedisCluster.close();
    }
}
