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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.node.Node;

/**
 * @author jialiang.linjl
 */
public class RateLimiterControllerTest {

    @Test
    public void testPaceController_normal1() throws InterruptedException {
        RateLimiterController paceController = new RateLimiterController(500, 10d);
        Node node = mock(Node.class);

        long start = TimeUtil.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            assertTrue(paceController.canPass(node, 1));
        }
        long end = TimeUtil.currentTimeMillis();
        assertTrue((end - start) > 9000);
        assertTrue((end - start) <= 11000);
    }
    
    @Test
    public void testPaceController_normal2() throws InterruptedException {
        RateLimiterController paceController = new RateLimiterController(500, 200d);
        Node node = mock(Node.class);

        long start = TimeUtil.currentTimeMillis();
        for (int i = 0; i < 5000; i++) {
            assertTrue(paceController.canPass(node, 1));
        }
        long end = TimeUtil.currentTimeMillis();
        assertTrue((end - start) > 24000);
        assertTrue((end - start) <= 26000);
    }

    @Test
    public void testPaceController_timeout1() throws InterruptedException {
        testPaceController_timeout0(15);
    }
    
    @Test
    public void testPaceController_timeout2() throws InterruptedException {
        
        testPaceController_timeout0(150);
    }
    
    private void testPaceController_timeout0(int ratio) throws InterruptedException {
        final RateLimiterController paceController = new RateLimiterController(500, ratio);
        final Node node = mock(Node.class);

        final AtomicInteger passcount = new AtomicInteger();
        final AtomicInteger blockcount = new AtomicInteger();
        final CountDownLatch countDown = new CountDownLatch(10);
        final int count = ratio * 2;
        final AtomicInteger done = new AtomicInteger();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < count; k++) {
                        
                        boolean pass = paceController.canPass(node, 1);

                        if (pass == true) {
                            passcount.incrementAndGet();
                        } else {
                            blockcount.incrementAndGet();
                        }
                        done.incrementAndGet();
                    }
                    countDown.countDown();
                }

            }, "Thread " + i);
            thread.start();
        }

        countDown.await();
        float ratio0 = (1000.f * passcount.get() / (System.currentTimeMillis() - time));
        
        System.out.println("pass: " + passcount.get());
        System.out.println("block: " + blockcount.get());
        System.out.println("done: " + done.get());
        System.out.println("ratio: " + ratio0);
        
        assertTrue(Math.abs(ratio0 - ratio) <= ratio / 10);
    }

    @Test
    public void testPaceController_zeroattack() throws InterruptedException {
        RateLimiterController paceController = new RateLimiterController(500, 0d);
        Node node = mock(Node.class);

        for (int i = 0; i < 2; i++) {
            assertFalse(paceController.canPass(node, 1));
            assertTrue(paceController.canPass(node, 0));
        }
    }
}
