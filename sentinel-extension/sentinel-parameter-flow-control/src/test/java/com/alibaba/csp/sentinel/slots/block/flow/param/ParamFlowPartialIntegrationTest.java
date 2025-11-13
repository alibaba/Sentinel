package com.alibaba.csp.sentinel.slots.block.flow.param;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author quguai
 * @date 2023/10/27 13:44
 */
public class ParamFlowPartialIntegrationTest {

    @Before
    public void setUp() throws Exception {
        ParamFlowRuleManager.loadRules(new ArrayList<>());
    }

    @After
    public void tearDown() throws Exception {
        ParamFlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testParamFlowRegex() {
        ParamFlowRule rule = new ParamFlowRule(".*")
                .setParamIdx(0)
                .setCount(1);
        rule.setRegex(true);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        verifyFlow("testParamFlowRegex_1", true, "args");
        verifyFlow("testParamFlowRegex_1", true, "args_1");

        verifyFlow("testParamFlowRegex_1", false, "args");
        verifyFlow("testParamFlowRegex_1", false, "args_1");

        verifyFlow("testParamFlowRegex_2", true, "args");
        verifyFlow("testParamFlowRegex_2", true, "args_1");

        verifyFlow("testParamFlowRegex_2", false, "args");
        verifyFlow("testParamFlowRegex_2", false, "args_1");
    }


    private void verifyFlow(String resource, boolean shouldPass, String... args) {
        Entry e = null;
        try {
            e = SphU.entry(resource, 1, EntryType.IN, args);
            assertTrue(shouldPass);
        } catch (BlockException e1) {
            assertFalse(shouldPass);
        } finally {
            if (e != null) {
                e.exit(1, args);
            }
        }
    }

}
