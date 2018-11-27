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
package com.alibaba.csp.sentinel.cluster.flow.statistic.metric;

import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterFlowEvent;
import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterMetricBucket;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.LongAdder;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterMetricLeapArray extends LeapArray<ClusterMetricBucket> {

    private final LongAdder[] occupyCounter;
    private boolean hasOccupied = false;

    /**
     * The total bucket count is: {@link #sampleCount} = intervalInSec * 1000 / windowLengthInMs.
     *
     * @param windowLengthInMs a single window bucket's time length in milliseconds.
     * @param intervalInSec    the total time span of this {@link LeapArray} in seconds.
     */
    public ClusterMetricLeapArray(int windowLengthInMs, int intervalInSec) {
        super(windowLengthInMs, intervalInSec);
        ClusterFlowEvent[] events = ClusterFlowEvent.values();
        this.occupyCounter = new LongAdder[events.length];
        for (ClusterFlowEvent event : events) {
            occupyCounter[event.ordinal()] = new LongAdder();
        }
    }

    @Override
    public ClusterMetricBucket newEmptyBucket() {
        return new ClusterMetricBucket();
    }

    @Override
    protected WindowWrap<ClusterMetricBucket> resetWindowTo(WindowWrap<ClusterMetricBucket> w, long startTime) {
        w.resetTo(startTime);
        w.value().reset();
        transferOccupyToBucket(w.value());
        return w;
    }

    private void transferOccupyToBucket(/*@Valid*/ ClusterMetricBucket bucket) {
        if (hasOccupied) {
            transferOccupiedCount(bucket, ClusterFlowEvent.PASS, ClusterFlowEvent.OCCUPIED_PASS);
            transferOccupiedThenReset(bucket, ClusterFlowEvent.PASS);
            transferOccupiedThenReset(bucket, ClusterFlowEvent.PASS_REQUEST);
            hasOccupied = false;
        }
    }

    private void transferOccupiedCount(ClusterMetricBucket bucket, ClusterFlowEvent source, ClusterFlowEvent target) {
        bucket.add(target, occupyCounter[source.ordinal()].sum());
    }

    private void transferOccupiedThenReset(ClusterMetricBucket bucket, ClusterFlowEvent event) {
        bucket.add(event, occupyCounter[event.ordinal()].sumThenReset());
    }

    public void addOccupyPass(int count) {
        occupyCounter[ClusterFlowEvent.PASS.ordinal()].add(count);
        occupyCounter[ClusterFlowEvent.PASS_REQUEST.ordinal()].add(1);
        this.hasOccupied = true;
    }

    public long getOccupiedCount(ClusterFlowEvent event) {
        return occupyCounter[event.ordinal()].sum();
    }
}
