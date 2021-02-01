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
package com.alibaba.csp.sentinel.cluster.flow.statistic.data;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterMetricBucket {

    private final LongAdder[] counters;

    public ClusterMetricBucket() {
        ClusterFlowEvent[] events = ClusterFlowEvent.values();
        this.counters = new LongAdder[events.length];
        for (ClusterFlowEvent event : events) {
            counters[event.ordinal()] = new LongAdder();
        }
    }

    public void reset() {
        for (ClusterFlowEvent event : ClusterFlowEvent.values()) {
            counters[event.ordinal()].reset();
        }
    }

    public long get(ClusterFlowEvent event) {
        return counters[event.ordinal()].sum();
    }

    public ClusterMetricBucket add(ClusterFlowEvent event, long count) {
        counters[event.ordinal()].add(count);
        return this;
    }
}
