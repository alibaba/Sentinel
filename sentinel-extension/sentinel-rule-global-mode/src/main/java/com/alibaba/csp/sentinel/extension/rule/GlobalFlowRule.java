package com.alibaba.csp.sentinel.extension.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author : jiez
 * @date : 2021/7/17 10:00
 */
public class GlobalFlowRule extends FlowRule implements GlobalRule<FlowRule> {

    @Override
    public boolean globalRule() {
        return false;
    }

    @Override
    public FlowRule toRule() {
        return null;
    }
}
