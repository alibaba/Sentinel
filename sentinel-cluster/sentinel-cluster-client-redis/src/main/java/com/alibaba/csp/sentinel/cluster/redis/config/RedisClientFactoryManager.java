package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.RedisClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClusterClientFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisSentinelFactory;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;

public class RedisClientFactoryManager implements ServerChangeObserver{
    public static final int JEDIS_CLIENT = 1;
    public static final int LETTUCE_CLIENT = 2;
    public static final int UNKNOWN_CLIENT = -1;

    private static int clientType = -1;
    private static ClusterClientConfig clientConfig;

    private static RedisClientFactory factory;

    public static RedisClientFactory getFactory() {
        return factory;
    }

    public static void setFactory(RedisClientFactory factory) {
        RedisClientFactoryManager.factory = factory;
    }

    public static int getClientType() {
        return clientType;
    }

    public static void setClientType(int clientType) {
        RedisClientFactoryManager.clientType = clientType;
        rebuildClientFactory();
    }

    @Override
    public void onRemoteServerChange(ClusterClientConfig assignConfig) {
        RedisClientFactoryManager.clientConfig = assignConfig;

        rebuildClientFactory();
    }


    private static void rebuildClientFactory() {
        if(clientType == UNKNOWN_CLIENT || clientConfig == null) {
            return;
        }

        if(factory != null) {
            factory.destroy();
        }

        LuaUtil.resetLuaSha();
        if(clientType == JEDIS_CLIENT) {
            if(clientConfig.getDistributedType() == ClusterClientConfig.REDIS_DISTRIBUTED_CLUSTER) {
                factory = new JedisClusterClientFactory(clientConfig);
            } else if(clientConfig.getDistributedType() == ClusterClientConfig.REDIS_DISTRIBUTED_SINGLE) {
                factory = new JedisClientFactory(clientConfig);
            } else if(clientConfig.getDistributedType() == ClusterClientConfig.REDIS_DISTRIBUTED_SENTINEL) {
                factory = new JedisSentinelFactory(clientConfig);
            } else {
                throw new IllegalArgumentException("cannot init process redis distributed type:" + clientConfig.getDistributedType());
            }
        } else if(clientType == LETTUCE_CLIENT) {
            // todo
        }
    }
}