package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.util.function.Supplier;

/**
 * @author FengJianxin
 * @since 1.8.6.2
 */
public enum IdGenType {

    SNOWFLAKE(SnowflakeIdGen::new),
    ATOMIC(AtomicIdGen::new),
    ;


    private final Supplier<IdGen> constructor;

    IdGenType(final Supplier<IdGen> constructor) {
        this.constructor = constructor;
    }

    public Supplier<IdGen> getConstructor() {
        return constructor;
    }
}
