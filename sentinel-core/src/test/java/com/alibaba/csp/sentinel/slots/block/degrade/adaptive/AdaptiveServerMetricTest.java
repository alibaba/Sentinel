package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AdaptiveServerMetric}.
 *
 * @author ylnxwlp
 */
public class AdaptiveServerMetricTest {

    private static final String RESOURCE_NAME = "testResource";
    private AdaptiveServerMetric metric;

    @Before
    public void setUp() {
        metric = new AdaptiveServerMetric(RESOURCE_NAME);
    }

    @Test
    public void testDefaultValue() {
        assertEquals("Default serverCpuUsage should be -1.0", -1.0, metric.getServerCpuUsage(), 0.0);
        assertEquals("Default serverTomcatUsageRate should be -1.0", -1.0, metric.getServerTomcatUsageRate(), 0.0);
        assertEquals("Default serverTomcatQueueSize should be -1", -1, metric.getServerTomcatQueueSize());
    }

    @Test
    public void testCpuUsageSetterAndGetter() {
        double[] testValues = {0.0, 0.5, 1.0, 10.5, 50.0, 99.9, 100.0, -1.0, -10.0};

        for (double value : testValues) {
            metric.setServerCpuUsage(value);
            assertEquals("CPU usage should be " + value, value, metric.getServerCpuUsage(), 0.0);
        }
    }

    @Test
    public void testTomcatUsageRateSetterAndGetter() {
        double[] testValues = {0.0, 0.1, 0.5, 1.0, 10.0, 50.5, 99.9, 100.0, -1.0, -5.5};

        for (double value : testValues) {
            metric.setServerTomcatUsageRate(value);
            assertEquals("Tomcat usage rate should be " + value, value, metric.getServerTomcatUsageRate(), 0.0);
        }
    }

    @Test
    public void testTomcatQueueSizeSetterAndGetter() {
        int[] testValues = {0, 1, 10, 100, 1000, -1, -10, Integer.MAX_VALUE, Integer.MIN_VALUE};

        for (int value : testValues) {
            metric.setServerTomcatQueueSize(value);
            assertEquals("Tomcat queue size should be " + value, value, metric.getServerTomcatQueueSize());
        }
    }

    @Test
    public void testConcurrentVisibility_SmokeTest() throws InterruptedException {
        final int threadCount = 10;
        final int iterations = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount * 3);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 3);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        double value = threadId * 1000 + j;
                        metric.setServerCpuUsage(value);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        double value = threadId * 1000 + j + 0.5;
                        metric.setServerTomcatUsageRate(value);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        int value = threadId * 1000 + j;
                        metric.setServerTomcatQueueSize(value);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        double cpuUsage = metric.getServerCpuUsage();
        double tomcatUsageRate = metric.getServerTomcatUsageRate();
        int tomcatQueueSize = metric.getServerTomcatQueueSize();

        assertTrue("CPU usage should be a valid double", Double.isFinite(cpuUsage) || cpuUsage == -1.0);
        assertTrue("Tomcat usage rate should be a valid double", Double.isFinite(tomcatUsageRate) || tomcatUsageRate == -1.0);
        assertTrue("Tomcat queue size should be a valid integer", tomcatQueueSize != Integer.MIN_VALUE + 1);
    }

    @Test
    public void testResourceName() throws Exception {
        Field resourceNameField = AdaptiveServerMetric.class.getDeclaredField("resourceName");
        resourceNameField.setAccessible(true);
        String resourceName = (String) resourceNameField.get(metric);
        assertEquals("Resource name should match", RESOURCE_NAME, resourceName);
    }
}