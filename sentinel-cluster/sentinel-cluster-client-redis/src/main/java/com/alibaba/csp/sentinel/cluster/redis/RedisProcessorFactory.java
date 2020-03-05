package com.alibaba.csp.sentinel.cluster.redis;

public interface RedisProcessorFactory {
    /**
     * create redis processor
     * @return
     */
    RedisProcessor getProcessor();

    /**
     * destroy factory
     */
    void destroy();
}
