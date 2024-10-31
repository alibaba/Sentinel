package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author quguai
 * @date 2023/10/27 13:56
 */
public class DegradePartialIntegrationTest {

    @Before
    public void setUp() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testDegradeRegex() {
        DegradeRule rule = new DegradeRule(".*")
                .setCount(0.5d)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setStatIntervalMs(20 * 1000)
                .setTimeWindow(10)
                .setMinRequestAmount(1);
        rule.setRegex(true);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        verifyDegradeFlow("testDegradeRegex_1", true, true);
        verifyDegradeFlow("testDegradeRegex_1", true, false);

        verifyDegradeFlow("testDegradeRegex_2", true, true);
        verifyDegradeFlow("testDegradeRegex_2", true, false);

    }

    private void verifyDegradeFlow(String resource, boolean error, boolean shouldPass) {
        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            assertTrue(shouldPass);
            if (error) {
                int i = 10 / 0;
            }
        } catch (BlockException e1) {
            assertFalse(shouldPass);
        } catch (Exception ex) {
            Tracer.traceEntry(ex, entry);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
