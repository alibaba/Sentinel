package com.alibaba.csp.sentinel.event.model.impl;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * Base event contains sentinel rule.
 *
 * @author Daydreamer-ia
 */
public class SentinelRuleEvent extends SentinelEvent {

    /**
     * sentinel rule.
     */
    private AbstractRule rule;

    public SentinelRuleEvent(AbstractRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule cannot be null to create SentinelRuleEvent");
        }
        this.rule = rule;
    }

    public AbstractRule getRule() {
        return rule;
    }

    public void setRule(AbstractRule rule) {
        this.rule = rule;
    }

}
