package com.alibaba.csp.sentinel.extension.global.rule.degrade;

import com.alibaba.csp.sentinel.extension.global.rule.GlobalRule;
import com.alibaba.csp.sentinel.extension.global.rule.GlobalRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultDegradeRulePropertyListener;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jiez
 * @date : 2021/7/21 17:37
 */
public class GlobalDegradeRulePropertyListener extends DefaultDegradeRulePropertyListener {

    @Override
    public void configUpdate(List<DegradeRule> conf) {
        handleAllRule(conf);
    }

    @Override
    public void configLoad(List<DegradeRule> conf) {
        handleAllRule(conf);
    }

    private void handleAllRule(List<DegradeRule> rules) {
        if (Objects.isNull(rules) || rules.size() <= 0) {
            return;
        }
        List<DegradeRule> globalFlowRule = new ArrayList<>();
        List<DegradeRule> normalFlowRule = new ArrayList<>();
        rules.forEach(rule -> {
            if (rule instanceof GlobalRule) {
                globalFlowRule.add(rule);
            } else {
                normalFlowRule.add(rule);
            }
        });
        handleNormalRule(normalFlowRule);
        handleGlobalRule(globalFlowRule);
    }

    private void handleNormalRule(List<DegradeRule> rules) {
        super.configUpdate(rules);
    }

    private void handleGlobalRule(List<DegradeRule> rules) {
        Map<String, List<CircuitBreaker>> circuitBreakers = DegradeRuleManager.buildCircuitBreakers(rules);
        GlobalRuleManager.updateGlobalDegradeRules(circuitBreakers);
    }
}
