package com.alibaba.csp.sentinel.extension.rule.flow;

import com.alibaba.csp.sentinel.extension.rule.GlobalRule;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author : jiez
 * @date : 2021/7/17 10:00
 */
public class GlobalFlowRule extends FlowRule implements GlobalRule<FlowRule> {

    @Override
    public FlowRule toRule() {
        return null;
    }
}
