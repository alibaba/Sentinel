package com.alibaba.csp.sentinel.dashboard.repository;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author FengJianxin
 * @since 1.8.6.2
 */
public class AtomicIdGen implements IdGen {

    private final AtomicLong ids;

    public AtomicIdGen() {
        ids = new AtomicLong(0);
    }

    @Override
    public long nextId() {
        return ids.incrementAndGet();
    }
}
