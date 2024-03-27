package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author xierz
 * @date 2020/10/4
 */
public class ResponseTimeCircuitBreakerTest extends AbstractTimeBasedTest {
    @Before
    public void setUp() {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testMaxSlowRatioThreshold() {
        String resource = "testMaxSlowRatioThreshold";
        DegradeRule rule = new DegradeRule("resource")
                .setCount(10)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(1)
                .setStatIntervalMs(5000)
                .setTimeWindow(5);
        rule.setResource(resource);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        assertTrue(entryAndSleepFor(resource, 20));
        assertTrue(entryAndSleepFor(resource, 20));
        assertTrue(entryAndSleepFor(resource, 20));

        // should be blocked, cause 3/3 requests' rt is bigger than max rt.
        assertFalse(entryAndSleepFor(resource,20));
        sleep(1000);
        assertFalse(entryAndSleepFor(resource,20));
        sleep(4000);

        assertTrue(entryAndSleepFor(resource, 20));
    }

}
