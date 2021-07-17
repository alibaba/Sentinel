package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.RuleSelector;

import java.util.List;
import java.util.Map;

/**
 * @author : jiez
 * @date : 2021/7/17 11:23
 */
public class DefaultFlowRuleSelector implements RuleSelector<FlowRule> {

    @Override
    public List<String> getSupportedRuleTypes() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public List<FlowRule> select(String resource) {
        // Flow rule map should not be null.
        Map<String, List<FlowRule>> flowRules = FlowRuleManager.getFlowRuleMap();
        return flowRules.get(resource);
    }
}
