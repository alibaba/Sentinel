package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.property.PropertyListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author : jiez
 * @date : 2021/7/23 14:05
 */
public abstract class BaseAuthorityRulePropertyListener implements PropertyListener<List<AuthorityRule>> {

    /**
     * Provide to subclass for update
     *
     * @param rules
     */
    protected void updateAuthorityRules(Map<String, Set<AuthorityRule>> rules) {
        AuthorityRuleManager.updateAuthorityRules(rules);
    }
}
