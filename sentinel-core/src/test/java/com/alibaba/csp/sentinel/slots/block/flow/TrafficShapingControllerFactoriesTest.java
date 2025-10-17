package com.alibaba.csp.sentinel.slots.block.flow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.controller.CustomTokenBucketControllerFactory;
import com.alibaba.csp.sentinel.slots.block.flow.controller.CustomTokenBucketControllerFactory.CustomTokenBucketController;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Test cases for {@link TrafficShapingControllerFactories}.
 *
 * @author icodening
 */
public class TrafficShapingControllerFactoriesTest {



    @Test
    public void testGetBuiltInFactories() {
        // Test that all built-in factories are registered
        assertNotNull("Default factory should be registered",
                TrafficShapingControllerFactories.get(RuleConstant.CONTROL_BEHAVIOR_DEFAULT));
        assertNotNull("WarmUp factory should be registered",
                TrafficShapingControllerFactories.get(RuleConstant.CONTROL_BEHAVIOR_WARM_UP));
        assertNotNull("RateLimiter factory should be registered",
                TrafficShapingControllerFactories.get(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER));
        assertNotNull("WarmUpRateLimiter factory should be registered",
                TrafficShapingControllerFactories.get(RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER));
        assertNotNull("CustomTokenBucket factory should be registered",
                TrafficShapingControllerFactories.get(RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN));
    }

    @Test
    public void testGetNonExistentFactory() {
        // Test that non-existent control behavior returns null
        assertNull("Non-existent factory should return null",
                TrafficShapingControllerFactories.get(999));
    }


    @Test
    public void testDefaultFactoryCreatesController() {
        FlowRule rule = new FlowRule("test")
                .setCount(100)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        
        TrafficShapingControllerFactory factory = TrafficShapingControllerFactories.get(
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        assertNotNull("Factory should exist", factory);
        
        TrafficShapingController controller = factory.create(rule);
        assertNotNull("Controller should be created", controller);
    }
    @Test
    public void testCustomControllerIntegrationWithFlowRuleUtil() {
        // Create a rule with custom control behavior
        FlowRule rule = new FlowRule("integrationTestResource")
            .setCount(100)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setControlBehavior(CustomTokenBucketControllerFactory.CUSTOM_CONTROL_BEHAVIOR);

        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule);
        FlowRuleUtil.buildFlowRuleMap(rules, null, false);

        assertNotNull("Rule should have a controller", rule.getRater());
    }

    @Test
    public void testCustomControllerWithFlowRule() {
        FlowRule rule = new FlowRule("customResource")
            .setCount(50)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setControlBehavior(CustomTokenBucketControllerFactory.CUSTOM_CONTROL_BEHAVIOR);

        CustomTokenBucketControllerFactory factory = new CustomTokenBucketControllerFactory();
        TrafficShapingController controller = factory.create(rule);

        assertNotNull("Controller should be created from rule", controller);
        assertTrue("Should create CustomTokenBucketController from rule",
            controller instanceof CustomTokenBucketController);

        assertTrue("Should allow initial request", controller.canPass(null, 1));
        assertFalse("Third request should fail", controller.canPass(null, 2));
    }


    @Test
    public void testUserDefinedControlBehaviorNotInReservedRange() {
        int userDefinedBehavior = RuleConstant.CONTROL_BEHAVIOR_USER_DEFINED_MIN;
        assertFalse("User-defined control behavior should not be in reserved range",
                TrafficShapingControllerFactories.isReservedControlBehavior(userDefinedBehavior));

        assertTrue("255 should be in reserved range",
                TrafficShapingControllerFactories.isReservedControlBehavior(255));
        assertFalse("256 should not be in reserved range",
                TrafficShapingControllerFactories.isReservedControlBehavior(256));
    }
}

