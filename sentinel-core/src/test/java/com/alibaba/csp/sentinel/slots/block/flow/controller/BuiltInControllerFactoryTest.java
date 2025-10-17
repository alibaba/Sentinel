
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingControllerFactories;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingControllerFactory;
import com.alibaba.csp.sentinel.slots.block.flow.controller.CustomTokenBucketControllerFactory.CustomTokenBucketController;
import java.lang.reflect.Method;
import org.junit.Test;

/**
 * Test cases for built-in controller factories.
 *
 * @author soulx
 */
public class BuiltInControllerFactoryTest {

	@Test
	public void testDefaultControllerFactory() {
		TrafficShapingControllerFactory factory = new DefaultControllerFactory();

		assertTrue("DefaultControllerFactory should be built-in", factory.isBuiltIn());
		assertEquals("Control behavior should be DEFAULT",
				RuleConstant.CONTROL_BEHAVIOR_DEFAULT, factory.getControlBehavior());

		FlowRule rule = new FlowRule("test")
				.setCount(100)
				.setGrade(RuleConstant.FLOW_GRADE_QPS);

		TrafficShapingController controller = factory.create(rule);
		assertNotNull("Controller should be created", controller);
		assertTrue("Should create DefaultController", controller instanceof DefaultController);
	}

	@Test
	public void testWarmUpControllerFactory() {
		TrafficShapingControllerFactory factory = new WarmUpControllerFactory();

		assertTrue("WarmUpControllerFactory should be built-in", factory.isBuiltIn());
		assertEquals("Control behavior should be WARM_UP",
				RuleConstant.CONTROL_BEHAVIOR_WARM_UP, factory.getControlBehavior());

		FlowRule rule = new FlowRule("test")
				.setCount(100)
				.setGrade(RuleConstant.FLOW_GRADE_QPS)
				.setWarmUpPeriodSec(10);

		TrafficShapingController controller = factory.create(rule);
		assertNotNull("Controller should be created", controller);
		assertTrue("Should create WarmUpController", controller instanceof WarmUpController);
	}

	@Test
	public void testThrottlingControllerFactory() {
		TrafficShapingControllerFactory factory = new ThrottlingControllerFactory();

		assertTrue("ThrottlingControllerFactory should be built-in", factory.isBuiltIn());
		assertEquals("Control behavior should be RATE_LIMITER",
				RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER, factory.getControlBehavior());

		FlowRule rule = new FlowRule("test")
				.setCount(100)
				.setGrade(RuleConstant.FLOW_GRADE_QPS)
				.setMaxQueueingTimeMs(500);

		TrafficShapingController controller = factory.create(rule);
		assertNotNull("Controller should be created", controller);
		assertTrue("Should create ThrottlingController", controller instanceof ThrottlingController);
	}

	@Test
	public void testWarmUpRateLimiterControllerFactory() {
		TrafficShapingControllerFactory factory = new WarmUpRateLimiterControllerFactory();

		assertTrue("WarmUpRateLimiterControllerFactory should be built-in", factory.isBuiltIn());
		assertEquals("Control behavior should be WARM_UP_RATE_LIMITER",
				RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER, factory.getControlBehavior());

		FlowRule rule = new FlowRule("test")
				.setCount(100)
				.setGrade(RuleConstant.FLOW_GRADE_QPS)
				.setWarmUpPeriodSec(10)
				.setMaxQueueingTimeMs(500);

		TrafficShapingController controller = factory.create(rule);
		assertNotNull("Controller should be created", controller);
		assertTrue("Should create WarmUpRateLimiterController",
				controller instanceof WarmUpRateLimiterController);
	}

	@Test
	public void testCustomTokenBucketControllerFactory() {
		TrafficShapingControllerFactory factory = new CustomTokenBucketControllerFactory();

		assertTrue("CustomTokenBucketControllerFactory should be built-in", factory.isBuiltIn());
		assertEquals("Control behavior should be USER_DEFINED_MIN",
				RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN, factory.getControlBehavior());

		FlowRule rule = new FlowRule("test")
				.setCount(100)
				.setGrade(RuleConstant.FLOW_GRADE_QPS);

		TrafficShapingController controller = factory.create(rule);
		assertNotNull("Controller should be created", controller);
		assertTrue("Should create CustomTokenBucketController",
				controller instanceof CustomTokenBucketController);
	}

	@Test
	public void testInvalidReservedBehaviorFactory() {
		InvalidReservedBehaviorFactory invalidFactory = new InvalidReservedBehaviorFactory();
		assertFalse("Test factory should not be built-in", invalidFactory.isBuiltIn());
		int controlBehavior = invalidFactory.getControlBehavior();

		try {
			Method validateMethod = TrafficShapingControllerFactories.class
					.getDeclaredMethod("validateControlBehavior", TrafficShapingControllerFactory.class);
			validateMethod.setAccessible(true);

			try {
				validateMethod.invoke(null, invalidFactory);
			} catch (Exception e) {
				// Expected: InvocationTargetException wrapping IllegalArgumentException
				Throwable cause = e.getCause();
				assertTrue("Should throw IllegalArgumentException",
						cause instanceof IllegalArgumentException);

				String errorMessage = cause.getMessage();
				assertTrue("Error message should mention reserved range",
						errorMessage.contains("reserved"));
				assertTrue("Error message should mention control behavior value",
						errorMessage.contains("[" + controlBehavior + "]"));
				assertTrue("Error message should mention user-defined minimum",
						errorMessage.contains(String.valueOf(RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN)));
			}
		} catch (NoSuchMethodException e) {
			fail("validateControlBehavior method should exist: " + e.getMessage());
		}
	}

	@Test
	public void testAllBuiltInFactoriesUseReservedRange() {
		// Test that all built-in factories use control behavior in reserved range [0, 255]
		TrafficShapingControllerFactory[] factories = {
				new DefaultControllerFactory(),
				new WarmUpControllerFactory(),
				new ThrottlingControllerFactory(),
				new WarmUpRateLimiterControllerFactory(),
				new CustomTokenBucketControllerFactory()
		};

		for (TrafficShapingControllerFactory factory : factories) {
			int controlBehavior = factory.getControlBehavior();
			assertTrue(
					String.format("Built-in factory %s should use reserved control behavior, but got %d",
							factory.getClass().getSimpleName(), controlBehavior),
					TrafficShapingControllerFactories.isReservedControlBehavior(controlBehavior));
		}
	}
}

