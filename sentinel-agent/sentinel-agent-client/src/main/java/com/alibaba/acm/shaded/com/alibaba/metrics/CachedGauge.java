package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Gauge} implementation which caches its value for a period of time.
 *
 * @param <T>    the type of the gauge's value
 */
public abstract class CachedGauge<T> implements Gauge<T> {
    private final Clock clock;
    private final AtomicLong reloadAt;
    private final long timeoutNS;

    private volatile T value;

    /**
     * Creates a new cached gauge with the given timeout period.
     *
     * @param timeout        the timeout
     * @param timeoutUnit    the unit of {@code timeout}
     */
    protected CachedGauge(long timeout, TimeUnit timeoutUnit) {
        this(Clock.defaultClock(), timeout, timeoutUnit);
    }

    /**
     * Creates a new cached gauge with the given clock and timeout period.
     *
     * @param clock          the clock used to calculate the timeout
     * @param timeout        the timeout
     * @param timeoutUnit    the unit of {@code timeout}
     */
    protected CachedGauge(Clock clock, long timeout, TimeUnit timeoutUnit) {
        this.clock = clock;
        this.reloadAt = new AtomicLong(clock.getTime());
        this.timeoutNS = timeoutUnit.toNanos(timeout);
    }

    /**
     * Loads the value and returns it.
     *
     * @return the new value
     */
    protected abstract T loadValue();

    public T getValue() {
        if (shouldLoad()) {
            this.value = loadValue();
        }
        return value;
    }

    @Override
    public long lastUpdateTime() {
        return reloadAt.get();
    }

    private boolean shouldLoad() {
        for (; ; ) {
            final long time = clock.getTick();
            final long current = reloadAt.get();
            if (current > time) {
                return false;
            }
            if (reloadAt.compareAndSet(current, time + timeoutNS)) {
                return true;
            }
        }
    }
}
