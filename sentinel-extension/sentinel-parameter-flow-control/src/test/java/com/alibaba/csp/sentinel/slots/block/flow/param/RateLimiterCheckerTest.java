package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author jialiang.linjl
 */
public class RateLimiterCheckerTest {

    @Test
    public void testSingleValueCheckQps() throws InterruptedException {
        final String resourceName = "testSingleValueCheckQpsWithoutExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        TimeUtil.currentTimeMillis();

        long threshold = 5L;

        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

        String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        long currentTime = TimeUtil.currentTimeMillis();
        long endTime = currentTime + rule.getDurationInSec() * 1000;
        int successCount = 0;
        while (currentTime <= endTime - 10) {
            if (ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA)) {
                successCount++;
            }
            currentTime = TimeUtil.currentTimeMillis();
        }
        assertEquals(successCount, threshold);

        // 模仿比较长的时间的停顿
        System.out.println("rest for 3 seconds");
        TimeUnit.SECONDS.sleep(3);

        currentTime = TimeUtil.currentTimeMillis();
        endTime = currentTime + rule.getDurationInSec() * 1000;
        successCount = 0;
        while (currentTime <= endTime - 10) {
            if (ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA)) {
                successCount++;
            }
            currentTime = TimeUtil.currentTimeMillis();
        }
        assertEquals(successCount, threshold);

        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testSingleValueCheckQpsInThreads() throws InterruptedException {
        final String resourceName = "testSingleValueCheckQpsInThreads";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;
        TimeUtil.currentTimeMillis();

        long threshold = 5L;

        final ParamFlowRule rule = new ParamFlowRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

        final String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
        metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

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

        assertEquals(successCount.get(), 1);
        System.out.println(threadCount);
        successCount.set(0);

        System.out.println("sleep for 3 seconds");
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

                        Random random = new Random();

                        try {
                            TimeUnit.MILLISECONDS.sleep(random.nextInt(20));
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
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
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testDuation() {

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
