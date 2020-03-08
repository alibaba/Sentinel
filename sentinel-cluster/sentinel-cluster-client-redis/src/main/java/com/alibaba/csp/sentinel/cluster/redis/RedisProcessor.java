package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Set;

public interface RedisProcessor {

    /**
     * request token from redis
     * @param luaCode
     * @param requestData
     * @return
     */
    int requestToken(String luaCode, RequestData requestData);

    /**
     * reset rule(public rule and clear flow metrics)
     * @param rule
     */
    void resetRedisRuleAndMetrics(String namespace, FlowRule rule);

    /**
     * clear redis rule
     * @param flowIds
     */
    void clearRuleAndMetrics(String namespace, Set<Long> flowIds);

    /**
     * close redis client
     */
    void close();
}
