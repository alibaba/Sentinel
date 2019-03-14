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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BucketLeapArray}.
 *
 * @author Eric Zhao
 */
public class BucketLeapArrayTest {

    private final int windowLengthInMs = 1000;
    private final int intervalInSec = 2;
    private final int intervalInMs = intervalInSec * 1000;
    private final int sampleCount = intervalInMs / windowLengthInMs;

    @Test
    public void testNewWindow() {
        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long time = TimeUtil.currentTimeMillis();
        WindowWrap<MetricBucket> window = leapArray.currentWindow(time);

        assertEquals(window.windowLength(), windowLengthInMs);
        assertEquals(window.windowStart(), time - time % windowLengthInMs);
        assertNotNull(window.value());
        assertEquals(0L, window.value().pass());
    }

    @Test
    public void testLeapArrayWindowStart() {
        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long firstTime = TimeUtil.currentTimeMillis();
        long previousWindowStart = firstTime - firstTime % windowLengthInMs;

        WindowWrap<MetricBucket> window = leapArray.currentWindow(firstTime);

        assertEquals(windowLengthInMs, window.windowLength());
        assertEquals(previousWindowStart, window.windowStart());
    }

    @Test
    public void testWindowAfterOneInterval() {
        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long firstTime = TimeUtil.currentTimeMillis();
        long previousWindowStart = firstTime - firstTime % windowLengthInMs;
        WindowWrap<MetricBucket> window = leapArray.currentWindow(previousWindowStart);

        assertEquals(windowLengthInMs, window.windowLength());
        assertEquals(previousWindowStart, window.windowStart());

        MetricBucket currentWindow = window.value();
        assertNotNull(currentWindow);

        currentWindow.addPass(1);
        currentWindow.addBlock(1);
        assertEquals(1L, currentWindow.pass());
        assertEquals(1L, currentWindow.block());

        long middleTime = previousWindowStart + windowLengthInMs / 2;

        window = leapArray.currentWindow(middleTime);
        assertEquals(previousWindowStart, window.windowStart());

        MetricBucket middleWindow = window.value();
        middleWindow.addPass(1);
        assertSame(currentWindow, middleWindow);
        assertEquals(2L, middleWindow.pass());
        assertEquals(1L, middleWindow.block());

        long nextTime = middleTime + windowLengthInMs / 2;
        window = leapArray.currentWindow(nextTime);
        assertEquals(windowLengthInMs, window.windowLength());
        assertEquals(windowLengthInMs, window.windowStart() - previousWindowStart);

        currentWindow = window.value();
        assertNotNull(currentWindow);
        assertEquals(0L, currentWindow.pass());
        assertEquals(0L, currentWindow.block());
    }

    @Deprecated
    public void testWindowDeprecatedRefresh() {
        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        final int len = sampleCount;
        long firstTime = TimeUtil.currentTimeMillis();
        List<WindowWrap<MetricBucket>> firstIterWindowList = new ArrayList<WindowWrap<MetricBucket>>(len);
        for (int i = 0; i < len; i++) {
            WindowWrap<MetricBucket> w = leapArray.currentWindow(firstTime + windowLengthInMs * i);
            w.value().addPass(1);
            firstIterWindowList.add(i, w);
        }

        for (int i = len; i < len * 2; i++) {
            WindowWrap<MetricBucket> w = leapArray.currentWindow(firstTime + windowLengthInMs * i);
            assertNotSame(w, firstIterWindowList.get(i - len));
        }
    }

    @Test
    public void testMultiThreadUpdateEmptyWindow() throws Exception {
        final long time = TimeUtil.currentTimeMillis();
        final int nThreads = 16;
        final BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        final CountDownLatch latch = new CountDownLatch(nThreads);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                leapArray.currentWindow(time).value().addPass(1);
                latch.countDown();
            }
        };

        for (int i = 0; i < nThreads; i++) {
            new Thread(task).start();
        }

        latch.await();

        assertEquals(nThreads, leapArray.currentWindow(time).value().pass());
    }

    @Test
    public void testGetPreviousWindow() {
        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long time = TimeUtil.currentTimeMillis();
        WindowWrap<MetricBucket> previousWindow = leapArray.currentWindow(time);
        assertNull(leapArray.getPreviousWindow(time));

        long nextTime = time + windowLengthInMs;
        assertSame(previousWindow, leapArray.getPreviousWindow(nextTime));

        long longTime = time + 11 * windowLengthInMs;
        assertNull(leapArray.getPreviousWindow(longTime));
    }

    @Test
    public void testListWindowsResetOld() throws Exception {
        final int windowLengthInMs = 100;
        final int intervalInMs = 1000;
        final int sampleCount = intervalInMs / windowLengthInMs;

        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long time = TimeUtil.currentTimeMillis();

        Set<WindowWrap<MetricBucket>> windowWraps = new HashSet<WindowWrap<MetricBucket>>();

        windowWraps.add(leapArray.currentWindow(time));
        windowWraps.add(leapArray.currentWindow(time + windowLengthInMs));

        List<WindowWrap<MetricBucket>> list = leapArray.list();
        for (WindowWrap<MetricBucket> wrap : list) {
            assertTrue(windowWraps.contains(wrap));
        }

        Thread.sleep(windowLengthInMs + intervalInMs);

        // This will replace the deprecated bucket, so all deprecated buckets will be reset.
        leapArray.currentWindow(time + windowLengthInMs + intervalInMs).value().addPass(1);

        assertEquals(1, leapArray.list().size());
    }

    @Test
    public void testListWindowsNewBucket() throws Exception {
        final int windowLengthInMs = 100;
        final int intervalInSec = 1;
        final int intervalInMs = intervalInSec * 1000;
        final int sampleCount = intervalInMs / windowLengthInMs;

        BucketLeapArray leapArray = new BucketLeapArray(sampleCount, intervalInMs);
        long time = TimeUtil.currentTimeMillis();

        Set<WindowWrap<MetricBucket>> windowWraps = new HashSet<WindowWrap<MetricBucket>>();

        windowWraps.add(leapArray.currentWindow(time));
        windowWraps.add(leapArray.currentWindow(time + windowLengthInMs));

        Thread.sleep(intervalInMs + windowLengthInMs * 3);

        List<WindowWrap<MetricBucket>> list = leapArray.list();
        for (WindowWrap<MetricBucket> wrap : list) {
            assertTrue(windowWraps.contains(wrap));
        }

        // This won't hit deprecated bucket, so no deprecated buckets will be reset.
        // But deprecated buckets can be filtered when collecting list.
        leapArray.currentWindow(TimeUtil.currentTimeMillis()).value().addPass(1);

        assertEquals(1, leapArray.list().size());
    }
}
