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

import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @param <T> type of data wrapper
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public abstract class LeapArray<T> {

    protected int windowLength;
    protected int sampleCount;
    protected int intervalInMs;

    protected AtomicReferenceArray<WindowWrap<T>> array;

    public LeapArray(int windowLength, int intervalInSec) {
        this.windowLength = windowLength;
        this.sampleCount = intervalInSec * 1000 / windowLength;
        this.intervalInMs = intervalInSec * 1000;

        this.array = new AtomicReferenceArray<WindowWrap<T>>(sampleCount);
    }

    public WindowWrap<T> currentWindow() {
        return currentWindow(TimeUtil.currentTimeMillis());
    }

    /**
     * Get window at provided timestamp.
     *
     * @param time a valid timestamp
     * @return the window at provided timestamp
     */
    abstract public WindowWrap<T> currentWindow(long time);

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

    AtomicReferenceArray<WindowWrap<T>> array() {
        return array;
    }

    private boolean isWindowDeprecated(WindowWrap<T> windowWrap) {
        return TimeUtil.currentTimeMillis() - windowWrap.windowStart() >= intervalInMs;
    }

    public List<WindowWrap<T>> list() {
        ArrayList<WindowWrap<T>> result = new ArrayList<WindowWrap<T>>();

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
        ArrayList<T> result = new ArrayList<T>();

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
