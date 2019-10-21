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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * QPS statistics interval.
 *
 * @author youji.zj
 * @author jialiang.linjl
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class IntervalProperty {

    /**
     * <p>Interval in milliseconds. This variable determines sensitivity of the QPS calculation.</p>
     * <p>
     * DO NOT MODIFY this value directly, use {@link #updateInterval(int)}, otherwise the modification will not
     * take effect.
     * </p>
     */
    public static volatile int INTERVAL = RuleConstant.DEFAULT_WINDOW_INTERVAL_MS;

    public static void register2Property(SentinelProperty<Integer> property) {
        property.addListener(new SimplePropertyListener<Integer>() {
            @Override
            public void configUpdate(Integer value) {
                if (value != null) {
                    updateInterval(value);
                }
            }
        });
    }

    /**
     * Update the {@link #INTERVAL}, All {@link ClusterNode}s will be reset if newInterval is
     * different from {@link #INTERVAL}
     *
     * @param newInterval New interval to set.
     */
    public static void updateInterval(int newInterval) {
        if (newInterval != INTERVAL) {
            INTERVAL = newInterval;
            ClusterBuilderSlot.resetClusterNodes();
        }
        RecordLog.info("[IntervalProperty] INTERVAL updated to: " + INTERVAL);
    }

}
