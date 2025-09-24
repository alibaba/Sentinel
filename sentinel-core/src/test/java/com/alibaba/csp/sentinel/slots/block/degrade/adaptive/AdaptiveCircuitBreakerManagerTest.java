package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AdaptiveCircuitBreakerManager}.
 *
 * @author ylnxwlp
 */
public class AdaptiveCircuitBreakerManagerTest {

    private static final String RESOURCE_A = "resourceA";
    private static final String RESOURCE_B = "resourceB";
    private static final String RESOURCE_X = "resourceX";

    @Before
    public void setUp() {
        AdaptiveCircuitBreakerManager.clearAll();
    }

    @After
    public void tearDown() {
        AdaptiveCircuitBreakerManager.clearAll();
    }

    @Test
    public void testGetAdaptiveCircuitBreaker_SameInstance() {
        AdaptiveCircuitBreaker breaker1 = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        AdaptiveCircuitBreaker breaker2 = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        assertNotNull(breaker1);
        assertNotNull(breaker2);
        assertSame(breaker1, breaker2);
    }

    @Test
    public void testGetAdaptiveCircuitBreaker_DifferentResources() {
        AdaptiveCircuitBreaker breakerA = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        AdaptiveCircuitBreaker breakerB = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_B);
        assertNotNull(breakerA);
        assertNotNull(breakerB);
        assertNotSame(breakerA, breakerB);
    }

    @Test
    public void testGetAdaptiveCircuitBreaker_ConcurrentCreationUniqueness() throws InterruptedException {
        AdaptiveCircuitBreakerManager.removeAdaptiveCircuitBreaker(RESOURCE_X);
        final int threadCount = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final AdaptiveCircuitBreaker[] breakers = new AdaptiveCircuitBreaker[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    breakers[index] = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_X);
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
        AdaptiveCircuitBreaker firstBreaker = breakers[0];
        assertNotNull("First breaker should not be null", firstBreaker);
        for (int i = 1; i < threadCount; i++) {
            assertSame("All breakers should be the same instance", firstBreaker, breakers[i]);
        }
    }

    @Test
    public void testRemoveAdaptiveCircuitBreaker() {
        AdaptiveCircuitBreaker breaker1 = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        assertNotNull(breaker1);
        AdaptiveCircuitBreakerManager.removeAdaptiveCircuitBreaker(RESOURCE_A);
        AdaptiveCircuitBreaker breaker2 = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        assertNotNull(breaker2);
        assertNotSame(breaker1, breaker2);
    }

    @Test
    public void testClearAll() {
        AdaptiveCircuitBreaker breakerA = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        AdaptiveCircuitBreaker breakerB = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_B);
        assertNotNull(breakerA);
        assertNotNull(breakerB);
        AdaptiveCircuitBreakerManager.clearAll();
        AdaptiveCircuitBreaker breakerAAfterClear = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_A);
        AdaptiveCircuitBreaker breakerBAfterClear = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_B);
        assertNotNull(breakerAAfterClear);
        assertNotNull(breakerBAfterClear);
        assertNotSame(breakerA, breakerAAfterClear);
        assertNotSame(breakerB, breakerBAfterClear);
    }

    @Test
    public void testConcurrentGetAndRemove_NoException() throws InterruptedException {
        final int threadCount = 50;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount * 2);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_X);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AdaptiveCircuitBreakerManager.removeAdaptiveCircuitBreaker(RESOURCE_X);
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
        assertTrue(true);
    }

    @Test
    public void testConcurrentGetAndClearAll_NoException() throws InterruptedException {
        final int threadCount = 50;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount * 2);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(RESOURCE_X);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AdaptiveCircuitBreakerManager.clearAll();
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
        assertTrue(true);
    }
}