package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.cluster.redis.request.RequestData;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Set;

public interface RedisClient {

    /**
     * execute redis lua code
     * @param luaCode
     * @param requestData
     * @return
     */
    int executeLua(String luaCode, RequestData requestData);

    /**
     * publish rule to redis
     * @param rule
     */
    void publishRule(FlowRule rule);

    /**
     * clear redis rule
     * @param ruleIds
     */
    void clearRule(Set<Long> ruleIds);

    /**
     * close redis client
     */
    void close();
}
