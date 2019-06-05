/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

import static org.junit.Assert.assertEquals;

/**
 * @author jialiang.linjl
 */
public class ParamFlowThrottleRateLimitingCheckerTest {

    @Test
    public void testSingleValueThrottleCheckQps() throws Exception {
        final String resourceName = "testSingleValueThrottleCheckQps";
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
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

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

        System.out.println("testSingleValueThrottleCheckQps: sleep for 3 seconds");
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
    }

    @Test
    public void testSingleValueThrottleCheckQpsMultipleThreads() throws Exception {
        final String resourceName = "testSingleValueThrottleCheckQpsMultipleThreads";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        final ParamFlowRule rule = new ParamFlowRule(resourceName)
            .setCount(threshold)
            .setParamIdx(paramIdx)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

        final String valueA = "valueA";
        ParameterMetric metric = new ParameterMetric();
        ParameterMetricStorage.getMetricsMap().put(resourceWrapper.getName(), metric);
        metric.getRuleTimeCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));

        int threadCount = 40;
        System.out.println(metric.getRuleTimeCounter(rule));

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

        System.out.println("testSingleValueThrottleCheckQpsMultipleThreads: sleep for 3 seconds");
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
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        ParameterMetricStorage.getMetricsMap().clear();
    }
}
