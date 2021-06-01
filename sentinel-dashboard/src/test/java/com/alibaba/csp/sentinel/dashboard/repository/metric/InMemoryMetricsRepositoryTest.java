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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link InMemoryMetricsRepository}.
 *
 * @author Nick Tan
 */
public class InMemoryMetricsRepositoryTest {

    private final static String DEFAULT_APP = "defaultApp";
    private final static String DEFAULT_RESOURCE = "defaultResource";
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


    @Test
    public void testSave() {
        MetricEntity entry = new MetricEntity();
        entry.setApp("testSave");
        entry.setResource("testResource");
        entry.setTimestamp(new Date(System.currentTimeMillis()));
        entry.setPassQps(1L);
        entry.setExceptionQps(1L);
        entry.setBlockQps(0L);
        entry.setSuccessQps(1L);
        inMemoryMetricsRepository.save(entry);
        List<String> resources = inMemoryMetricsRepository.listResourcesOfApp("testSave");
        Assert.assertTrue(resources.size() == 1 && "testResource".equals(resources.get(0)));
    }


    @Test
    public void testSaveAll() {
        List<MetricEntity> entities = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            MetricEntity entry = new MetricEntity();
            entry.setApp("testSaveAll");
            entry.setResource("testResource" + i);
            entry.setTimestamp(new Date(System.currentTimeMillis()));
            entry.setPassQps(1L);
            entry.setExceptionQps(1L);
            entry.setBlockQps(0L);
            entry.setSuccessQps(1L);
            entities.add(entry);
        }
        inMemoryMetricsRepository.saveAll(entities);
        List<String> result = inMemoryMetricsRepository.listResourcesOfApp("testSaveAll");
        Assert.assertTrue(result.size() == entities.size());
    }


    @Test
    public void testExpireMetric() {
        long now = System.currentTimeMillis();
        MetricEntity expireEntry = new MetricEntity();
        expireEntry.setApp(DEFAULT_APP);
        expireEntry.setResource(DEFAULT_RESOURCE);
        expireEntry.setTimestamp(new Date(now - EXPIRE_TIME - 1L));
        expireEntry.setPassQps(1L);
        expireEntry.setExceptionQps(1L);
        expireEntry.setBlockQps(0L);
        expireEntry.setSuccessQps(1L);
        inMemoryMetricsRepository.save(expireEntry);

        MetricEntity entry = new MetricEntity();
        entry.setApp(DEFAULT_APP);
        entry.setResource(DEFAULT_RESOURCE);
        entry.setTimestamp(new Date(now));
        entry.setPassQps(1L);
        entry.setExceptionQps(1L);
        entry.setBlockQps(0L);
        entry.setSuccessQps(1L);
        inMemoryMetricsRepository.save(entry);

        List<MetricEntity> list = inMemoryMetricsRepository.queryByAppAndResourceBetween(
                DEFAULT_APP, DEFAULT_RESOURCE, now - EXPIRE_TIME, now);

        assertFalse(CollectionUtils.isEmpty(list));
        assertEquals(1, list.size());
        assertTrue(list.get(0).getTimestamp().getTime() >= now - EXPIRE_TIME && list.get(0).getTimestamp().getTime() <= now);

    }


    @Test
    public void testConcurrentPutAndGet() {

        List<CompletableFuture> futures = new ArrayList<>(10000);
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(8);

        for (int j = 0; j < 10000; j++) {
            final int finalJ = j;
            futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            cyclicBarrier.await();
                            if (finalJ % 2 == 0) {
                                batchSave();
                            } else {
                                inMemoryMetricsRepository.listResourcesOfApp(DEFAULT_APP);
                            }

                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }

                    }, executorService)
            );
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

    private void batchSave() {
        for (int i = 0; i < 100; i++) {
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


}