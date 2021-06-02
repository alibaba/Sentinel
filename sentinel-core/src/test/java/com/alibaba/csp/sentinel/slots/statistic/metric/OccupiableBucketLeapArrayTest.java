package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.slots.statistic.metric.occupy.OccupiableBucketLeapArray;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link OccupiableBucketLeapArray}.
 *
 * @author jialiang.linjl
 */
public class OccupiableBucketLeapArrayTest extends AbstractTimeBasedTest {

    private final int windowLengthInMs = 200;
    private final int intervalInSec = 2;
    private final int intervalInMs = intervalInSec * 1000;
    private final int sampleCount = intervalInMs / windowLengthInMs;

    @Test
    public void testNewWindow() {
        long currentTime = System.currentTimeMillis();
        setCurrentMillis(currentTime);
        OccupiableBucketLeapArray leapArray = new OccupiableBucketLeapArray(sampleCount, intervalInMs);

        WindowWrap<MetricBucket> currentWindow = leapArray.currentWindow(currentTime);
        currentWindow.value().addPass(1);
        assertEquals(currentWindow.value().pass(), 1L);

        leapArray.addWaiting(currentTime + windowLengthInMs, 1);
        assertEquals(leapArray.currentWaiting(), 1);
        assertEquals(currentWindow.value().pass(), 1L);

    }

    @Test
    public void testWindowInOneInterval() {
        OccupiableBucketLeapArray leapArray = new OccupiableBucketLeapArray(sampleCount, intervalInMs);
        long currentTime = System.currentTimeMillis();
        setCurrentMillis(currentTime);

        WindowWrap<MetricBucket> currentWindow = leapArray.currentWindow(currentTime);
        currentWindow.value().addPass(1);
        assertEquals(currentWindow.value().pass(), 1L);

        leapArray.addWaiting(currentTime + windowLengthInMs, 2);
        assertEquals(leapArray.currentWaiting(), 2);
        assertEquals(currentWindow.value().pass(), 1L);

        leapArray.currentWindow(currentTime + windowLengthInMs);
        List<MetricBucket> values = leapArray.values(currentTime + windowLengthInMs);
        assertEquals(values.size(), 2);

        long sum = 0;
        for (MetricBucket bucket : values) {
            sum += bucket.pass();
        }
        assertEquals(sum, 3);
    }

    @Test
    public void testMultiThreadUpdateEmptyWindow() throws Exception {
        final long time = System.currentTimeMillis();
        setCurrentMillis(time);
        final int nThreads = 16;
        final OccupiableBucketLeapArray leapArray = new OccupiableBucketLeapArray(sampleCount, intervalInMs);
        final CountDownLatch latch = new CountDownLatch(nThreads);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                leapArray.currentWindow(time).value().addPass(1);
                leapArray.addWaiting(time + windowLengthInMs, 1);
                latch.countDown();
            }
        };

        for (int i = 0; i < nThreads; i++) {
            new Thread(task).start();
        }

        latch.await();

        assertEquals(nThreads, leapArray.currentWindow(time).value().pass());
        assertEquals(nThreads, leapArray.currentWaiting());

        leapArray.currentWindow(time + windowLengthInMs);
        long sum = 0;
        List<MetricBucket> values = leapArray.values(time + windowLengthInMs);
        for (MetricBucket bucket : values) {
            sum += bucket.pass();
        }
        assertEquals(values.size(), 2);
        assertEquals(sum, nThreads * 2);
    }

    @Test
    public void testWindowAfterOneInterval() {
        OccupiableBucketLeapArray leapArray = new OccupiableBucketLeapArray(sampleCount, intervalInMs);
        long currentTime = System.currentTimeMillis();
        setCurrentMillis(currentTime);

        System.out.println(currentTime);
        for (int i = 0; i < intervalInSec * 1000 / windowLengthInMs; i++) {
            WindowWrap<MetricBucket> currentWindow = leapArray.currentWindow(currentTime + i * windowLengthInMs);
            currentWindow.value().addPass(1);
            leapArray.addWaiting(currentTime + (i + 1) * windowLengthInMs, 1);
            System.out.println(currentTime + i * windowLengthInMs);
            leapArray.debug(currentTime + i * windowLengthInMs);
        }

        System.out.println(currentTime + intervalInSec * 1000);
        List<MetricBucket> values = leapArray
            .values(currentTime - currentTime % windowLengthInMs + intervalInSec * 1000);
        leapArray.debug(currentTime + intervalInSec * 1000);
        assertEquals(values.size(), intervalInSec * 1000 / windowLengthInMs);

        long sum = 0;
        for (MetricBucket bucket : values) {
            sum += bucket.pass();
        }
        assertEquals(sum, 2 * intervalInSec * 1000 / windowLengthInMs - 1);

        /**
         * https://github.com/alibaba/Sentinel/issues/685
         *
         * Here we could not use exactly current time, because the following result is related with the above elapse.
         * So we use the beginning current time to ensure.
         */
        assertEquals(leapArray.currentWaiting(), 10);
    }
}
