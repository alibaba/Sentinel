package com.alibaba.csp.sentinel.cluster.redis.lua;

public interface RedisScriptLoader {
    String load(String luaCode, long flowId);
}
