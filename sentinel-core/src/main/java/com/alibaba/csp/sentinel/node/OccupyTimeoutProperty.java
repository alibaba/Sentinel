/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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

/**
 * @author jialiang.linjl
 * @author Carpenter Lee
 * @since 1.5.0
 */
public class OccupyTimeoutProperty {

    /**
     * <p>
     * Max occupy timeout in milliseconds. Requests with priority can occupy tokens of the future statistic
     * window, and {@code occupyTimeout} limit the max time length that can be occupied.
     * </p>
     * <p>
     * Note that the timeout value should never be greeter than {@link IntervalProperty#INTERVAL}.
     * </p>
     * DO NOT MODIFY this value directly, use {@link #updateTimeout(int)},
     * otherwise the modification will not take effect.
     */
    private static volatile int occupyTimeout = 500;

    public static void register2Property(SentinelProperty<Integer> property) {
        property.addListener(new SimplePropertyListener<Integer>() {
            @Override
            public void configUpdate(Integer value) {
                if (value != null) {
                    updateTimeout(value);
                }
            }
        });
    }

    public static int getOccupyTimeout() {
        return occupyTimeout;
    }

    /**
     * Update the timeout value.</br>
     * Note that the time out should never greeter than {@link IntervalProperty#INTERVAL},
     * or it will be ignored.
     *
     * @param newInterval new value.
     */
    public static void updateTimeout(int newInterval) {
        if (newInterval < 0) {
            RecordLog.warn("[OccupyTimeoutProperty] Illegal timeout value will be ignored: " + occupyTimeout);
            return;
        }
        if (newInterval > IntervalProperty.INTERVAL) {
            RecordLog.warn("[OccupyTimeoutProperty] Illegal timeout value will be ignored: {}, should <= {}",
                occupyTimeout, IntervalProperty.INTERVAL);
            return;
        }
        if (newInterval != occupyTimeout) {
            occupyTimeout = newInterval;
        }
        RecordLog.info("[OccupyTimeoutProperty] occupyTimeout updated to: {}", occupyTimeout);
    }
}
