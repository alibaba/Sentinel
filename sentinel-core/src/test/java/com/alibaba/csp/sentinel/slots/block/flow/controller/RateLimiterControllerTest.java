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
import com.alibaba.csp.sentinel.util.TimeUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author jialiang.linjl
 */
public class RateLimiterControllerTest {

    @Test
    public void testPaceController_normal() throws InterruptedException {
        RateLimiterController paceController = new RateLimiterController(500, 10d);
        Node node = mock(Node.class);

        long start = TimeUtil.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            assertTrue(paceController.canPass(node, 1));
        }
        long end = TimeUtil.currentTimeMillis();
        assertTrue((end - start) > 400);
    }

    @Test
    public void testPaceController_timeout() throws InterruptedException {
        final RateLimiterController limiterController = new RateLimiterController(1000, 160d);
        final Node node = mock(Node.class);

        final AtomicInteger passcount = new AtomicInteger();
        final AtomicInteger blockcount = new AtomicInteger();

        final AtomicInteger done = new AtomicInteger();
        long lastSec = System.currentTimeMillis() / 1000;
        for (int i = 0; i < 1000; i++) {
            boolean pass = limiterController.canPass(node, 1);

            if (pass == true) {
                passcount.incrementAndGet();
            } else {
                blockcount.incrementAndGet();
            }
            done.incrementAndGet();
            long now = System.currentTimeMillis() / 1000;
            if (lastSec != now) {
                System.out.println("pass:" + passcount.get() + ", tm:" + lastSec);
                System.out.println("block" + blockcount.get() + ", tm:" + lastSec);
                System.out.println("done" + done.get() + ", tm:" + lastSec);
                passcount.set(0);
                blockcount.set(0);
                done.set(0);
            }
            lastSec = now;
        }
        //        countDown.await();
        System.out.println("pass:" + passcount.get() + ", tm:" + lastSec);
        System.out.println("block" + blockcount.get() + ", tm:" + lastSec);
        System.out.println("done" + done.get() + ", tm:" + lastSec);

    }

    @Test
    public void testPaceController_multiThread() throws InterruptedException {
        final RateLimiterController limiterController = new RateLimiterController(1000, 160d);
        final Node node = mock(Node.class);

        final AtomicInteger passcount = new AtomicInteger();
        final AtomicInteger blockcount = new AtomicInteger();

        final AtomicInteger done = new AtomicInteger();
        final AtomicLong lastTm = new AtomicLong(System.currentTimeMillis() / 1000);
        int count = 1000;
        final CountDownLatch countDown = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        boolean pass = limiterController.canPass(node, 1);
                        if (pass) {
                            passcount.incrementAndGet();
                        } else {
                            blockcount.incrementAndGet();
                        }
                        done.incrementAndGet();
                        long now = System.currentTimeMillis() / 1000;
                        if (lastTm.get() != now) {
                            System.out.println("pass:" + passcount.get() + ", tm:" + lastTm.get());
                            System.out.println("block:" + blockcount.get() + ", tm:" + lastTm.get());
                            System.out.println("done:" + done.get() + ", tm:" + lastTm.get());
                            passcount.set(0);
                            blockcount.set(0);
                            done.set(0);
                        }
                        lastTm.set(now);
                    }
                    countDown.countDown();
                }
            }, "Thread " + i);
            thread.start();
        }
        countDown.await();
        System.out.println("pass:" + passcount.get() + ", tm:" + lastTm.get());
        System.out.println("block:" + blockcount.get() + ", tm:" + lastTm.get());
        System.out.println("done:" + done.get() + ", tm:" + lastTm.get());

    }
}
