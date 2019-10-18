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
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;

/**
 * The fundamental data structure for metric statistics in a time span.
 * 时间跨度中度量统计的基本数据结构。
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @see LeapArray
 */
public class BucketLeapArray extends LeapArray<MetricBucket> {

    public BucketLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    /**
     * 创建一个新的桶
     * @param time
     * @return
     */
    @Override
    public MetricBucket newEmptyBucket(long time) {
        return new MetricBucket();
    }

    /**
     * 重设置窗口到{@link WindowWrap<MetricBucket>}
     * @param w
     * @param startTime  the start time of the bucket in milliseconds
     * @return
     */
    @Override
    protected WindowWrap<MetricBucket> resetWindowTo(WindowWrap<MetricBucket> w, long startTime) {
        // Update the start time and reset value.
        w.resetTo(startTime);
        w.value().reset();
        return w;
    }
}
