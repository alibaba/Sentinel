package com.alibaba.csp.sentinel.cluster.redis.jedis;

import com.alibaba.csp.sentinel.cluster.redis.RedisClient;
import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.redis.config.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


import java.util.HashSet;
import java.util.Set;

public class JedisClusterClientFactory  implements RedisClientFactory {
    private JedisCluster jedisCluster;
    public JedisClusterClientFactory(ClusterClientConfig config) {
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
    public RedisClient getClient() {
        return new JedisClusterClient(jedisCluster);
    }

    @Override
    public void destroy() {
        jedisCluster.close();
    }
}
