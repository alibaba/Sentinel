/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author Eric Zhao
 * @since 1.8.0
 */
public class ResponseTimeCircuitBreaker extends AbstractCircuitBreaker {

    private static final double SLOW_REQUEST_RATIO_MAX_VALUE = 1.0d;

    private final long maxAllowedRt;
    private final double maxSlowRequestRatio;
    private final int minRequestAmount;

    private final SlowRequestLeapArray slidingCounter;

    public ResponseTimeCircuitBreaker(DegradeRule rule) {
        super(rule);
        AssertUtil.isTrue(rule.getGrade() == RuleConstant.DEGRADE_GRADE_RT, "rule metric type should be RT");
        this.maxAllowedRt = Math.round(rule.getCount());
        this.maxSlowRequestRatio = rule.getSlowRatioThreshold();
        this.minRequestAmount = rule.getMinRequestAmount();
        /*
         * Init slidingCounter based on the value of statIntervalMs and maxAllowedRt
         * 1. If maxAllowedRt can be divided into statIntervalMs,
         *    then sampleCnt = (maxAllowedRt / statIntervalMs()) + 1
         * 2. Else sampleCnt = (maxAllowedRt / statIntervalMs()) + 2
         * In all the above cases, the windowLengthInMs of slidingCounter is equal to statIntervalMs,
         * and intervalInMs is greater than maxAllowedRt.
         */
        long sampleCntLong = maxAllowedRt / rule.getStatIntervalMs();
        AssertUtil.isTrue((int) sampleCntLong == sampleCntLong, "count of the max allowed rt is too large");
        boolean canDivided = maxAllowedRt % rule.getStatIntervalMs() == 0;
        int sampleCnt = (int) sampleCntLong + (canDivided ? 1 : 2);
        int intervalInMs = sampleCnt * rule.getStatIntervalMs();
        this.slidingCounter = new SlowRequestLeapArray(sampleCnt, intervalInMs);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * This implementation will try to check previous deprecated buckets when the status equal to {@code State.CLOSED}.
     */
    @Override
    public boolean tryPass(Context context) {
        boolean superTryPass = super.tryPass(context);
        if (!superTryPass) {
            return false;
        }
        long createTime = context.getCurEntry().getCreateTimestamp();
        SlowRequestCounter curCounter = slidingCounter.currentWindow(createTime).value();
        long curWindowTime = createTime - createTime % slidingCounter.getWindowLengthInMs();
        // Just check prev inflight request once.
        if (currentState.get() != State.HALF_OPEN
                && curCounter.getStatus() != SlowRequestCounter.CHECKED_BY_ENTRY
                && curCounter.casStatus(SlowRequestCounter.UNCHECKED, SlowRequestCounter.CHECKED_BY_ENTRY)) {
            long prevTotalCount = curCounter.getPrevTotalCount();
            if (prevTotalCount >= minRequestAmount && openIfNecessary(prevTotalCount, curCounter.getPrevSlowCount())) {
                return false;
            }
            // Check inflight request in deprecated buckets.
            List<SlowRequestCounter> deprecatedCounterList = slidingCounter.listAllDeprecated(curWindowTime);
            for (SlowRequestCounter deprecatedCounter : deprecatedCounterList) {
                long oldTotalCount = deprecatedCounter.getTotalCount();
                if (oldTotalCount >= minRequestAmount && openIfNecessary(oldTotalCount,
                        deprecatedCounter.getInflightCount() + deprecatedCounter.getSlowCount())) {
                    return false;
                }
            }
        }
        curCounter.totalCount.add(1L);
        curCounter.inflightCount.add(1L);
        return true;
    }

    @Override
    public void resetStat() {
        // Reset all bucket completely.
        slidingCounter.resetCompletely();
    }

    @Override
    public void onRequestComplete(Context context) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }
        SlowRequestCounter entryCounter = slidingCounter.getWindowValue(entry.getCreateTimestamp());
        // the original bucket is deprecated, it had been checked by successor.
        if (entryCounter == null) {
            return;
        }
        long completeTime = entry.getCompleteTimestamp();
        if (completeTime <= 0) {
            completeTime = TimeUtil.currentTimeMillis();
        }
        long rt = completeTime - entry.getCreateTimestamp();
        // decrease the inflight count and increase slow count if rt > maxAllowedRt.
        entryCounter.inflightCount.add(-1);
        if (rt > maxAllowedRt) {
            entryCounter.slowCount.add(1);
        }
        handleStateChangeWhenThresholdExceeded(rt, entry, entryCounter);
    }

    /**
     * calculate and transfer status to open.
     *
     * @return whether transferred to open.
     */
    private boolean openIfNecessary(long totalCount, long slowCount) {
        double currentRatio = slowCount * 1.0d / totalCount;
        if (currentRatio > maxSlowRequestRatio) {
            transformToOpen(currentRatio);
            return true;
        }
        if (Double.compare(currentRatio, maxSlowRequestRatio) == 0 &&
                Double.compare(maxSlowRequestRatio, SLOW_REQUEST_RATIO_MAX_VALUE) == 0) {
            transformToOpen(currentRatio);
            return true;
        }
        return false;
    }

    private void handleStateChangeWhenThresholdExceeded(long rt, Entry entry, SlowRequestCounter curCounter) {
        if (currentState.get() == State.OPEN) {
            return;
        }

        if (currentState.get() == State.HALF_OPEN) {
            // In detecting request
            // TODO: improve logic for half-open recovery
            if (rt > maxAllowedRt) {
                fromHalfOpenToOpen(1.0d);
            } else {
                fromHalfOpenToClose();
            }
            return;
        }

        if (rt <= maxAllowedRt) {
            return;
        }
        long curTotalCount = curCounter.getTotalCount();
        if (curTotalCount < minRequestAmount || curCounter.getStatus() != SlowRequestCounter.CHECKED_BY_ENTRY) {
            return;
        }
        long curInflightCount = curCounter.getInflightCount();
        long curSlowCount = curCounter.getSlowCount();
        long leftTimeInWindow = slidingCounter.getWindowLengthInMs() -
                entry.getCreateTimestamp() % slidingCounter.getWindowLengthInMs();
        if (rt - maxAllowedRt > leftTimeInWindow || (rt - leftTimeInWindow >= 0 && curInflightCount <= 0L)) {
            // We can ensure the slow count will not change.
            openIfNecessary(curTotalCount, curInflightCount + curSlowCount);
        } else {
            double currentRatio = curSlowCount * 1.0d / curTotalCount;
            boolean isEqualToMaxRatio = Double.compare(currentRatio, maxSlowRequestRatio) == 0 &&
                    Double.compare(maxSlowRequestRatio, SLOW_REQUEST_RATIO_MAX_VALUE) == 0;
            if (currentRatio > maxSlowRequestRatio || isEqualToMaxRatio) {
                transformToOpen(currentRatio);
            }
        }
    }

    static class SlowRequestCounter {
        /*
         * When a entry try pass, increase totalCount and inflightCount.
         * When a entry exits, decrease inflightCount and increase slowCount if rt > maxAllowedRt.
         * When this counter reset, prevSlowCount=inflightCount+slowCount and prevTotalCount=totalCount.
         */
        private LongAdder slowCount;
        private LongAdder inflightCount;
        private LongAdder totalCount;
        private LongAdder prevSlowCount;
        private LongAdder prevTotalCount;

        /** status value to indicate previous count has not been checked */
        static final int UNCHECKED = 0;
        /** status value to indicate previous count has been checked by a successor entry */
        static final int CHECKED_BY_ENTRY = 1;
        /*
         * status of the counter.
         */
        private AtomicInteger status;

        public SlowRequestCounter() {
            this.slowCount = new LongAdder();
            this.inflightCount = new LongAdder();
            this.totalCount = new LongAdder();
            this.prevSlowCount = new LongAdder();
            this.prevTotalCount = new LongAdder();
            this.status = new AtomicInteger(UNCHECKED);
        }

        public long getSlowCount() {
            return slowCount.sum();
        }

        public long getInflightCount() {
            return inflightCount.sum();
        }

        public long getTotalCount() {
            return totalCount.sum();
        }

        public long getPrevSlowCount() {
            return prevSlowCount.sum();
        }

        public long getPrevTotalCount() {
            return prevTotalCount.sum();
        }

        public int getStatus() {
            return status.get();
        }

        public boolean casStatus(int oldStatus, int status) {
            return this.status.compareAndSet(oldStatus, status);
        }

        /**
         * Track the previous deprecated bucket.
         */
        public SlowRequestCounter reset() {
            prevSlowCount.reset();
            prevSlowCount.add(slowCount.sumThenReset() + inflightCount.sumThenReset());
            prevTotalCount.reset();
            prevTotalCount.add(totalCount.sumThenReset());
            status.set(UNCHECKED);
            return this;
        }

        /**
         * Reset all counts and status.
         */
        public SlowRequestCounter resetCompletely() {
            prevSlowCount.reset();
            prevTotalCount.reset();
            inflightCount.reset();
            slowCount.reset();
            totalCount.reset();
            status.set(CHECKED_BY_ENTRY);
            return this;
        }

        @Override
        public String toString() {
            return "SlowRequestCounter{" +
                "slowCount=" + slowCount +
                ", inflightCount=" + inflightCount +
                ", totalCount=" + totalCount +
                ", prevSlowCount=" + prevSlowCount +
                ", prevTotalCount=" + prevTotalCount +
                ", status=" + status +
                '}';
        }
    }

    static class SlowRequestLeapArray extends LeapArray<SlowRequestCounter> {

        public SlowRequestLeapArray(int sampleCount, int intervalInMs) {
            super(sampleCount, intervalInMs);
        }

        @Override
        public SlowRequestCounter newEmptyBucket(long timeMillis) {
            return new SlowRequestCounter();
        }

        @Override
        protected WindowWrap<SlowRequestCounter> resetWindowTo(WindowWrap<SlowRequestCounter> w, long startTime) {
            w.resetTo(startTime);
            w.value().reset();
            return w;
        }

        public int getWindowLengthInMs() {
            return windowLengthInMs;
        }

        /**
         * Get all deprecated buckets for entire sliding window.
         *
         * @param timeMillis a valid timestamp in milliseconds
         * @return deprecated bucket list for entire sliding window.
         */
        @SuppressWarnings("unchecked")
        public List<SlowRequestCounter> listAllDeprecated(long timeMillis) {
            int size = array.length();
            if (size == 1) {
                return Collections.EMPTY_LIST;
            }
            int idx = (int) ((timeMillis / windowLengthInMs) % size);
            timeMillis -= timeMillis % windowLengthInMs;
            // Reduce memory footprint in normal case.
            List<SlowRequestCounter> counterList = Collections.EMPTY_LIST;
            for (int i = 0; i < size - 1; i++) {
                idx = (--idx) < 0 ? idx + size : idx;
                WindowWrap<SlowRequestCounter> windowWrap = array.get(idx);
                if (windowWrap == null) {
                    continue;
                }
                // There is a valid previous window.
                if (!isWindowDeprecated(timeMillis, windowWrap)) {
                    break;
                }
                if (counterList == Collections.EMPTY_LIST) {
                    counterList = new ArrayList<>(size - 1 - i);
                }
                counterList.add(windowWrap.value());
            }
            return counterList;
        }

        /**
         * Reset all counts and status in all bucket, and reset window start time.
         */
        public void resetCompletely() {
            int size = array.length();
            // Reset start timestamp to a small value.
            long resetTimeMillis = 0L;
            for (int i = 0; i < size; i++, resetTimeMillis += windowLengthInMs) {
                WindowWrap<SlowRequestCounter> windowWrap = array.get(i);
                if (windowWrap == null) {
                    continue;
                }
                SlowRequestCounter counter = windowWrap.value();
                counter.resetCompletely();
                windowWrap.resetTo(resetTimeMillis);
            }
        }
    }
}
