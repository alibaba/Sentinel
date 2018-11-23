/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.statistic.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * <p>
 * Basic data structure for statistic metrics.
 * </p>
 * <p>
 * Using sliding window algorithm to count data. Each bucket cover {@link #windowLengthInMs} time span,
 * and the total time span is {@link #intervalInMs}, so the total bucket count is:
 * {@link #sampleCount} = intervalInMs / windowLengthInMs.
 * </p>
 *
 * @param <T> type of statistic data
 * @author jialiang.linjl
 * @author Eric Zhao
 * @author Carpenter Lee
 */
public abstract class LeapArray<T> {

    protected int windowLengthInMs;
    protected int sampleCount;
    protected int intervalInMs;

    protected final AtomicReferenceArray<WindowWrap<T>> array;

    /**
     * The fine-grained update lock is used only when current bucket is deprecated.
     */
    private final ReentrantLock updateLock = new ReentrantLock();

    /**
     * The total bucket count is: {@code sampleCount = intervalInMs / windowLengthInMs}.
     *
     * @param windowLengthInMs a single window bucket's time length in milliseconds.
     * @param intervalInSec    the total time span of this {@link LeapArray} in seconds.
     */
    public LeapArray(int windowLengthInMs, int intervalInSec) {
        AssertUtil.isTrue(windowLengthInMs > 0, "bucket length is invalid: " + windowLengthInMs);
        int intervalInMs = intervalInSec * 1000;
        AssertUtil.isTrue(intervalInSec * 1000 > windowLengthInMs,
            "total time span of the window should be greater than bucket length");
        AssertUtil.isTrue(intervalInMs % windowLengthInMs == 0, "time span needs to be evenly divided");

        this.windowLengthInMs = windowLengthInMs;
        this.intervalInMs = intervalInMs;
        this.sampleCount = intervalInMs / windowLengthInMs;

        this.array = new AtomicReferenceArray<WindowWrap<T>>(sampleCount);
    }

    /**
     * Get the bucket at current timestamp.
     *
     * @return the bucket at current timestamp
     */
    public WindowWrap<T> currentWindow() {
        return currentWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Create a new statistic value for bucket.
     *
     * @return the new empty bucket
     */
    public abstract T newEmptyBucket();

    /**
     * Reset given bucket to provided start time and reset the value.
     *
     * @param startTime  the start time of the bucket
     * @param windowWrap current bucket
     * @return new clean bucket at given start time
     */
    protected abstract WindowWrap<T> resetWindowTo(WindowWrap<T> windowWrap, long startTime);

    /**
     * Get bucket item at provided timestamp.
     *
     * @param time a valid timestamp
     * @return current bucket item at provided timestamp if the time is valid; null if time is invalid
     */
    public WindowWrap<T> currentWindow(long time) {
        if (time < 0) {
            return null;
        }
        long timeId = time / windowLengthInMs;
        // Calculate current index so we can map the timestamp to the leap array.
        int idx = (int)(timeId % array.length());

        // Calculate current bucket start time.
        long windowStart = time - time % windowLengthInMs;

        /*
         * Get bucket item at given time from the array.
         *
         * (1) Bucket is absent, then just create a new bucket and CAS update to circular array.
         * (2) Bucket is up-to-date, then just return the bucket.
         * (3) Bucket is deprecated, then reset current bucket and clean all deprecated buckets.
         */
        while (true) {
            WindowWrap<T> old = array.get(idx);
            if (old == null) {
                /*
                 *     B0       B1      B2    NULL      B4
                 * ||_______|_______|_______|_______|_______||___
                 * 200     400     600     800     1000    1200  timestamp
                 *                             ^
                 *                          time=888
                 *            bucket is empty, so create new and update
                 *
                 * If the old bucket is absent, then we create a new bucket at {@code windowStart},
                 * then try to update circular array via a CAS operation. Only one thread can
                 * succeed to update, while other threads yield its time slice.
                 */
                WindowWrap<T> window = new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket());
                if (array.compareAndSet(idx, null, window)) {
                    // Successfully updated, return the created bucket.
                    return window;
                } else {
                    // Contention failed, the thread will yield its time slice to wait for bucket available.
                    Thread.yield();
                }
            } else if (windowStart == old.windowStart()) {
                /*
                 *     B0       B1      B2     B3      B4
                 * ||_______|_______|_______|_______|_______||___
                 * 200     400     600     800     1000    1200  timestamp
                 *                             ^
                 *                          time=888
                 *            startTime of Bucket 3: 800, so it's up-to-date
                 *
                 * If current {@code windowStart} is equal to the start timestamp of old bucket,
                 * that means the time is within the bucket, so directly return the bucket.
                 */
                return old;
            } else if (windowStart > old.windowStart()) {
                /*
                 *   (old)
                 *             B0       B1      B2    NULL      B4
                 * |_______||_______|_______|_______|_______|_______||___
                 * ...    1200     1400    1600    1800    2000    2200  timestamp
                 *                              ^
                 *                           time=1676
                 *          startTime of Bucket 2: 400, deprecated, should be reset
                 *
                 * If the start timestamp of old bucket is behind provided time, that means
                 * the bucket is deprecated. We have to reset the bucket to current {@code windowStart}.
                 * Note that the reset and clean-up operations are hard to be atomic,
                 * so we need a update lock to guarantee the correctness of bucket update.
                 *
                 * The update lock is fine-grained and will take effect only when
                 * bucket is deprecated, so in most cases it won't lead to performance loss.
                 */
                if (updateLock.tryLock()) {
                    try {
                        // Successfully get the update lock, now we reset the bucket.
                        return resetWindowTo(old, windowStart);
                    } finally {
                        updateLock.unlock();
                    }
                } else {
                    // Contention failed, the thread will yield its time slice to wait for bucket available.
                    Thread.yield();
                }
            } else if (windowStart < old.windowStart()) {
                // Should not go through here, as the provided time is already behind.
                return new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket());
            }
        }
    }

    /**
     * Get the previous bucket item before provided timestamp.
     *
     * @param time a valid timestamp
     * @return the previous bucket item before provided timestamp
     */
    public WindowWrap<T> getPreviousWindow(long time) {
        if (time < 0) {
            return null;
        }
        long timeId = (time - windowLengthInMs) / windowLengthInMs;
        int idx = (int)(timeId % array.length());
        time = time - windowLengthInMs;
        WindowWrap<T> wrap = array.get(idx);

        if (wrap == null || isWindowDeprecated(wrap)) {
            return null;
        }

        if (wrap.windowStart() + windowLengthInMs < (time)) {
            return null;
        }

        return wrap;
    }

    /**
     * Get the previous bucket item for current timestamp.
     *
     * @return the previous bucket item for current timestamp
     */
    public WindowWrap<T> getPreviousWindow() {
        return getPreviousWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Get statistic value from bucket for provided timestamp.
     *
     * @param time a valid timestamp
     * @return the statistic value if bucket for provided timestamp is up-to-date; otherwise null
     */
    public T getWindowValue(long time) {
        if (time < 0) {
            return null;
        }
        long timeId = time / windowLengthInMs;
        int idx = (int)(timeId % array.length());

        WindowWrap<T> old = array.get(idx);
        if (old == null || isWindowDeprecated(old)) {
            return null;
        }

        return old.value();
    }

    /**
     * Check if a bucket is deprecated, which means that the bucket
     * has been behind for at least an entire window time span.
     *
     * @param windowWrap a non-null bucket
     * @return true if the bucket is deprecated; otherwise false
     */
    private boolean isWindowDeprecated(/*@NonNull*/ WindowWrap<T> windowWrap) {
        return TimeUtil.currentTimeMillis() - windowWrap.windowStart() >= intervalInMs;
    }

    /**
     * Get valid bucket list for entire sliding window.
     * The list will only contain "valid" buckets.
     *
     * @return valid bucket list for entire sliding window.
     */
    public List<WindowWrap<T>> list() {
        List<WindowWrap<T>> result = new ArrayList<WindowWrap<T>>();

        for (int i = 0; i < array.length(); i++) {
            WindowWrap<T> windowWrap = array.get(i);
            if (windowWrap == null || isWindowDeprecated(windowWrap)) {
                continue;
            }
            result.add(windowWrap);
        }

        return result;
    }

    /**
     * Get aggregated value list for entire sliding window.
     * The list will only contain value from "valid" buckets.
     *
     * @return aggregated value list for entire sliding window
     */
    public List<T> values() {
        List<T> result = new ArrayList<T>();

        for (int i = 0; i < array.length(); i++) {
            WindowWrap<T> windowWrap = array.get(i);
            if (windowWrap == null || isWindowDeprecated(windowWrap)) {
                continue;
            }
            result.add(windowWrap.value());
        }
        return result;
    }

    /**
     * Get sample count (total amount of buckets).
     *
     * @return sample count
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Get total interval length of the sliding window.
     *
     * @return interval in second
     */
    public int getIntervalInSecond() {
        return intervalInMs / 1000;
    }
}
