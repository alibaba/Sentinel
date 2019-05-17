package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author jialiang.linjl
 */
public class ParamFlowDefaultCheckerTest extends AbstractTimeBasedTest {

    @Test
    public void testParamFlowDefaultCheckSingleQps() {
        final String resourceName = "testParamFlowDefaultCheckSingleQps";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);

        String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule,
            new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

        // We mock the time directly to avoid unstable behaviour.
        setCurrentMillis(System.currentTimeMillis());

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleep(3000);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
    }

    @Test
    public void testParamFlowDefaultCheckSingleQpsWithBurst() throws InterruptedException {
        final String resourceName = "testParamFlowDefaultCheckSingleQpsWithBurst";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);
        rule.setBurstCount(3);

        String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule,
            new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

        // We mock the time directly to avoid unstable behaviour.
        setCurrentMillis(System.currentTimeMillis());

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleep(1002);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleep(1002);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleep(2000);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleep(1002);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
    }

    @Test
    public void testParamFlowDefaultCheckQpsInDifferentDuration() throws InterruptedException {
        final String resourceName = "testParamFlowDefaultCheckQpsInDifferentDuration";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);
        rule.setDurationInSec(60);

        String valueA = "helloWorld";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule,
            new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

        // We mock the time directly to avoid unstable behaviour.
        setCurrentMillis(System.currentTimeMillis());

        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleepSecond(1);
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleepSecond(10);
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleepSecond(30);
        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        sleepSecond(30);
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

        assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
    }

    @Test
    public void testParamFlowDefaultCheckSingleValueCheckQpsMultipleThreads() throws Exception {
        // In this test case we use the actual time.
        useActualTime();

        final String resourceName = "testParamFlowDefaultCheckSingleValueCheckQpsMultipleThreads";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        final ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);

        final String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
        metric.getRuleTokenCounterMap().put(rule,
            new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));
        int threadCount = 40;

        final CountDownLatch waitLatch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA)) {
                        successCount.incrementAndGet();
                    }
                    waitLatch.countDown();
                }

            });
            t.setName("sentinel-simulate-traffic-task-" + i);
            t.start();
        }
        waitLatch.await();

        assertEquals(successCount.get(), threshold);
        successCount.set(0);

        System.out.println("testParamFlowDefaultCheckSingleValueCheckQpsMultipleThreads: sleep for 3 seconds");
        TimeUnit.SECONDS.sleep(3);

        successCount.set(0);
        final CountDownLatch waitLatch1 = new CountDownLatch(threadCount);
        final long currentTime = TimeUtil.currentTimeMillis();
        final long endTime = currentTime + rule.getDurationInSec() * 1000 - 1;
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    long currentTime1 = currentTime;
                    while (currentTime1 <= endTime) {
                        if (ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA)) {
                            successCount.incrementAndGet();
                        }

                        try {
                            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(20));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        currentTime1 = TimeUtil.currentTimeMillis();
                    }

                    waitLatch1.countDown();
                }

            });
            t.setName("sentinel-simulate-traffic-task-" + i);
            t.start();
        }
        waitLatch1.await();

        assertEquals(successCount.get(), threshold);
    }

    @Before
    public void setUp() throws Exception {
        ParamFlowSlot.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        ParamFlowSlot.getMetricsMap().clear();
    }
}
