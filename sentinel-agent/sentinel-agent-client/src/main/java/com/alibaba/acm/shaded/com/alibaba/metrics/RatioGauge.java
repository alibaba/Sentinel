package com.alibaba.acm.shaded.com.alibaba.metrics;

import static com.alibaba.acm.shaded.com.alibaba.metrics.Constants.NOT_AVAILABLE;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

/**
 * A gauge which measures the ratio of one value to another.
 * <p/>
 * If the denominator is zero, not a number, or infinite, the resulting ratio is not a number.
 *
 * 一种衡量比率的度量器，传入分子和分母，该度量器会自动计算比率
 */
public abstract class RatioGauge implements Gauge<Double> {

    private long lastUpdated = System.currentTimeMillis();

    /**
     * A ratio of one quantity to another.
     */
    public static class Ratio {
        /**
         * Creates a new ratio with the given numerator and denominator.
         *
         * @param numerator      the numerator of the ratio
         * @param denominator    the denominator of the ratio
         * @return {@code numerator:denominator}
         */
        public static Ratio of(double numerator, double denominator) {
            return new Ratio(numerator, denominator);
        }

        private final double numerator;
        private final double denominator;

        private Ratio(double numerator, double denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        /**
         * Returns the ratio, which is either a {@code double} between 0 and 1 (inclusive) or
         * {@code NaN}.
         *
         * @return the ratio
         */
        public double getValue() {
            final double d = denominator;
            if (isNaN(d) || isInfinite(d) || d == 0) {
                return Double.NaN;
            }
            return numerator / d;
        }

        @Override
        public String toString() {
            return numerator + ":" + denominator;
        }
    }

    /**
     * Returns the {@link Ratio} which is the gauge's current value.
     *
     * @return the {@link Ratio} which is the gauge's current value
     */
    protected abstract Ratio getRatio();

    public Double getValue() {
        try {
            Ratio r = getRatio();
            if (r == null) {
                return (double)NOT_AVAILABLE;
            }
            double result = r.getValue();
            lastUpdated = System.currentTimeMillis();
            return result;
        } catch (Exception e) {
            return (double)NOT_AVAILABLE;
        }
    }

    @Override
    public long lastUpdateTime() {
        return lastUpdated;
    }
}
