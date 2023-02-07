/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author Eric Zhao
 * @author jialiang.linjl
 * @since 2.0
 */
public class ThrottlingController implements TrafficShapingController {

    // Refactored from legacy RateLimitController of Sentinel 1.x.

    private static final long MS_TO_NS_OFFSET = TimeUnit.MILLISECONDS.toNanos(1);

    private final int maxQueueingTimeMs;
    private final int statDurationMs;

    private final double count;
    private final boolean useNanoSeconds;

    private final AtomicLong latestPassedTime = new AtomicLong(-1);

    public ThrottlingController(int queueingTimeoutMs, double maxCountPerStat) {
        this(queueingTimeoutMs, maxCountPerStat, 1000);
    }

    public ThrottlingController(int queueingTimeoutMs, double maxCountPerStat, int statDurationMs) {
        AssertUtil.assertTrue(statDurationMs > 0, "statDurationMs should be positive");
        AssertUtil.assertTrue(maxCountPerStat >= 0, "maxCountPerStat should be >= 0");
        AssertUtil.assertTrue(queueingTimeoutMs >= 0, "queueingTimeoutMs should be >= 0");
        this.maxQueueingTimeMs = queueingTimeoutMs;
        this.count = maxCountPerStat;
        this.statDurationMs = statDurationMs;
        // Use nanoSeconds when durationMs%count != 0 or count/durationMs> 1 (to be accurate)
        if (maxCountPerStat > 0) {
            this.useNanoSeconds = statDurationMs % Math.round(maxCountPerStat) != 0 || maxCountPerStat / statDurationMs > 1;
        } else {
            this.useNanoSeconds = false;
        }
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    private boolean checkPassUsingNanoSeconds(int acquireCount, double maxCountPerStat) {
        final long maxQueueingTimeNs = maxQueueingTimeMs * MS_TO_NS_OFFSET;
        long currentTime = System.nanoTime();
        // Calculate the interval between every two requests.
        final long costTimeNs = Math.round(1.0d * MS_TO_NS_OFFSET * statDurationMs * acquireCount / maxCountPerStat);

        // Expected pass time of this request.
        long expectedTime = costTimeNs + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            latestPassedTime.set(currentTime);
            return true;
        } else {
            final long curNanos = System.nanoTime();
            // Calculate the time to wait.
            long waitTime = costTimeNs + latestPassedTime.get() - curNanos;
            if (waitTime > maxQueueingTimeNs) {
                return false;
            }

            long oldTime = latestPassedTime.addAndGet(costTimeNs);
            waitTime = oldTime - curNanos;
            if (waitTime > maxQueueingTimeNs) {
                latestPassedTime.addAndGet(-costTimeNs);
                return false;
            }
            // in race condition waitTime may <= 0
            if (waitTime > 0) {
                sleepNanos(waitTime);
            }
            return true;
        }
    }

    private boolean checkPassUsingCachedMs(int acquireCount, double maxCountPerStat) {
        long currentTime = TimeUtil.currentTimeMillis();
        // Calculate the interval between every two requests.
        long costTime = Math.round(1.0d * statDurationMs * acquireCount / maxCountPerStat);

        // Expected pass time of this request.
        long expectedTime = costTime + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            // Contention may exist here, but it's okay.
            latestPassedTime.set(currentTime);
            return true;
        } else {
            // Calculate the time to wait.
            long waitTime = costTime + latestPassedTime.get() - TimeUtil.currentTimeMillis();
            if (waitTime > maxQueueingTimeMs) {
                return false;
            }

            long oldTime = latestPassedTime.addAndGet(costTime);
            waitTime = oldTime - TimeUtil.currentTimeMillis();
            if (waitTime > maxQueueingTimeMs) {
                latestPassedTime.addAndGet(-costTime);
                return false;
            }
            // in race condition waitTime may <= 0
            if (waitTime > 0) {
                sleepMs(waitTime);
            }
            return true;
        }
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {
        // Pass when acquire count is less or equal than 0.
        if (acquireCount <= 0) {
            return true;
        }
        // Reject when count is less or equal than 0.
        // Otherwise, the costTime will be max of long and waitTime will overflow in some cases.
        if (count <= 0) {
            return false;
        }
        if (useNanoSeconds) {
            return checkPassUsingNanoSeconds(acquireCount, this.count);
        } else {
            return checkPassUsingCachedMs(acquireCount, this.count);
        }
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private void sleepNanos(long ns) {
        LockSupport.parkNanos(ns);
    }

}
