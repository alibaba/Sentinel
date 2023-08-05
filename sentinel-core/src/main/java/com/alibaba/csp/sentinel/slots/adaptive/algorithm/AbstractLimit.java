package com.alibaba.csp.sentinel.slots.adaptive.algorithm;

import java.util.Queue;

/**
 * @author ElonTusk
 * @name AbstractLimit
 * @date 2023/8/2 14:48
 */
public abstract class AbstractLimit {
    public abstract int update(Queue<Integer> oldLimits, double minRt, double rt, double passQps);
}
