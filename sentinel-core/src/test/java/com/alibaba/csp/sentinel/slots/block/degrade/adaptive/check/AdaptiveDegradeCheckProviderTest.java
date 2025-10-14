package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import org.junit.*;
import org.junit.rules.TestName;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Unit test for {@link AdaptiveDegradeCheckProvider}.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeCheckProviderTest {

    @Rule
    public TestName testName = new TestName();

    @Before
    public void resetSingleton() throws Exception {
        Field f = AdaptiveDegradeCheckProvider.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @After
    public void cleanup() throws Exception {
        Field f = AdaptiveDegradeCheckProvider.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void testSingleton_SameReferenceAcrossCalls() {
        AdaptiveDegradeCheck a = AdaptiveDegradeCheckProvider.getInstance();
        AdaptiveDegradeCheck b = AdaptiveDegradeCheckProvider.getInstance();
        Assert.assertSame("getInstance() should return the same reference", a, b);
    }

    @Test
    public void testConcurrent_GetInstanceIsSingleton() throws Exception {
        final int threads = 64;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final List<AdaptiveDegradeCheck> results =
                Collections.synchronizedList(new ArrayList<>(threads));

        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                    results.add(AdaptiveDegradeCheckProvider.getInstance());
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            t.start();
        }

        start.countDown();
        done.await();

        Assert.assertFalse("No instance captured", results.isEmpty());
        AdaptiveDegradeCheck first = results.get(0);
        for (int i = 1; i < results.size(); i++) {
            Assert.assertSame("All threads should see the same singleton instance", first, results.get(i));
        }
    }

    @Test
    public void testNoSPI_ReturnsDefaultImplementation() {
        Assume.assumeFalse("SPI is present on classpath, skipping this test.", isSpiPresent());

        AdaptiveDegradeCheck inst = AdaptiveDegradeCheckProvider.getInstance();
        Assert.assertNotNull(inst);
        Assert.assertTrue(
                "Should fallback to DefaultAdaptiveDegradeCheck when no SPI is provided",
                inst instanceof DefaultAdaptiveDegradeCheck
        );
    }

    @Test
    public void testWithSPI_ReturnsFirstDiscoveredImplementation() {
        Assume.assumeTrue("No SPI on classpath, skipping this test.", isSpiPresent());

        Class<?> firstSpi = firstSpiImplClass();
        Assert.assertNotNull("First SPI class should not be null", firstSpi);

        AdaptiveDegradeCheck inst = AdaptiveDegradeCheckProvider.getInstance();
        Assert.assertNotNull(inst);

        Assert.assertEquals(
                "Provider should return the first SPI implementation discovered by ServiceLoader",
                firstSpi, inst.getClass()
        );
    }

    private static boolean isSpiPresent() {
        ServiceLoader<AdaptiveDegradeCheck> loader = ServiceLoader.load(AdaptiveDegradeCheck.class);
        Iterator<AdaptiveDegradeCheck> it = loader.iterator();
        try {
            return it.hasNext();
        } catch (Throwable t) {
            return false;
        }
    }

    private static Class<?> firstSpiImplClass() {
        ServiceLoader<AdaptiveDegradeCheck> loader = ServiceLoader.load(AdaptiveDegradeCheck.class);
        for (AdaptiveDegradeCheck impl : loader) {
            return impl.getClass();
        }
        return null;
    }
}
