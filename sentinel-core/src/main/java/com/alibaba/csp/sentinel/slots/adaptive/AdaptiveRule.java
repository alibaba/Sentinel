package com.alibaba.csp.sentinel.slots.adaptive;


import com.alibaba.csp.sentinel.slots.adaptive.algorithm.AbstractLimit;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ElonTusk
 * @name AdaptiveRule
 * @date 2023/8/2 13:35
 */
public class AdaptiveRule {
    private int strategy;
    private String refResource;

    private int count;

    public int getStrategy() {
        return strategy;
    }

    public AdaptiveRule setStrategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public String getResource() {
        return refResource;
    }

    public AdaptiveRule setResource(String refResource) {
        this.refResource = refResource;
        return this;
    }

    public int getCount() {
        return count;
    }

    public AdaptiveRule setCount(int count) {
        this.count = count;
        return this;
    }

    private AbstractLimit limiter;

    private Queue<Integer> oldCounts = new ConcurrentLinkedQueue();

    private final int oldCountsMaxSize = RuleConstant.OLD_COUNTS_MAX_SIZE;

    private AtomicInteger times = new AtomicInteger(0);

    public AbstractLimit getLimiter() {
        return limiter;
    }

    public AdaptiveRule setLimiter(AbstractLimit limiter) {
        this.limiter = limiter;
        return this;
    }

    public boolean addCount(int count) {
        while (oldCounts.size() >= oldCountsMaxSize) {
            oldCounts.poll();
        }
        return oldCounts.add(count);
    }

    public Queue<Integer> getOldCounts() {
        return oldCounts;
    }

    public int incrementTimes() {
        return times.incrementAndGet();
    }

    AdaptiveRule setTimes(int times) {
        this.times.set(times);
        return this;
    }
}
