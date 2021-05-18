package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RuleProviderTest {

    @Test
    public void testIsInRules() {
        Assert.assertFalse(RuleProvider.isInRules("testResource"));
    }

    @Test
    public void testNotIsInRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("newResource");
        rules.add(flowRule);
        FlowRuleManager.loadRules(rules);
        Assert.assertFalse(RuleProvider.isNotInRules("newResource"));
    }

}
