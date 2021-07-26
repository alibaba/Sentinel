package com.alibaba.csp.sentinel.extension.rule.authority;

import com.alibaba.csp.sentinel.extension.rule.GlobalRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;

/**
 * @author : jiez
 * @date : 2021/7/24 16:35
 */
public class GlobalAuthorityRule extends AuthorityRule implements GlobalRule<AuthorityRule> {

    @Override
    public AuthorityRule toRule() {
        return null;
    }
}
