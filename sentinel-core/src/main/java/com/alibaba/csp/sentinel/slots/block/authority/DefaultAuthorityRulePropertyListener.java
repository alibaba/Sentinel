package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : jiez
 * @date : 2021/7/23 14:06
 */
public class DefaultAuthorityRulePropertyListener extends BaseAuthorityRulePropertyListener {

    @Override
    public void configUpdate(List<AuthorityRule> rules) {
        Map<String, Set<AuthorityRule>> ruleMap = AuthorityRuleManager.buildAuthorityRuleMap(rules);
        super.updateAuthorityRules(ruleMap);
        RecordLog.info("[AuthorityRuleManager] Authority rules received: {}", AuthorityRuleManager.getRules());
    }

    @Override
    public void configLoad(List<AuthorityRule> rules) {
        Map<String, Set<AuthorityRule>> ruleMap = AuthorityRuleManager.buildAuthorityRuleMap(rules);
        super.updateAuthorityRules(ruleMap);
        RecordLog.info("[AuthorityRuleManager] Load authority rules: {}", AuthorityRuleManager.getRules());
    }


}
