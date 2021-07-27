package com.alibaba.csp.sentinel.extension.global.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author : jiez
 * @date : 2021/7/17 9:58
 */
public interface GlobalRule<T extends Rule> {

    /**
     * transfer to ordinary rule
     *
     * @return <T> some class extend from rule
     */
    T toRule();
}
