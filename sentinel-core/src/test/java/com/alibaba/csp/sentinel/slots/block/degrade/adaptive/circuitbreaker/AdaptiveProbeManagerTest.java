package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdaptiveProbeManager}.
 */
public class AdaptiveProbeManagerTest {

    private AdaptiveProbeManager manager;

    @Before
    public void setUp() {
        manager = new AdaptiveProbeManager();
        manager.clearAllProbes();
    }

    @After
    public void tearDown() {
        manager.clearAllProbes();
    }

    @Test
    public void getProbe_shouldReturnSameInstance_forSameCircuitBreaker() {
        AdaptiveCircuitBreaker cb = mock(AdaptiveCircuitBreaker.class);
        AdaptiveProbe p1 = AdaptiveProbeManager.getProbe(cb);
        AdaptiveProbe p2 = AdaptiveProbeManager.getProbe(cb);
        assertNotNull(p1);
        assertSame("getProbe should return same instance for same CB", p1, p2);
        assertEquals("probe count should be 1", 1, manager.getProbeCount());
    }

    @Test
    public void getProbe_shouldReturnDifferentInstances_forDifferentCircuitBreakers() {
        AdaptiveCircuitBreaker cb1 = mock(AdaptiveCircuitBreaker.class);
        AdaptiveCircuitBreaker cb2 = mock(AdaptiveCircuitBreaker.class);
        AdaptiveProbe p1 = AdaptiveProbeManager.getProbe(cb1);
        AdaptiveProbe p2 = AdaptiveProbeManager.getProbe(cb2);
        assertNotNull(p1);
        assertNotNull(p2);
        assertNotSame("different CBs should map to different probes", p1, p2);
        assertEquals("probe count should be 2", 2, manager.getProbeCount());
    }

    @Test
    public void removeProbe_shouldReturnExisting_and_nextGetCreatesNewInstance() {
        AdaptiveCircuitBreaker cb = mock(AdaptiveCircuitBreaker.class);
        AdaptiveProbe first = AdaptiveProbeManager.getProbe(cb);
        assertEquals(1, manager.getProbeCount());
        AdaptiveProbe removed = AdaptiveProbeManager.removeProbe(cb);
        assertSame("removeProbe should return the previously stored probe", first, removed);
        assertEquals("after removal, count should decrease", 0, manager.getProbeCount());
        AdaptiveProbe second = AdaptiveProbeManager.getProbe(cb);
        assertNotSame("after removal, a fresh probe should be created", first, second);
        assertEquals(1, manager.getProbeCount());
    }

    @Test
    public void removeProbe_onUnknownCircuitBreaker_shouldReturnNull() {
        AdaptiveCircuitBreaker cb = mock(AdaptiveCircuitBreaker.class);
        AdaptiveProbe removed = AdaptiveProbeManager.removeProbe(cb);
        assertNull("removing unknown mapping should return null", removed);
        assertEquals(0, manager.getProbeCount());
    }

    @Test
    public void clearAllProbes_shouldResetCountToZero() {
        AdaptiveCircuitBreaker cb1 = mock(AdaptiveCircuitBreaker.class);
        AdaptiveCircuitBreaker cb2 = mock(AdaptiveCircuitBreaker.class);
        AdaptiveProbeManager.getProbe(cb1);
        AdaptiveProbeManager.getProbe(cb2);
        assertEquals(2, manager.getProbeCount());
        manager.clearAllProbes();
        assertEquals("after clear, count should be 0", 0, manager.getProbeCount());
        AdaptiveProbe p = AdaptiveProbeManager.getProbe(cb1);
        assertNotNull(p);
        assertEquals(1, manager.getProbeCount());
    }

    @Test
    public void concurrentGetProbe_shouldReturnSingleInstance_andCountOne() throws Exception {
        final AdaptiveCircuitBreaker cb = mock(AdaptiveCircuitBreaker.class);
        final int threads = 20;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch doneGate = new CountDownLatch(threads);
        final AdaptiveProbe[] results = new AdaptiveProbe[threads];
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    startGate.await();
                    results[idx] = AdaptiveProbeManager.getProbe(cb);
                } catch (InterruptedException ignored) {
                } finally {
                    doneGate.countDown();
                }
            });
        }
        startGate.countDown();
        doneGate.await(5, TimeUnit.SECONDS);
        pool.shutdownNow();
        AdaptiveProbe first = results[0];
        assertNotNull("first result should not be null", first);
        for (int i = 1; i < threads; i++) {
            assertSame("all concurrent results should be same instance", first, results[i]);
        }
        assertEquals("probe count should be exactly 1", 1, manager.getProbeCount());
        long nulls = Arrays.stream(results).filter(Objects::isNull).count();
        assertEquals(0, nulls);
    }
}
