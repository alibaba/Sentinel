package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.util.Snowflake;

/**
 * @author FengJianxin
 * @since 1.8.6.4
 */
public class SnowflakeIdGen implements IdGen {

    private final Snowflake SF;

    public SnowflakeIdGen() {
        SF = new Snowflake();
    }

    @Override
    public long nextId() {
        return SF.nextId();
    }

}
