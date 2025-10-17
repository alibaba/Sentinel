package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class RuleProvider {

    public static List<AbstractRule> getAllRules() {
        List<AuthorityRule> authorityRules = AuthorityRuleManager.getRules();
        List<DegradeRule> degradeRules = DegradeRuleManager.getRules();
        List<FlowRule> flowRules = FlowRuleManager.getRules();
        List<SystemRule> systemRules = SystemRuleManager.getRules();

        List<AbstractRule> allRules = new ArrayList<>();
        allRules.addAll(authorityRules);
        allRules.addAll(degradeRules);
        allRules.addAll(flowRules);
        allRules.addAll(systemRules);

        return allRules;
    }

    public static boolean isInRules(String resource) {
        List<AbstractRule> allRules = getAllRules();
        for (AbstractRule abstractRule : allRules) {
            if (StringUtil.equals(abstractRule.getResource(), resource)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNotInRules(String resource) {
        return !isInRules(resource);
    }
}
