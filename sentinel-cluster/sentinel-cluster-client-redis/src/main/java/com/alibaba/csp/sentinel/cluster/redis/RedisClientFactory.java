package com.alibaba.csp.sentinel.cluster.redis;

public interface RedisClientFactory {
    /**
     * create redis client
     * @return
     */
    RedisClient getClient();

    /**
     * destroy factory
     */
    void destroy();
}
