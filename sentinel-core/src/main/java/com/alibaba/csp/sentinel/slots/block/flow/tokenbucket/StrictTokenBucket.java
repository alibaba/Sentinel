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
