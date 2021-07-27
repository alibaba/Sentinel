package com.alibaba.csp.sentinel.extension.global.rule.authority;

import com.alibaba.csp.sentinel.extension.global.rule.GlobalRule;
import com.alibaba.csp.sentinel.extension.global.rule.GlobalRuleManager;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.authority.DefaultAuthorityRulePropertyListener;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/21 17:37
 */
public class GlobalAuthorityRulePropertyListener extends DefaultAuthorityRulePropertyListener {

    @Override
    public void configUpdate(List<AuthorityRule> rules) {
        handleAllRule(rules);
    }

    @Override
    public void configLoad(List<AuthorityRule> rules) {
        handleAllRule(rules);
    }


    private void handleAllRule(List<AuthorityRule> rules) {
        if (Objects.isNull(rules) || rules.size() <= 0) {
            return;
        }
        List<AuthorityRule> globalFlowRule = new ArrayList<>();
        List<AuthorityRule> normalFlowRule = new ArrayList<>();
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

    private void handleNormalRule(List<AuthorityRule> rules) {
        super.configUpdate(rules);
    }

    private void handleGlobalRule(List<AuthorityRule> rules) {
        Map<String, Set<AuthorityRule>> ruleMapTemp = AuthorityRuleManager.buildAuthorityRuleMap(rules);
        Map<String, List<AuthorityRule>> ruleMap = new HashMap<>();
        for (Map.Entry<String, Set<AuthorityRule>> authorityRuleEntry : ruleMapTemp.entrySet()) {
            if (Objects.nonNull(authorityRuleEntry.getValue())) {
                ruleMap.put(authorityRuleEntry.getKey(), new ArrayList<>(authorityRuleEntry.getValue()));
            }
            GlobalRuleManager.updateGlobalAuthorityRules(ruleMap);
        }
    }
}
