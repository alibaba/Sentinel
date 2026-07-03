/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrentConcurrencyManagerTest {
    @Test
    public void updateTest() throws InterruptedException {
        CurrentConcurrencyManager.put(113L, 0);
        CurrentConcurrencyManager.put(223L, 0);
        final CountDownLatch countDownLatch = new CountDownLatch(1000);
        ExecutorService pool = Executors.newFixedThreadPool(100,
                new NamedThreadFactory("CurrentConcurrencyManagerTest", true)
        );
        for (int i = 0; i < 1000; i++) {
            Runnable task = () -> {
                CurrentConcurrencyManager.addConcurrency(113L, 1);
                CurrentConcurrencyManager.addConcurrency(223L, 2);
                countDownLatch.countDown();
            };
            pool.execute(task);
        }
        countDownLatch.await();
        pool.shutdown();
        Assert.assertEquals(1000, CurrentConcurrencyManager.get(113L).get());
        Assert.assertEquals(2000, CurrentConcurrencyManager.get(223L).get());
    }
}
