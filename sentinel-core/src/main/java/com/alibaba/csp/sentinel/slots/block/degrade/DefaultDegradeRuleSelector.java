package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author : jiez
 * @date : 2021/7/23 11:08
 */
public class DefaultDegradeRuleSelector implements RuleSelector<CircuitBreaker> {

    @Override
    public List<String> getSupportedRuleTypes() {
        return Collections.singletonList(RuleConstant.RULE_SELECTOR_TYPE_DEGRADE_RULE);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public List<CircuitBreaker> select(String resource) {
        return DegradeRuleManager.getCircuitBreakers(resource);
    }
}
