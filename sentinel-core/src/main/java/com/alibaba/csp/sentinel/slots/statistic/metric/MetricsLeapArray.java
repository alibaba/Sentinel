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

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.MetricBucket;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

/**
 * The fundamental data structure for metric statistics in a time window.
 *
 * @see LeapArray
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class MetricsLeapArray extends LeapArray<MetricBucket> {

    /**
     * Constructor
     *
     * @param windowLengthInMs a single window bucket's time length in milliseconds.
     * @param intervalInSec    the total time span of this {@link MetricsLeapArray} in seconds.
     */
    public MetricsLeapArray(int windowLengthInMs, int intervalInSec) {
        super(windowLengthInMs, intervalInSec);
    }

    @Override
    public MetricBucket newEmptyBucket() {
        return new MetricBucket();
    }

    @Override
    protected WindowWrap<MetricBucket> resetWindowTo(WindowWrap<MetricBucket> w, long startTime) {
        w.resetTo(startTime);
        w.value().reset();
        return w;
    }
}
