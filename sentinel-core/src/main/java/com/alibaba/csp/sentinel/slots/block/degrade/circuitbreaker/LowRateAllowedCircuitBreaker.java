package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class LowRateAllowedCircuitBreaker extends AbstractCircuitBreaker {
    private final int strategy;
    private final int minRequestAmount;
    private final double threshold;
    private final double lowRateThreshold;

    // Sliding time window for statistics
    private final LeapArray<SimpleErrorCounter> stat;

    // Token bucket related variables
    private final AtomicLong storedTokens = new AtomicLong(0);
    private final AtomicLong lastRefillTime = new AtomicLong(0);
    private final long intervalInMs = 1000; // 1 second
    private final long maxTokenNum;

    public LowRateAllowedCircuitBreaker(DegradeRule rule) {
        this(rule, new SimpleErrorCounterLeapArray(1, rule.getStatIntervalMs()));
    }

    LowRateAllowedCircuitBreaker(DegradeRule rule, LeapArray<SimpleErrorCounter> stat) {
        super(rule);
        this.strategy = rule.getGrade();
        AssertUtil.isTrue(strategy == RuleConstant.DEGRADE_GRADE_EXCEPTION_THRESHOLD,
                "LowRateAllowedCircuitBreaker only supports exception ratio strategy");
        AssertUtil.notNull(stat, "stat cannot be null");

        this.minRequestAmount = rule.getMinRequestAmount();
        this.threshold = rule.getCount();
        this.stat = stat;

        // Get low-rate threshold from rule, default 1 QPS
        this.lowRateThreshold = Math.max(1, rule.getSlowRatioThreshold());
        this.maxTokenNum = (long) lowRateThreshold; // Max tokens equal to QPS threshold

        // Initialize token bucket
        initTokenBucket();

    }

    /**
     * Initialize token bucket
     */
    private void initTokenBucket() {
        storedTokens.set(maxTokenNum); // Start with full token bucket
        lastRefillTime.set(TimeUtil.currentTimeMillis());
    }

    /**
     * Reset token bucket state
     */
    private void resetTokenBucket() {
        storedTokens.set(maxTokenNum);
        lastRefillTime.set(TimeUtil.currentTimeMillis());
    }

    @Override
    protected void resetStat() {
        // Reset statistics
        stat.currentWindow().value().reset();
        // Reset token bucket
        resetTokenBucket();
    }

    @Override
    public void onRequestComplete(Context context) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }

        Throwable error = entry.getError();
        SimpleErrorCounter counter = stat.currentWindow().value();

        // Update statistics
        if (error != null) {
            counter.getErrorCount().add(1);
        }
        counter.getTotalCount().add(1);

        // Handle state transition when threshold is exceeded
        handleStateChangeWhenThresholdExceeded(error);
    }

    @Override
    public boolean tryPass(Context context) {
        State state = currentState.get();

        // Allow all requests in CLOSED state
        if (state == State.CLOSED) {
            return true;
        }

        // Check if we can try recovery in HALF_OPEN state
        if (state == State.HALF_OPEN) {
            return tryAcquireHalfOpenPermit();
        }

        // Control low-rate access using token bucket in OPEN state
        if (state == State.OPEN) {
            if (retryTimeoutArrived()) {
                if (fromOpenToHalfOpen(context)) {
                    resetStat();
                    return true;
                }
            }
            return tryAcquireToken();
        }

        return false;
    }

    /**
     * Try to acquire permit in HALF_OPEN state
     */
    private boolean tryAcquireHalfOpenPermit() {
        // Only allow one probe request in HALF_OPEN state
        return storedTokens.getAndUpdate(t -> t > 0 ? t - 1 : t) > 0;
    }

    /**
     * Try to acquire a token
     */
    private boolean tryAcquireToken() {
        long currentTime = TimeUtil.currentTimeMillis();
        syncToken(currentTime);

        // Atomically try to acquire a token
        return storedTokens.getAndUpdate(t -> t > 0 ? t - 1 : t) > 0;
    }

    /**
     * Sync token count based on elapsed time
     */
    private void syncToken(long currentTime) {
        long lastTime = lastRefillTime.get();

        // Calculate tokens to refill
        if (currentTime > lastTime) {
            long elapsedTime = currentTime - lastTime;
            long newTokens = (long) (elapsedTime * lowRateThreshold / intervalInMs);

            if (newTokens > 0) {
                // Use CAS to ensure thread safety
                if (lastRefillTime.compareAndSet(lastTime, currentTime)) {
                    // Atomically update token count, not exceeding max capacity
                    storedTokens.updateAndGet(old -> Math.min(old + newTokens, maxTokenNum));
                }
            }
        }
    }

    /**
     * Handle state transition when threshold is exceeded
     */
    private void handleStateChangeWhenThresholdExceeded(Throwable error) {
        State state = currentState.get();

        // Do nothing if already in OPEN state
        if (state == State.OPEN) {
            return;
        }

        // Handle request result in HALF_OPEN state
        if (state == State.HALF_OPEN) {
            if (error == null) {
                // Probe request succeeded, transition to CLOSED state
                fromHalfOpenToClose();
            } else {
                // Probe request failed, reopen the circuit breaker
                fromHalfOpenToOpen(1.0d);
            }
            return;
        }

        // Check if need to trigger degradation in CLOSED state
        List<SimpleErrorCounter> counters = stat.values();
        long errCount = 0;
        long totalCount = 0;

        for (SimpleErrorCounter counter : counters) {
            errCount += counter.errorCount.sum();
            totalCount += counter.totalCount.sum();
        }

        // Check if minimum request count is reached
        if (totalCount < minRequestAmount) {
            return;
        }

        // Calculate current failure ratio
        double currentRatio = errCount * 1.0d / totalCount;

        // Trigger degradation if threshold is exceeded
        if (currentRatio > threshold) {
            transformToOpen(currentRatio);
        }
    }

    // Simple error counter
    static class SimpleErrorCounter {
        private LongAdder errorCount;
        private LongAdder totalCount;

        public SimpleErrorCounter() {
            this.errorCount = new LongAdder();
            this.totalCount = new LongAdder();
        }

        public LongAdder getErrorCount() {
            return errorCount;
        }

        public LongAdder getTotalCount() {
            return totalCount;
        }

        public SimpleErrorCounter reset() {
            errorCount.reset();
            totalCount.reset();
            return this;
        }
    }

    // Leap array for simple error counter
    static class SimpleErrorCounterLeapArray extends LeapArray<SimpleErrorCounter> {
        public SimpleErrorCounterLeapArray(int sampleCount, int intervalInMs) {
            super(sampleCount, intervalInMs);
        }

        @Override
        public SimpleErrorCounter newEmptyBucket(long timeMillis) {
            return new SimpleErrorCounter();
        }

        @Override
        protected WindowWrap<SimpleErrorCounter> resetWindowTo(WindowWrap<SimpleErrorCounter> w, long startTime) {
            w.resetTo(startTime);
            w.value().reset();
            return w;
        }
    }
}
