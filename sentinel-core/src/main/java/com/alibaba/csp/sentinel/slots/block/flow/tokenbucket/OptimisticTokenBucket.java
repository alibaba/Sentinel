package com.alibaba.csp.sentinel.slots.block.flow.tokenbucket;

import com.alibaba.csp.sentinel.util.TimeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author GU
 */
public class OptimisticTokenBucket extends AbstractTokenBucket {
    private static final long currentTokenNumOffset;

    private static final long nextProduceTimeOffset;

    private static final long tokenRefreshingOffset;

    private volatile int tokenRefreshing = 0;

    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            currentTokenNumOffset = unsafe.objectFieldOffset
                    (AbstractTokenBucket.class.getDeclaredField("currentTokenNum"));
            nextProduceTimeOffset = unsafe.objectFieldOffset
                    (AbstractTokenBucket.class.getDeclaredField("nextProduceTime"));
            tokenRefreshingOffset = unsafe.objectFieldOffset
                    (OptimisticTokenBucket.class.getDeclaredField("tokenRefreshing"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public OptimisticTokenBucket(long unitProduceNum, long maxTokenNum, long intervalInMs) {
        super(unitProduceNum, maxTokenNum, false, intervalInMs);
    }

    public OptimisticTokenBucket(long unitProduceNum, long maxTokenNum, boolean fullStart, long intervalInMs) {
        super(unitProduceNum, maxTokenNum, fullStart, intervalInMs);
    }

    @Override
    public boolean tryConsume(long tokenNum) {
        if (tokenNum > maxTokenNum) {
            return false;
        }
        long currentTimestamp = TimeUtil.currentTimeMillis();
        refreshCurrentTokenNum(currentTimestamp);
        long theCurrentTokenNum = currentTokenNum;
        while (true) {
            long leaveToken = theCurrentTokenNum - tokenNum;
            if (leaveToken >= 0) {
                if (compareAndSwapCurrentTokenNum(theCurrentTokenNum, leaveToken)) {
                    return true;
                }
            } else if (isTokenRefreshing()) {
                Thread.yield();
            } else {
                return false;
            }
            theCurrentTokenNum = currentTokenNum;
        }
    }

    @Override
    public void refreshCurrentTokenNum(long currentTimestamp) {
        long lastProduceTime = nextProduceTime;
        long theNextProduceTime = nextProduceTime(currentTimestamp);
        while (lastProduceTime <= theNextProduceTime) {
            if (compareAndSwapNextProduceTime(lastProduceTime, theNextProduceTime)) {
                tokenRefreshing();
                long producedTokenNum = calProducedTokenNum(lastProduceTime, currentTimestamp);
                long theTokenNum = currentTokenNum;
                while (!compareAndSwapCurrentTokenNum(theTokenNum, Math.min(maxTokenNum, theTokenNum + producedTokenNum))) {
                    theTokenNum = currentTokenNum;
                }
                tokenRefreshFinished();
                break;
            } else {
                lastProduceTime = nextProduceTime;
            }
        }
    }

    protected long calProducedTokenNum(long lastProduceTime, long currentTimestamp) {
        if (lastProduceTime > currentTimestamp) {
            return 0;
        }
        long nextRefreshUnitCount = (lastProduceTime - startTime) / intervalInMs;
        long currentUnitCount = (currentTimestamp - startTime) / intervalInMs;
        long unitCount = currentUnitCount - nextRefreshUnitCount + 1;
        return unitCount * unitProduceNum;
    }

    protected long nextProduceTime(long currentTimestamp) {
        return intervalInMs - ((currentTimestamp - startTime) % intervalInMs) + currentTimestamp;
    }

    private boolean compareAndSwapCurrentTokenNum(long expect, long update) {
        return unsafe.compareAndSwapLong(this, currentTokenNumOffset, expect, update);

    }

    private boolean compareAndSwapNextProduceTime(long expect, long update) {
        return unsafe.compareAndSwapLong(this, nextProduceTimeOffset, expect, update);
    }

    private boolean tokenRefreshing() {
        return unsafe.compareAndSwapInt(this, tokenRefreshingOffset, 0, 1);
    }

    private boolean tokenRefreshFinished() {
        return unsafe.compareAndSwapInt(this, tokenRefreshingOffset, 1, 0);
    }

    private boolean isTokenRefreshing() {
        return tokenRefreshing == 1;
    }
}
