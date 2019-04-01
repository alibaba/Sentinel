/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Test cases for {@link InMemoryMetricsRepository}.
 *
 * @author Nick Tan
 */
public class InMemoryMetricsRepositoryTest {

    private static final String DEFAULT_APP = "default";
    private static final String DEFAULT_EXPIRE_APP = "default_expire_app";
    private static final String DEFAULT_RESOURCE = "test";
    private static final long EXPIRE_TIME = 1000 * 60 * 5L;

    private InMemoryMetricsRepository inMemoryMetricsRepository;

    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        inMemoryMetricsRepository = new InMemoryMetricsRepository();
        executorService = Executors.newFixedThreadPool(8);
    }

    @After
    public void tearDown() {
        executorService.shutdownNow();
    }

    private void testSave() {
        for (int i = 0; i < 1000000; i++) {
            MetricEntity entry = new MetricEntity();
            entry.setApp(DEFAULT_APP);
            entry.setResource(DEFAULT_RESOURCE);
            entry.setTimestamp(new Date(System.currentTimeMillis()));
            entry.setPassQps(1L);
            entry.setExceptionQps(1L);
            entry.setBlockQps(0L);
            entry.setSuccessQps(1L);
            inMemoryMetricsRepository.save(entry);
        }
    }

    @Test
    public void testExpireMetric() {
        long now = System.currentTimeMillis();
        MetricEntity expireEntry = new MetricEntity();
        expireEntry.setApp(DEFAULT_EXPIRE_APP);
        expireEntry.setResource(DEFAULT_RESOURCE);
        expireEntry.setTimestamp(new Date(now - EXPIRE_TIME - 10L));
        expireEntry.setPassQps(1L);
        expireEntry.setExceptionQps(1L);
        expireEntry.setBlockQps(0L);
        expireEntry.setSuccessQps(1L);
        inMemoryMetricsRepository.save(expireEntry);

        MetricEntity entry = new MetricEntity();
        entry.setApp(DEFAULT_EXPIRE_APP);
        entry.setResource(DEFAULT_RESOURCE);
        entry.setTimestamp(new Date(now));
        entry.setPassQps(1L);
        entry.setExceptionQps(1L);
        entry.setBlockQps(0L);
        entry.setSuccessQps(1L);
        inMemoryMetricsRepository.save(entry);

        List<MetricEntity> list = inMemoryMetricsRepository.queryByAppAndResourceBetween(
            DEFAULT_EXPIRE_APP, DEFAULT_RESOURCE, now - 2 * EXPIRE_TIME, now + EXPIRE_TIME);

        assertFalse(CollectionUtils.isEmpty(list));
        assertEquals(1, list.size());
    }

    @Test
    public void testListResourcesOfApp() {
        // prepare basic test data
        testSave();
        System.out.println( "[" + System.currentTimeMillis() + "] Basic test data ready in testListResourcesOfApp");

        List<CompletableFuture> futures = Lists.newArrayList();

        // concurrent query resources of app
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(8);
        for (int j = 0; j < 10000; j++) {
            futures.add(
                CompletableFuture.runAsync(() -> {
                        try {
                            cyclicBarrier.await();
                            inMemoryMetricsRepository.listResourcesOfApp(DEFAULT_APP);
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                }, executorService)
            );
        }

        // batch add metric entity
        for (int i = 0; i < 10000; i++) {
            MetricEntity entry = new MetricEntity();
            entry.setApp(DEFAULT_APP);
            entry.setResource(DEFAULT_RESOURCE);
            entry.setTimestamp(new Date(System.currentTimeMillis() - EXPIRE_TIME - 1000L));
            entry.setPassQps(1L);
            entry.setExceptionQps(1L);
            entry.setBlockQps(0L);
            entry.setSuccessQps(1L);
            inMemoryMetricsRepository.save(entry);
        }

        CompletableFuture all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            all.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            if (e.getCause() instanceof ConcurrentModificationException) {
                fail("concurrent error occurred");
            } else {
                fail("unexpected exception");
            }
        } catch (TimeoutException e) {
            fail("allOf future timeout");
        }
    }

}