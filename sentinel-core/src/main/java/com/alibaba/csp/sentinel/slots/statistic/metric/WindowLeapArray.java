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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.Window;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

/**
 * The fundamental data structure for metric statistics in a time window.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class WindowLeapArray extends LeapArray<Window> {

    private final int timeLength;

    public WindowLeapArray(int windowLengthInMs, int intervalInSec) {
        super(windowLengthInMs, intervalInSec);
        timeLength = intervalInSec * 1000;
    }

    private ReentrantLock addLock = new ReentrantLock();

    @Override
    public WindowWrap<Window> currentWindow(long time) {

        long timeId = time / windowLength;
        // Calculate current index.
        int idx = (int)(timeId % array.length());

        // Cut the time to current window start.
        time = time - time % windowLength;

        while (true) {
            WindowWrap<Window> old = array.get(idx);
            if (old == null) {
                WindowWrap<Window> window = new WindowWrap<Window>(windowLength, time, new Window());
                if (array.compareAndSet(idx, null, window)) {
                    return window;
                } else {
                    Thread.yield();
                }
            } else if (time == old.windowStart()) {
                return old;
            } else if (time > old.windowStart()) {
                if (addLock.tryLock()) {
                    try {
                        WindowWrap<Window> window = new WindowWrap<Window>(windowLength, time, new Window());
                        if (array.compareAndSet(idx, old, window)) {
                            for (int i = 0; i < array.length(); i++) {
                                WindowWrap<Window> tmp = array.get(i);
                                if (tmp == null) {
                                    continue;
                                } else {
                                    if (tmp.windowStart() < time - timeLength) {
                                        array.set(i, null);
                                    }
                                }
                            }
                            return window;
                        }
                    } finally {
                        addLock.unlock();
                    }

                } else {
                    Thread.yield();
                }

            } else if (time < old.windowStart()) {
                return new WindowWrap<Window>(windowLength, time, new Window());
            }
        }
    }
}
