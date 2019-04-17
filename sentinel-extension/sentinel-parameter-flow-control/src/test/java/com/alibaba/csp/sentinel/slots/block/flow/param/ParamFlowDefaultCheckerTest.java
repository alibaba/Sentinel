package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.TimeUtil;

public class ParamFlowDefaultCheckerTest {
	@Before
	public void setUp() throws Exception {
		ParamFlowSlot.getMetricsMap().clear();
	}

	@After
	public void tearDown() throws Exception {
		ParamFlowSlot.getMetricsMap().clear();
	}

	@Test
	public void testSingleQps() throws InterruptedException {
		final String resourceName = "testSingleValueCheckQpsWithoutExceptionItems";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;
		TimeUtil.currentTimeMillis();

		long threshold = 5L;

		ParamFlowRule rule = new ParamFlowRule();
		rule.setResource(resourceName);
		rule.setCount(threshold);
		rule.setParamIdx(paramIdx);

		String valueA = "valueA";
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
		metric.getRuleQPSCounters().put(rule,
				new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		System.out.println("end of one second");
		// 测试间隔的请求
		TimeUnit.SECONDS.sleep(3);
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
	}
	


	@Test
	public void testSingleQpsWithBurst() throws InterruptedException {
		final String resourceName = "testSingleQpsWithBurst";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;
		TimeUtil.currentTimeMillis();

		long threshold = 5L;

		ParamFlowRule rule = new ParamFlowRule();
		rule.setResource(resourceName);
		rule.setCount(threshold);
		rule.setParamIdx(paramIdx);
		rule.setBurstCount(3);

		String valueA = "valueA";
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
		metric.getRuleQPSCounters().put(rule,
				new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

		System.out.println(TimeUtil.currentTimeMillis());
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		System.out.println("end of one second");
		// 测试间隔的请求
		TimeUnit.MILLISECONDS.sleep(1002);
		System.out.println(TimeUtil.currentTimeMillis());
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		TimeUnit.MILLISECONDS.sleep(1002);
		System.out.println(TimeUtil.currentTimeMillis());
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		TimeUnit.SECONDS.sleep(2);
		System.out.println(TimeUtil.currentTimeMillis());
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		TimeUnit.MILLISECONDS.sleep(1002);
		System.out.println(TimeUtil.currentTimeMillis());
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
	}

	@Test
	public void testQpsInDifferentDuration() throws InterruptedException {
		final String resourceName = "testQpsInDifferentDuration";
		final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
		int paramIdx = 0;
		TimeUtil.currentTimeMillis();

		long threshold = 5L;

		ParamFlowRule rule = new ParamFlowRule();
		rule.setResource(resourceName);
		rule.setCount(threshold);
		rule.setParamIdx(paramIdx);
		rule.setDurationInSec(60);

		String valueA = "helloWorld";
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
		metric.getRuleQPSCounters().put(rule,
				new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(4000));

		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		System.out.println("end of one second");
		// 测试间隔的请求
		TimeUnit.SECONDS.sleep(1);
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		// 测试间隔的请求
		TimeUnit.SECONDS.sleep(10);
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		
		TimeUnit.SECONDS.sleep(30);
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		
		TimeUnit.SECONDS.sleep(30);
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));

		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
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

		final String valueA = "valueA";
		ParameterMetric metric = new ParameterMetric();
		ParamFlowSlot.getMetricsMap().put(resourceWrapper, metric);
		metric.getRuleTimeCounters().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(4000));
		metric.getRuleQPSCounters().put(rule,
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

}
