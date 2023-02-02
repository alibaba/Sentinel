/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Eric Zhao
 * @author jialiang.linjl
 */
public class ThrottlingControllerTest {

    @Test
    public void testThrottlingControllerNormal() throws InterruptedException {
        ThrottlingController paceController = new ThrottlingController(500, 10d);
        Node node = mock(Node.class);

        long start = TimeUtil.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            assertTrue(paceController.canPass(node, 1));
        }
        long end = TimeUtil.currentTimeMillis();
        assertTrue((end - start) > 400);
    }

    @Test
    public void testThrottlingControllerQueueTimeout() throws InterruptedException {
        final ThrottlingController paceController = new ThrottlingController(500, 10d);
        final Node node = mock(Node.class);

        final AtomicInteger passCount = new AtomicInteger();
        final AtomicInteger blockCount = new AtomicInteger();
        final CountDownLatch countDown = new CountDownLatch(1);

        final AtomicInteger done = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean pass = paceController.canPass(node, 1);

                    if (pass) {
                        passCount.incrementAndGet();
                    } else {
                        blockCount.incrementAndGet();
                    }
                    done.incrementAndGet();

                    if (done.get() >= 10) {
                        countDown.countDown();
                    }
                }

            }, "Thread-TestThrottlingControllerQueueTimeout-" + i);
            thread.start();
        }
        countDown.await();

        System.out.println("pass: " + passCount.get());
        System.out.println("block: " + blockCount.get());
        System.out.println("done: " + done.get());
        assertTrue(blockCount.get() > 0);
    }

    @Test
    public void testThrottlingControllerZeroThreshold() throws InterruptedException {
        ThrottlingController paceController = new ThrottlingController(500, 0d);
        Node node = mock(Node.class);

        for (int i = 0; i < 2; i++) {
            assertFalse(paceController.canPass(node, 1));
            assertTrue(paceController.canPass(node, 0));
        }
    }
}
