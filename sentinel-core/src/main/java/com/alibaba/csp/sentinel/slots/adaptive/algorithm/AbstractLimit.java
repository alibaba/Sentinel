package com.alibaba.csp.sentinel.slots.adaptive.algorithm;

import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slots.adaptive.AdaptiveRule;

import java.util.Queue;

/**
 * @author ElonTusk
 * @name AbstractLimit
 * @date 2023/8/2 14:48
 */
public abstract class AbstractLimit {
    public abstract int update(AdaptiveRule rule, StatisticNode node);
}
