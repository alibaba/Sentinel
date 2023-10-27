package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class RuleManagerTest {

    private RuleManager<FlowRule> ruleManager;

    @Before
    public void setUp() throws Exception {
        ruleManager = new RuleManager<>();
    }

    @Test
    public void testUpdateRules() throws Exception {
        // Setup
        final Map<String, List<FlowRule>> rulesMap = generateFlowRules(true);

        // Run the test
        ruleManager.updateRules(rulesMap);

        // Verify the results
        assertEquals(ruleManager.getRules().size(), 2);
        Field regexRules = RuleManager.class.getDeclaredField("regexRules");
        regexRules.setAccessible(true);
        assertEquals(((Map)regexRules.get(ruleManager)).size(), 1);
        Field simpleRules = RuleManager.class.getDeclaredField("simpleRules");
        simpleRules.setAccessible(true);
        assertEquals(((Map)simpleRules.get(ruleManager)).size(), 1);
    }

    @Test
    public void testGetRulesWithCache() throws Exception {
        // Setup
        final Map<String, List<FlowRule>> rulesMap = generateFlowRules(true);

        // Run the test
        ruleManager.updateRules(rulesMap);

        // Verify the results
        Field regexCacheRules = RuleManager.class.getDeclaredField("regexCacheRules");
        regexCacheRules.setAccessible(true);
        assertEquals(((Map)regexCacheRules.get(ruleManager)).size(), 0);
        ruleManager.getRules("rule2");
        assertEquals(((Map)regexCacheRules.get(ruleManager)).size(), 1);
    }

    @Test
    public void testRebuildRulesWhenUpdateRules() throws Exception {
        // Setup
        final Map<String, List<FlowRule>> rulesMap = generateFlowRules(true);

        // Run the test
        ruleManager.updateRules(rulesMap);
        ruleManager.getRules("rule2");
        ruleManager.updateRules(generateFlowRules(true));

        // Verify the results
        Field regexCacheRules = RuleManager.class.getDeclaredField("regexCacheRules");
        regexCacheRules.setAccessible(true);
        assertEquals(((Map)regexCacheRules.get(ruleManager)).size(), 1);

        // Clean up regular rules
        ruleManager.updateRules(generateFlowRules(false));
        // Verify the results
        assertEquals(((Map)regexCacheRules.get(ruleManager)).size(), 0);
    }
    @Test
    public void testValidRegexRule() {
        // Setup
        FlowRule flowRule = new FlowRule();
        flowRule.setRegex(true);
        flowRule.setResource("{}");
        // Run the test and verify
        Assert.assertFalse(RuleManager.checkRegexResourceField(flowRule));

        flowRule.setResource(".*");
        // Run the test and verify
        Assert.assertTrue(RuleManager.checkRegexResourceField(flowRule));
    }

    @Test
    public void testHasConfig() {
        // Setup
        final Map<String, List<FlowRule>> rulesMap = generateFlowRules(true);

        // Run the test and verify the results
        ruleManager.updateRules(rulesMap);
        assertTrue(ruleManager.hasConfig("rule1"));
        assertFalse(ruleManager.hasConfig("rule3"));
    }

    private Map<String, List<FlowRule>> generateFlowRules(boolean withRegex) {
        Map<String, List<FlowRule>> result = new HashMap<>(2);
        FlowRule flowRule1 = new FlowRule("rule1");
        flowRule1.setRegex(withRegex);
        result.put(flowRule1.getResource(), Collections.singletonList(flowRule1));
        FlowRule flowRule2 = new FlowRule("rule2");
        result.put(flowRule2.getResource(), Collections.singletonList(flowRule2));
        return result;
    }
}
