package com.alibaba.csp.sentinel.extension.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author : jiez
 * @date : 2021/7/17 9:58
 */
public interface GlobalRule<T extends Rule> {

    /**
     * judge is global rule
     * @return is global rule
     */
    boolean globalRule();

    /**
     * transfer to ordinary rule
     *
     * @return <T> some class extend from rule
     */
    T toRule();
}
