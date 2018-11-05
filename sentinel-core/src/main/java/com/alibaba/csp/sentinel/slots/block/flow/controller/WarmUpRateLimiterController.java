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

import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author jialiang.linjl
 */
public class WarmUpRateLimiterController extends WarmUpController {

    final int timeOutInMs;
    final AtomicLong latestPassedTime = new AtomicLong(-1);

    /**
     * @param count
     * @param warmUpPeriodSec
     */
    public WarmUpRateLimiterController(double count, int warmUpPeriodSec, int timeOutMs, int coldFactor) {
        super(count, warmUpPeriodSec, coldFactor);
        this.timeOutInMs = timeOutMs;
    }

    @Override
    public boolean canPass(Node node, int acquireCount) {
        long previousQps = node.previousPassQps();
        syncToken(previousQps);

        long currentTime = TimeUtil.currentTimeMillis();

        long restToken = storedTokens.get();
        long costTime = 0;
        long expectedTime = 0;
        if (restToken >= warningToken) {
            long aboveToken = restToken - warningToken;

            // current interval = restToken*slope+1/count
            double warmingQps = Math.nextUp(1.0 / (aboveToken * slope + 1.0 / count));
            costTime = Math.round(1.0 * (acquireCount) / warmingQps * 1000);
        } else {
            costTime = Math.round(1.0 * (acquireCount) / count * 1000);
        }
        expectedTime = costTime + latestPassedTime.get();

        if (expectedTime <= currentTime) {
            latestPassedTime.set(currentTime);
            return true;
        } else {
            long waitTime = costTime + latestPassedTime.get() - currentTime;
            if (waitTime >= timeOutInMs) {
                return false;
            } else {
                long oldTime = latestPassedTime.addAndGet(costTime);
                try {
                    waitTime = oldTime - TimeUtil.currentTimeMillis();
                    if (waitTime >= timeOutInMs) {
                        latestPassedTime.addAndGet(-costTime);
                        return false;
                    }
                    Thread.sleep(waitTime);
                    return true;
                } catch (InterruptedException e) {
                }
            }
        }
        return false;
    }
}

