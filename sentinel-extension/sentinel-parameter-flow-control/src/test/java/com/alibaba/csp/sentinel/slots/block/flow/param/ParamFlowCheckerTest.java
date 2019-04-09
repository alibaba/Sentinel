/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
 * Test cases for {@link ParamFlowChecker}.
 *
 * @author Eric Zhao
 */
public class ParamFlowCheckerTest {

	@Test
	public void testHotParamCheckerPassCheckExceedArgs() {
		final String resourceName = "testHotParamCheckerPassCheckExceedArgs";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 1;

		ParamFlowRule rule = new ParamFlowRule();
		rule.setResource(resourceName);
		rule.setCount(10);
		rule.setParamIdx(paramIdx);

		assertTrue("The rule will pass if the paramIdx exceeds provided args",
				ParamFlowChecker.passCheck(resourceWrapper, rule, 1, "abc"));
	}

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
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		long currentTime = TimeUtil.currentTimeMillis();
		long endTime = currentTime + rule.getDurationInSec() * 1000;
		int successCount = 0;
		while (currentTime <= endTime) {
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
		while (currentTime <= endTime) {
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
		final String resourceName = "testSingleValueCheckQpsWithTimeout";
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
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		int threadCount = 40;

		final CountDownLatch waitLatch = new CountDownLatch(threadCount + 5);
		final AtomicInteger successCount = new AtomicInteger();
		for (int i = 0; i < threadCount + 5; i++) {
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

		TimeUnit.SECONDS.sleep(3);
		successCount.set(0);
		final CountDownLatch waitLatch1 = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					long currentTime = TimeUtil.currentTimeMillis();
					long endTime = currentTime + rule.getDurationInSec() * 1000;
					
					while (currentTime <= endTime) {
						if (ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA)) {
							successCount.incrementAndGet();
						}
						currentTime = TimeUtil.currentTimeMillis();
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
	public void testSingleValueCheckQpsWithExceptionItems() throws InterruptedException {
		final String resourceName = "testSingleValueCheckQpsWithExceptionItems";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		TimeUtil.currentTimeMillis();
		int paramIdx = 0;

		long globalThreshold = 5L;
		int thresholdB = 0;
		int thresholdD = 7;

		ParamFlowRule rule = new ParamFlowRule();
		rule.setResource(resourceName);
		rule.setCount(globalThreshold);
		rule.setParamIdx(paramIdx);
		rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);

		String valueA = "valueA";
		String valueB = "valueB";
		String valueC = "valueC";
		String valueD = "valueD";

		// Directly set parsed map for test.
		Map<Object, Integer> map = new HashMap<Object, Integer>();
		map.put(valueB, thresholdB);
		map.put(valueD, thresholdD);
		rule.setParsedHotItems(map);

		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
		TimeUnit.SECONDS.sleep(3);
	}

	@Test
	public void testSingleValueCheckThreadCountWithExceptionItems() {
		final String resourceName = "testSingleValueCheckThreadCountWithExceptionItems";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;

		long globalThreshold = 5L;
		int thresholdB = 3;
		int thresholdD = 7;

		ParamFlowRule rule = new ParamFlowRule(resourceName).setCount(globalThreshold).setParamIdx(paramIdx)
				.setGrade(RuleConstant.FLOW_GRADE_THREAD);

		String valueA = "valueA";
		String valueB = "valueB";
		String valueC = "valueC";
		String valueD = "valueD";

		// Directly set parsed map for test.
		Map<Object, Integer> map = new HashMap<Object, Integer>();
		map.put(valueB, thresholdB);
		map.put(valueD, thresholdD);
		rule.setParsedHotItems(map);

		ParameterMetric metric = mock(ParameterMetric.class);
		when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold - 1);
		when(metric.getThreadCount(paramIdx, valueB)).thenReturn(globalThreshold - 1);
		when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold - 1);
		when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold + 1);
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);

		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));

		when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold);
		when(metric.getThreadCount(paramIdx, valueB)).thenReturn(thresholdB - 1L);
		when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold + 1);
		when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold - 1).thenReturn((long) thresholdD);

		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
	}

	@Test
	public void testPassLocalCheckForCollection() throws InterruptedException {
		final String resourceName = "testPassLocalCheckForCollection";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;
		double globalThreshold = 1;

		ParamFlowRule rule = new ParamFlowRule(resourceName).setParamIdx(paramIdx).setCount(globalThreshold);

		String v1 = "a", v2 = "B", v3 = "Cc";
		List<String> list = Arrays.asList(v1, v2, v3);
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));
		assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, list));

		TimeUnit.SECONDS.sleep(3);
	}

	@Test
	public void testPassLocalCheckForArray() throws InterruptedException {
		final String resourceName = "testPassLocalCheckForArray";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;
		double globalThreshold = 1;

		ParamFlowRule rule = new ParamFlowRule(resourceName).setParamIdx(paramIdx)
				.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER).setCount(globalThreshold);

		TimeUtil.currentTimeMillis();

		String v1 = "a", v2 = "B", v3 = "Cc";
		Object arr = new String[] { v1, v2, v3 };
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		assertTrue(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));
		assertFalse(ParamFlowChecker.passCheck(resourceWrapper, rule, 1, arr));

		TimeUnit.SECONDS.sleep(3);
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