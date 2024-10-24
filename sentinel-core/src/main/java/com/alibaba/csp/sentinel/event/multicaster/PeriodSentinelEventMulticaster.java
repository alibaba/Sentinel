/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.multicaster;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;

import java.util.concurrent.locks.LockSupport;

/**
 * A broadcaster that triggers events periodically.
 *
 * @author Daydreamer-ia
 */
public class PeriodSentinelEventMulticaster extends DefaultSentinelEventMulticaster {

    /**
     * period.
     */
    private final long periodMs;

    public PeriodSentinelEventMulticaster(long periodMs) {
        this.periodMs = periodMs;
    }

    @Override
    public void run() {
        long periodNanos = periodMs * 1000 * 1000;
        while (isRunning()) {
            try {
                LockSupport.parkNanos(periodNanos);
                while (queue != null && !queue.isEmpty()) {
                    SentinelEvent poll = queue.poll();
                    if (poll == null) {
                        continue;
                    }
                    // handle event
                    receiveEvent(poll);
                    // increase sequence
                    increaseSequence(poll.getClass());
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
