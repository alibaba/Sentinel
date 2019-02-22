package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * InMemoryMetricsRepository Test
 *
 * @author Nick Tan
 */
public class InMemoryMetricsRepositoryTest {

    private static final String DEFAULT_APP = "default";
    private static final String DEFAULT_EXPIRE_APP = "default_expire_app";
    private static final String DEFAULT_RESOURCE = "test";
    private static final long EXPIRE_TIME = 1000 * 60 * 5L;
    private InMemoryMetricsRepository inMemoryMetricsRepository;

    private static final int AVAILABLE_CPU_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_CPU_PROCESSORS);

    @Before
    public void setUp() throws Exception {

        inMemoryMetricsRepository = new InMemoryMetricsRepository();
    }

    @Test
    public void save() throws InterruptedException {

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
    public void testExpireMetric() throws InterruptedException {

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

        Assert.assertEquals(false, CollectionUtils.isEmpty(list));

        assertTrue(list.size() == 1);

    }

    @Test
    public void listResourcesOfApp() throws InterruptedException {
        // prepare basic test data
        save();
        System.out.println(System.currentTimeMillis() + "[basic test data ready]");

        List<CompletableFuture> futures = Lists.newArrayList();

        // concurrent query resources of app
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(AVAILABLE_CPU_PROCESSORS);
        for (int j = 0; j < 10000; j++) {

            futures.add(
                CompletableFuture.runAsync(() -> {
                        try {
                            cyclicBarrier.await();
                            inMemoryMetricsRepository.listResourcesOfApp(DEFAULT_APP);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                    }, executorService
                ));
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

        CompletableFuture all = CompletableFuture.allOf(futures.toArray((new CompletableFuture[futures.size()])));
        try {
            all.join();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            assertFalse("concurrent error", e instanceof ConcurrentModificationException);
        }
    }

}