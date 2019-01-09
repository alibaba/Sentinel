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
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * @author jialiang.linjl
 */
public class RateLimiterController implements TrafficShapingController {

    private final long maxQueueingTimeNano;
    private final double count;

    private final AtomicLong latestPassedTime = new AtomicLong(-1);

    public RateLimiterController(long timeOut, double count) {
        this.maxQueueingTimeNano = 1000000 * timeOut;
        this.count = count;
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        return canPass(node, acquireCount, false);
    }

    @Override
    public boolean canPass(Node node, int acquireCount, boolean prioritized) {

        long currentTime = System.nanoTime();
        long costTime = Math.round(1e9 * (acquireCount) / count);
        long lastTime = latestPassedTime.get();
        long expectedTime = costTime + lastTime;
        long waitTime = expectedTime - currentTime;
        if (waitTime <= 0) {
            latestPassedTime.set(currentTime);
            return true;
        } else if (waitTime >= maxQueueingTimeNano) {
            return false;
        }
        while (!latestPassedTime.compareAndSet(lastTime, expectedTime)) {
            lastTime = latestPassedTime.get();
            expectedTime = costTime + lastTime;
            currentTime = System.nanoTime();
            waitTime = expectedTime - currentTime;
            if (waitTime >= maxQueueingTimeNano) {
                return false;
            }
        }

        LockSupport.parkNanos(waitTime);
        return true;
    }

}
