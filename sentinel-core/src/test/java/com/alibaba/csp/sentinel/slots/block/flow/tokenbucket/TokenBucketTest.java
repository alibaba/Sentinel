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

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author LearningGp
 */
public class TokenBucketTest extends AbstractTimeBasedTest {

    private static ThreadPoolExecutor threadPoolExecutor;

    @BeforeClass
    public static void beforeClass() throws Exception {
        threadPoolExecutor = new ThreadPoolExecutor(64, 64, 0,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("sentinel-token-bucket-test", true),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        threadPoolExecutor.shutdownNow();
    }

    @Test
    public void testForDefaultTokenBucket() throws InterruptedException {
        long unitProduceNum = 1;
        long maxTokenNum = 2;
        long intervalInMs = 1000;
        long testStart = System.currentTimeMillis();
        setCurrentMillis(testStart);

        DefaultTokenBucket defaultTokenBucket = new DefaultTokenBucket(unitProduceNum, maxTokenNum, intervalInMs);

        assertTrue(defaultTokenBucket.tryConsume(1));
        assertFalse(defaultTokenBucket.tryConsume(1));

        DefaultTokenBucket defaultTokenBucketFullStart = new DefaultTokenBucket(unitProduceNum, maxTokenNum,
                true, intervalInMs);

        assertTrue(defaultTokenBucketFullStart.tryConsume(2));
        assertFalse(defaultTokenBucketFullStart.tryConsume(1));

        sleep(1000);
        assertTrue(defaultTokenBucket.tryConsume(1));
        assertFalse(defaultTokenBucket.tryConsume(1));

        sleep(1000);
        assertTrue(defaultTokenBucketFullStart.tryConsume(2));
        assertFalse(defaultTokenBucketFullStart.tryConsume(1));
    }

    @Test
    public void testForStrictTokenBucket() throws InterruptedException {
        long unitProduceNum = 5;
        long maxTokenNum = 10;
        long intervalInMs = 1000;
        final int n = 64;
        long testStart = System.currentTimeMillis();
        setCurrentMillis(testStart);

        final AtomicLong passNum = new AtomicLong();
        final AtomicLong passNumFullStart = new AtomicLong();
        final CountDownLatch countDownLatch = new CountDownLatch(n);
        final CountDownLatch countDownLatchFullStart = new CountDownLatch(n);
        final StrictTokenBucket strictTokenBucket = new StrictTokenBucket(unitProduceNum, maxTokenNum, intervalInMs);
        final StrictTokenBucket strictTokenBucketFullStart = new StrictTokenBucket(unitProduceNum, maxTokenNum, true,
                intervalInMs);

        for (int i = 0; i < n; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (strictTokenBucket.tryConsume(1)) {
                        passNum.incrementAndGet();
                    }
                    countDownLatch.countDown();
                }
            });
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (strictTokenBucketFullStart.tryConsume(1)) {
                        passNumFullStart.incrementAndGet();
                    }
                    countDownLatchFullStart.countDown();
                }
            });
        }

        countDownLatch.await();
        countDownLatchFullStart.await();
        assertEquals(5, passNum.longValue());
        assertEquals(10, passNumFullStart.longValue());
    }

}
