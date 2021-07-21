package com.alibaba.csp.sentinel.extension.rule.flow;

import com.alibaba.csp.sentinel.extension.rule.GlobalRule;
import com.alibaba.csp.sentinel.slots.block.flow.DefaultFlowRulePropertyListener;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRulePropertyListener;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jiez
 * @date : 2021/7/21 17:37
 */
public class GlobalFlowRulePropertyListener extends DefaultFlowRulePropertyListener {

    @Override
    public void configUpdate(List<FlowRule> rules) {
    }

    @Override
    public void configLoad(List<FlowRule> rules) {
        if (Objects.isNull(rules) || rules.size() <= 0) {
            return;
        }
    }

    private void handleRule(List<FlowRule> rules) {
        if (Objects.isNull(rules) || rules.size() <= 0) {
            return;
        }
        List<FlowRule> globalFlowRule = new ArrayList<>();
        List<FlowRule> normalFlowRule = new ArrayList<>();
        rules.forEach(rule -> {
            if (rule instanceof GlobalRule) {
                globalFlowRule.add(rule);
            } else {
                normalFlowRule.add(rule);
            }
        });
        super.configUpdate(normalFlowRule);
        handleGlobalRule(globalFlowRule);
    }

    private void handleGlobalRule(List<FlowRule> rules) {
        Map<String, List<FlowRule>> globalRuleMap = FlowRuleUtil.buildFlowRuleMap(rules);
    }
}
