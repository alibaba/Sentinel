package com.alibaba.csp.sentinel.slots.statistic.metric;

import com.alibaba.csp.sentinel.slots.statistic.metric.occupy.FutureBucketLeapArray;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link FutureBucketLeapArray}.
 *
 * @author jialiang.linjl
 */
public class FutureBucketLeapArrayTest {

    private final int windowLengthInMs = 200;
    private final int intervalInSec = 2;
    private final int intervalInMs = intervalInSec * 1000;
    private final int sampleCount = intervalInMs / windowLengthInMs;

    @Test
    public void testFutureMetricLeapArray() {
        FutureBucketLeapArray array = new FutureBucketLeapArray(sampleCount, intervalInMs);

        long currentTime = TimeUtil.currentTimeMillis();
        for (int i = 0; i < intervalInSec * 1000; i = i + windowLengthInMs) {
            array.currentWindow(i + currentTime).value().addPass(1);
            assertEquals(array.values(i + currentTime).size(), 0);
        }
    }
}
