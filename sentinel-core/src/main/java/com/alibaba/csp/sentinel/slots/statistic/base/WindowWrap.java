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

/**
 * Wrapper entity class for a period of time window.
 *
 * @param <T> data type
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class WindowWrap<T> {

    /**
     * The length of the window.
     */
    private final long windowLength;

    /**
     * Start time of the window in milliseconds.
     */
    private long windowStart;

    /**
     * Statistic value.
     */
    private T value;

    /**
     * @param windowLength the time length of the window
     * @param windowStart  the start timestamp of the window
     * @param value        window data
     */
    public WindowWrap(long windowLength, long windowStart, T value) {
        this.windowLength = windowLength;
        this.windowStart = windowStart;
        this.value = value;
    }

    public long windowLength() {
        return windowLength;
    }

    public long windowStart() {
        return windowStart;
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public WindowWrap<T> resetTo(long startTime) {
        this.windowStart = startTime;
        return this;
    }

    @Override
    public String toString() {
        return "WindowWrap{" +
            "windowLength=" + windowLength +
            ", windowStart=" + windowStart +
            ", value=" + value +
            '}';
    }
}
