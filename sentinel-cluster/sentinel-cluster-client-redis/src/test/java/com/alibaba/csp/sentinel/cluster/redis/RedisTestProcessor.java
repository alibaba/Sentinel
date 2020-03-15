package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Map;

public interface RedisTestProcessor {
    void initRedisConf() throws NoSuchFieldException, IllegalAccessException;

    FlowRule getRule(String namespace, Long flowId);

    Map<String, String> getBucketCount(String namespace, Long flowId);
}
