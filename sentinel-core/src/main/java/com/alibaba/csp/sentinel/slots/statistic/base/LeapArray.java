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

import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Basic data structure for statistic metrics.
 *
 * @param <T> type of data wrapper
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public abstract class LeapArray<T> {

    protected int windowLength;
    protected int sampleCount;
    protected int intervalInMs;

    protected final AtomicReferenceArray<WindowWrap<T>> array;

    private final ReentrantLock updateLock = new ReentrantLock();

    public LeapArray(int windowLength, int intervalInSec) {
        this.windowLength = windowLength;
        this.intervalInMs = intervalInSec * 1000;
        this.sampleCount = intervalInMs / windowLength;

        this.array = new AtomicReferenceArray<WindowWrap<T>>(sampleCount);
    }

    /**
     * Get the window at current timestamp.
     *
     * @return the window at current timestamp
     */
    public WindowWrap<T> currentWindow() {
        return currentWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Create a new bucket.
     *
     * @return the new empty bucket
     */
    public abstract T newEmptyBucket();

    /**
     * Reset current window to provided start time and reset all counters.
     *
     * @param startTime  the start time of the window
     * @param windowWrap current window
     * @return new clean window wrap
     */
    protected abstract WindowWrap<T> resetWindowTo(WindowWrap<T> windowWrap, long startTime);

    /**
     * Get window at provided timestamp.
     *
     * @param time a valid timestamp
     * @return the window at provided timestamp
     */
    public WindowWrap<T> currentWindow(long time) {
        long timeId = time / windowLength;
        // Calculate current index.
        int idx = (int)(timeId % array.length());

        // Cut the time to current window start.
        time = time - time % windowLength;

        while (true) {
            WindowWrap<T> old = array.get(idx);
            if (old == null) {
                WindowWrap<T> window = new WindowWrap<T>(windowLength, time, newEmptyBucket());
                if (array.compareAndSet(idx, null, window)) {
                    return window;
                } else {
                    Thread.yield();
                }
            } else if (time == old.windowStart()) {
                return old;
            } else if (time > old.windowStart()) {
                if (updateLock.tryLock()) {
                    try {
                        // if (old is deprecated) then [LOCK] resetTo currentTime.
                        return resetWindowTo(old, time);
                    } finally {
                        updateLock.unlock();
                    }
                } else {
                    Thread.yield();
                }

            } else if (time < old.windowStart()) {
                // Cannot go through here.
                return new WindowWrap<T>(windowLength, time, newEmptyBucket());
            }
        }
    }

    public WindowWrap<T> getPreviousWindow(long time) {
        long timeId = (time - windowLength) / windowLength;
        int idx = (int)(timeId % array.length());
        time = time - windowLength;
        WindowWrap<T> wrap = array.get(idx);

        if (wrap == null || isWindowDeprecated(wrap)) {
            return null;
        }

        if (wrap.windowStart() + windowLength < (time)) {
            return null;
        }

        return wrap;
    }

    public WindowWrap<T> getPreviousWindow() {
        return getPreviousWindow(System.currentTimeMillis());
    }

    public T getWindowValue(long time) {
        long timeId = time / windowLength;
        int idx = (int)(timeId % array.length());

        WindowWrap<T> old = array.get(idx);
        if (old == null || isWindowDeprecated(old)) {
            return null;
        }

        return old.value();
    }

    private boolean isWindowDeprecated(WindowWrap<T> windowWrap) {
        return TimeUtil.currentTimeMillis() - windowWrap.windowStart() >= intervalInMs;
    }

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
}
