package com.alibaba.csp.sentinel.slots.adaptive.algorithm;

import java.util.Map;
import java.util.Queue;

/**
 * @author ElonTusk
 * @name BRPC
 * @date 2023/8/17 16:27
 */
public class BRPCLimit extends AbstractLimit {

    private static class BRPCLimitContainer {
        private static BRPCLimit instance = new BRPCLimit();
    }

    public static AbstractLimit getInstance() {
        return BRPCLimit.BRPCLimitContainer.instance;
    }

    double alpha = 0.3;
    double min_explore_ratio = 1.1;
    double max_explore_ratio = 1.3;
    double correction_factor = 0.1;
    double change_step = 0.02;
    double maxQps = 0;
    double explore_ratio = 1;

    @Override
    public int update(Queue<Integer> oldLimits, double minRt, double rt, double passQps) {
        double emaFactor = alpha / 10;
        if (passQps >= maxQps) {
            maxQps = passQps;
        } else {
            maxQps = passQps * emaFactor + maxQps * (1 - emaFactor);
        }

        //double maxConcurrency = maxQps * ((1 + alpha) * minRt  - rt) / 1000;
        if (rt <= minRt * (1.0 + min_explore_ratio * correction_factor) ||
                passQps <= maxQps / (1.0 + min_explore_ratio)) {
            explore_ratio = Math.min(max_explore_ratio, explore_ratio + change_step);
        } else {
            explore_ratio = Math.max(min_explore_ratio, explore_ratio - change_step);
        }
        double maxConcurrency =
                minRt * maxQps * (1 + explore_ratio);
        System.out.println("\n" + maxQps + " " + explore_ratio);
        int maxQps = (int) (maxConcurrency / rt);
        maxQps = Math.max(10, Math.min(maxQps, 1000));
        return maxQps;
    }
}
