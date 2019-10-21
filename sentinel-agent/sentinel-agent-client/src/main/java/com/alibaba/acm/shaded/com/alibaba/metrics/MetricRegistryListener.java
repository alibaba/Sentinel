package com.alibaba.acm.shaded.com.alibaba.metrics;

import java.util.EventListener;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricRegistryListener extends EventListener {
    /**
     * A no-op implementation of {@link MetricRegistryListener}.
     */
    abstract class Base implements MetricRegistryListener {
        @Override
        public void onGaugeAdded(MetricName name, Gauge<?> gauge) {
        }

        @Override
        public void onGaugeRemoved(MetricName name) {
        }

        @Override
        public void onCounterAdded(MetricName name, Counter counter) {
        }

        @Override
        public void onCounterRemoved(MetricName name) {
        }

        @Override
        public void onHistogramAdded(MetricName name, Histogram histogram) {
        }

        @Override
        public void onHistogramRemoved(MetricName name) {
        }

        @Override
        public void onMeterAdded(MetricName name, Meter meter) {
        }

        @Override
        public void onMeterRemoved(MetricName name) {
        }

        @Override
        public void onTimerAdded(MetricName name, Timer timer) {
        }

        @Override
        public void onTimerRemoved(MetricName name) {
        }

        @Override
        public void onCompassAdded(MetricName name, Compass compass) {

        }

        @Override
        public void onCompassRemoved(MetricName name) {

        }

        @Override
        public void onFastCompassAdded(MetricName name, FastCompass compass) {

        }

        @Override
        public void onFastCompassRemoved(MetricName name) {

        }
    }

    /**
     * Called when a {@link Gauge} is added to the registry.
     *
     * @param name  the gauge's name
     * @param gauge the gauge
     */
    void onGaugeAdded(MetricName name, Gauge<?> gauge);

    /**
     * Called when a {@link Gauge} is removed from the registry.
     *
     * @param name the gauge's name
     */
    void onGaugeRemoved(MetricName name);

    /**
     * Called when a {@link Counter} is added to the registry.
     *
     * @param name    the counter's name
     * @param counter the counter
     */
    void onCounterAdded(MetricName name, Counter counter);

    /**
     * Called when a {@link Counter} is removed from the registry.
     *
     * @param name the counter's name
     */
    void onCounterRemoved(MetricName name);

    /**
     * Called when a {@link Histogram} is added to the registry.
     *
     * @param name      the histogram's name
     * @param histogram the histogram
     */
    void onHistogramAdded(MetricName name, Histogram histogram);

    /**
     * Called when a {@link Histogram} is removed from the registry.
     *
     * @param name the histogram's name
     */
    void onHistogramRemoved(MetricName name);

    /**
     * Called when a {@link Meter} is added to the registry.
     *
     * @param name  the meter's name
     * @param meter the meter
     */
    void onMeterAdded(MetricName name, Meter meter);

    /**
     * Called when a {@link Meter} is removed from the registry.
     *
     * @param name the meter's name
     */
    void onMeterRemoved(MetricName name);

    /**
     * Called when a {@link Timer} is added to the registry.
     *
     * @param name  the timer's name
     * @param timer the timer
     */
    void onTimerAdded(MetricName name, Timer timer);

    /**
     * Called when a {@link Timer} is removed from the registry.
     *
     * @param name the timer's name
     */
    void onTimerRemoved(MetricName name);

    /**
     * Called when a {@link Compass} is added to the registry.
     *
     * @param name  the compass's name
     * @param compass the compass object added
     */
    void onCompassAdded(MetricName name, Compass compass);

    /**
     * Called when a {@link Compass} is removed from the registry.
     *
     * @param name the compass's name
     */
    void onCompassRemoved(MetricName name);

    /**
     * Called when a {@link FastCompass} is added to the registry.
     * @param name the FastCompass's name
     * @param compass the FastCompass object added
     */
    void onFastCompassAdded(MetricName name, FastCompass compass);

    /**
     * Called when a {@link FastCompass} is removed from the registry.
     *
     * @param name the FastCompass's name
     */
    void onFastCompassRemoved(MetricName name);
}
