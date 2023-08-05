package com.alibaba.csp.sentinel.slots.adaptive.algorithm;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ElonTusk
 * @name GradientLimit
 * @date 2023/8/17 15:28
 */
public class GradientLimit extends AbstractLimit {
    private static class GradientLimitContainer {
        private static GradientLimit instance = new GradientLimit();
    }

    public static AbstractLimit getInstance() {
        return GradientLimit.GradientLimitContainer.instance;
    }

    private int minLimit = 10;
    private int maxLimit = 200;
    private int window = 60;
    private int warmupWindow = 10;
    private double tolerance = 0.4;

    @Override
    public int update(Queue<Integer> oldLimits, double minRt, double rt, double passQps) {
        double estimatedQps = 20;
        for (Integer oldLimit : oldLimits) {
            estimatedQps = oldLimit;
        }
        double estimatedLimit = estimatedQps * rt;
        final double queueSize = Math.sqrt(estimatedLimit);

        double shortRtt = minRt;
        double longRtt = calLongRtt(rt);


        if (longRtt / shortRtt > 2) {
            longRtt = longRtt * 0.95;
        }

        final double gradient = Math.max(0.5, Math.min(1.0, tolerance * longRtt / shortRtt));
        double newLimit = estimatedLimit * gradient + queueSize;
        newLimit = estimatedLimit * (1 - RuleConstant.ADAPTIVE_LIMIT_SMOOTHING) + newLimit * RuleConstant.ADAPTIVE_LIMIT_SMOOTHING;
        newLimit = Math.max(minLimit * rt, Math.min(maxLimit * rt, newLimit));

        estimatedQps = newLimit / rt;
        return (int) estimatedQps;
    }

    private AtomicInteger count = new AtomicInteger(0);

    private double sum = 0.0;
    private double value = 0.0;

    private double calLongRtt(double rt) {
        if (count.get() < warmupWindow) {
            count.incrementAndGet();
            sum += rt;
            value = sum / count.get();
        } else {
            double factor = factor(window);
            value = value * (1 - factor) + rt * factor;
        }
        return value;
    }

    private double factor(int n) {
        return 2.0 / (n + 1);
    }
}
