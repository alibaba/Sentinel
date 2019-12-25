package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Set;

public interface RedisClient {

    /**
     * request token from redis
     * @param luaCode
     * @param requestData
     * @return
     */
    int requestToken(String luaCode, RequestData requestData);

    /**
     * reset flow metrics data when publish rule
     * @param flowIds
     */
    void resetFlowMetrics(Set<Long> flowIds);

    /**
     * publish rule to redis
     * @param rule
     */
    void publishRule(FlowRule rule);

    /**
     * clear redis rule
     * @param flowIds
     */
    void clearRule(Set<Long> flowIds);

    /**
     * close redis client
     */
    void close();
}
