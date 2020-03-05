package com.alibaba.csp.sentinel.cluster.redis.config;

import com.alibaba.csp.sentinel.cluster.redis.RedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisClusterProcessorFactory;
import com.alibaba.csp.sentinel.cluster.redis.jedis.JedisSentinelFactory;
import com.alibaba.csp.sentinel.cluster.redis.lua.LuaUtil;
import com.alibaba.csp.sentinel.log.RecordLog;

public class RedisProcessorFactoryManager implements ServerChangeObserver{
    public static final int JEDIS_CLIENT = 1;
    public static final int LETTUCE_CLIENT = 2;
    public static final int UNKNOWN_CLIENT = -1;

    private static int clientType = -1;
    private static ClusterClientConfig clientConfig;
    private static RedisProcessorFactory factory;

    public static RedisProcessorFactory getFactory() {
        return factory;
    }

    public static void setFactory(RedisProcessorFactory factory) {
        RedisProcessorFactoryManager.factory = factory;
    }

    public static int getClientType() {
        return clientType;
    }

    public static void setClientType(int clientType) {
        RedisProcessorFactoryManager.clientType = clientType;
        rebuildClientFactory();
    }

    @Override
    public void onRemoteServerChange(ClusterClientConfig assignConfig) {
        RedisProcessorFactoryManager.clientConfig = assignConfig;

        rebuildClientFactory();
    }


    private static void rebuildClientFactory() {
        if(clientType == UNKNOWN_CLIENT || clientConfig == null) {
            RecordLog.info("[RedisProcessorFactoryManager] cannot build client factory, clientType: " + clientType + ", clientConfig:" + clientConfig);
            return;
        }

        if(factory != null) {
            factory.destroy();
        }

        LuaUtil.resetLuaSha();
        if(clientType == JEDIS_CLIENT) {
            if(clientConfig.getClusterType() == ClusterClientConfig.REDIS_CLUSTER) {
                factory = new JedisClusterProcessorFactory(clientConfig);
            } else if(clientConfig.getClusterType() == ClusterClientConfig.REDIS_SINGLE) {
                factory = new JedisProcessorFactory(clientConfig);
            } else if(clientConfig.getClusterType() == ClusterClientConfig.REDIS_SENTINEL) {
                factory = new JedisSentinelFactory(clientConfig);
            } else {
                throw new IllegalArgumentException("cannot init process redis distributed type:" + clientConfig.getClusterType());
            }
        } else if(clientType == LETTUCE_CLIENT) {
            // todo
        }
    }
}
