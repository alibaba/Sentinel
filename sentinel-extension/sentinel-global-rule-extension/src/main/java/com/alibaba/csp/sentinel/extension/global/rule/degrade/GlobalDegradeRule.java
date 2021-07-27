package com.alibaba.csp.sentinel.extension.global.rule.degrade;

import com.alibaba.csp.sentinel.extension.global.rule.GlobalRule;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * @author : jiez
 * @date : 2021/7/26 9:44
 */
public class GlobalDegradeRule extends DegradeRule implements GlobalRule {

    @Override
    public Rule toRule() {
        return null;
    }
}
