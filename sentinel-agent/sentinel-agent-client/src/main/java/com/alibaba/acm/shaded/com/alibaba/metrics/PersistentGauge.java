package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * A subclass of {@link Gauge} which should be persistent.
 * A gauge that is never invalidated.
 * @author wangtao 2018-05-22 15:56.
 */
public abstract class PersistentGauge<T> implements Gauge<T> {

    /**
     * This gauge is always available, and be updated constantly.
     */
    @Override
    public long lastUpdateTime() {
        return System.currentTimeMillis();
    }
}
