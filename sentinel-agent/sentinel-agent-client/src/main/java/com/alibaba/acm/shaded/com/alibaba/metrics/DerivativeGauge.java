package com.alibaba.acm.shaded.com.alibaba.metrics;

/**
 * A gauge whose value is derived from the value of another gauge.
 *
 * @param <F> the base gauge's value type
 * @param <T> the derivative type
 */
public abstract class DerivativeGauge<F, T> implements Gauge<T> {

    private final Gauge<F> base;
    private long lastUpdate;

    /**
     * Creates a new derivative with the given base gauge.
     *
     * @param base the gauge from which to derive this gauge's value
     */
    protected DerivativeGauge(Gauge<F> base) {
        this.base = base;
    }

    @Override
    public T getValue() {
        T result = transform(base.getValue());
        lastUpdate = System.currentTimeMillis();
        return result;
    }

    @Override
    public long lastUpdateTime() {
        return lastUpdate;
    }

    /**
     * Transforms the value of the base gauge to the value of this gauge.
     *
     * @param value the value of the base gauge
     * @return this gauge's value
     */
    protected abstract T transform(F value);
}