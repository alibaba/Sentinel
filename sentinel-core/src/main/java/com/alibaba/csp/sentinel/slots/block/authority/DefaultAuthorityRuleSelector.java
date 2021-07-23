package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/23 11:08
 */
public class DefaultAuthorityRuleSelector implements RuleSelector<AuthorityRule> {

    @Override
    public List<String> getSupportedRuleTypes() {
        return Collections.singletonList(RuleConstant.RULE_SELECTOR_TYPE_AUTHORITY_RULE);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public List<AuthorityRule> select(String resource) {
        Map<String, Set<AuthorityRule>> authorityRules = AuthorityRuleManager.getAuthorityRules();
        if (authorityRules == null) {
            return null;
        }
        return new ArrayList<>(authorityRules.get(resource));
    }
}
