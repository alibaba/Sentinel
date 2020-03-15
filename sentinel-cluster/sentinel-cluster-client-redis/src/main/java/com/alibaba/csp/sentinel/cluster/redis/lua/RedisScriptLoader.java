package com.alibaba.csp.sentinel.cluster.redis.lua;

public interface RedisScriptLoader {
    /**
     * load luaCode to redis
     * @param luaCode
     * @param slotKey
     * @return
     */
    String load(String luaCode, String slotKey);
}
