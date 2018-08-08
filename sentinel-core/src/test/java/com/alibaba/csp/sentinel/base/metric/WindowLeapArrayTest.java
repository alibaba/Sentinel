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
package com.alibaba.csp.sentinel.base.metric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.slots.statistic.base.Window;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.slots.statistic.metric.WindowLeapArray;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WindowLeapArray}.
 *
 * @author Eric Zhao
 */
public class WindowLeapArrayTest {

    private final int windowLengthInMs = 1000;
    private final int intervalInSec = 2;

    @Test
    public void testNewWindow() {
        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long time = TimeUtil.currentTimeMillis();
        WindowWrap<Window> window = leapArray.currentWindow(time);

        assertEquals(window.windowLength(), windowLengthInMs);
        assertEquals(window.windowStart(), time - time % windowLengthInMs);
        assertNotNull(window.value());
        assertEquals(0L, window.value().pass());
    }

    @Test
    public void testLeapArrayWindowStart() {
        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long firstTime = TimeUtil.currentTimeMillis();
        long previousWindowStart = firstTime - firstTime % windowLengthInMs;

        WindowWrap<Window> window = leapArray.currentWindow(firstTime);

        assertEquals(windowLengthInMs, window.windowLength());
        assertEquals(previousWindowStart, window.windowStart());
    }

    @Test
    public void testWindowAfterOneInterval() {
        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long firstTime = TimeUtil.currentTimeMillis();
        long previousWindowStart = firstTime - firstTime % windowLengthInMs;
        WindowWrap<Window> window = leapArray.currentWindow(previousWindowStart);

        assertEquals(windowLengthInMs, window.windowLength());
        assertEquals(previousWindowStart, window.windowStart());

        Window currentWindow = window.value();
        assertNotNull(currentWindow);

        currentWindow.addPass();
        currentWindow.addBlock();
        assertEquals(1L, currentWindow.pass());
        assertEquals(1L, currentWindow.block());

        long middleTime = previousWindowStart + windowLengthInMs / 2;

        window = leapArray.currentWindow(middleTime);
        assertEquals(previousWindowStart, window.windowStart());

        Window middleWindow = window.value();
        middleWindow.addPass();
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
        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        final int len = intervalInSec * 1000 / windowLengthInMs;
        long firstTime = TimeUtil.currentTimeMillis();
        List<WindowWrap<Window>> firstIterWindowList = new ArrayList<WindowWrap<Window>>(len);
        for (int i = 0; i < len; i++) {
            WindowWrap<Window> w = leapArray.currentWindow(firstTime + windowLengthInMs * i);
            w.value().addPass();
            firstIterWindowList.add(i, w);
        }

        for (int i = len; i < len * 2; i++) {
            WindowWrap<Window> w = leapArray.currentWindow(firstTime + windowLengthInMs * i);
            assertNotSame(w, firstIterWindowList.get(i - len));
        }
    }

    @Test
    public void testMultiThreadUpdateEmptyWindow() throws Exception {
        final long time = TimeUtil.currentTimeMillis();
        final int nThreads = 16;
        final WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        final CountDownLatch latch = new CountDownLatch(nThreads);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                leapArray.currentWindow(time).value().addPass();
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
        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long time = TimeUtil.currentTimeMillis();
        WindowWrap<Window> previousWindow = leapArray.currentWindow(time);
        assertNull(leapArray.getPreviousWindow(time));

        long nextTime = time + windowLengthInMs;
        assertSame(previousWindow, leapArray.getPreviousWindow(nextTime));

        long longTime = time + 11 * windowLengthInMs;
        assertNull(leapArray.getPreviousWindow(longTime));
    }

    @Test
    public void testListWindowsResetOld() throws Exception {
        final int windowLengthInMs = 100;
        final int intervalInSec = 1;
        final int intervalInMs = intervalInSec * 1000;

        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long time = TimeUtil.currentTimeMillis();

        Set<WindowWrap<Window>> windowWraps = new HashSet<WindowWrap<Window>>();

        windowWraps.add(leapArray.currentWindow(time));
        windowWraps.add(leapArray.currentWindow(time + windowLengthInMs));

        List<WindowWrap<Window>> list = leapArray.list();
        for (WindowWrap<Window> wrap : list) {
            assertTrue(windowWraps.contains(wrap));
        }

        Thread.sleep(windowLengthInMs + intervalInMs);

        // This will replace the deprecated bucket, so all deprecated buckets will be reset.
        leapArray.currentWindow(time + windowLengthInMs + intervalInMs).value().addPass();

        assertEquals(1, leapArray.list().size());
    }

    @Test
    public void testListWindowsNewBucket() throws Exception {
        final int windowLengthInMs = 100;
        final int intervalInSec = 1;

        WindowLeapArray leapArray = new WindowLeapArray(windowLengthInMs, intervalInSec);
        long time = TimeUtil.currentTimeMillis();

        Set<WindowWrap<Window>> windowWraps = new HashSet<WindowWrap<Window>>();

        windowWraps.add(leapArray.currentWindow(time));
        windowWraps.add(leapArray.currentWindow(time + windowLengthInMs));

        Thread.sleep(intervalInSec * 1000 + windowLengthInMs * 3);

        List<WindowWrap<Window>> list = leapArray.list();
        for (WindowWrap<Window> wrap : list) {
            assertTrue(windowWraps.contains(wrap));
        }

        // This won't hit deprecated bucket, so no deprecated buckets will be reset.
        // But deprecated buckets can be filtered when collecting list.
        leapArray.currentWindow(TimeUtil.currentTimeMillis()).value().addPass();

        assertEquals(1, leapArray.list().size());
    }
}