package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class RuleProviderTest {

    @Test
    public void testIsInRules() {
        assertFalse(RuleProvider.isInRules("testResource"));
    }

    @Test
    public void testNotIsInRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("newResource");
        rules.add(flowRule);
        FlowRuleManager.loadRules(rules);
        assertFalse(RuleProvider.isNotInRules("newResource"));
    }

}
