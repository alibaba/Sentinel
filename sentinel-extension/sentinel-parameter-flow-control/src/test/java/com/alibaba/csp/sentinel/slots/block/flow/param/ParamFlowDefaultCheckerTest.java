package com.alibaba.csp.sentinel.slots.block.flow.param;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
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
	public void testSingleValueCheckQpsWithoutExceptionItems() throws InterruptedException {
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
		metric.getRuleCounterMap().put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicReference<Long>>(4000));

		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		
		System.out.println("end of one second");
		//测试间隔的请求
		TimeUnit.SECONDS.sleep(3);
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertTrue(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
		assertFalse(ParamFlowChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
	}

}
