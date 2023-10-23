/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.flow.tokenbucket;

import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author LearningGp
 */
public class StrictTokenBucket extends AbstractTokenBucket{

    final private Object refreshLock = new Object();
    final private Object consumeLock = new Object();

    public StrictTokenBucket(long unitProduceNum, long maxTokenNum, long intervalInMs) {
        super(unitProduceNum, maxTokenNum, false, intervalInMs);
    }

    public StrictTokenBucket(long unitProduceNum, long maxTokenNum, boolean fullStart, long intervalInMs) {
        super(unitProduceNum, maxTokenNum, fullStart, intervalInMs);
    }

    @Override
    public boolean tryConsume(long tokenNum) {
        if (tokenNum > maxTokenNum) {
            return false;
        }
        long currentTimestamp = TimeUtil.currentTimeMillis();
        refreshCurrentTokenNum(currentTimestamp);
        if (tokenNum <= currentTokenNum) {
            synchronized (consumeLock) {
                if (tokenNum <= currentTokenNum) {
                    currentTokenNum -= tokenNum;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void refreshCurrentTokenNum(long currentTimestamp) {
        if (nextProduceTime > currentTimestamp) {
            return;
        }
        long producedTokenNum = calProducedTokenNum(currentTimestamp);
        synchronized (refreshLock) {
            if (nextProduceTime > currentTimestamp) {
                return;
            }
            currentTokenNum = Math.min(maxTokenNum, currentTokenNum + producedTokenNum);
            updateNextProduceTime(currentTimestamp);
        }
    }

}
