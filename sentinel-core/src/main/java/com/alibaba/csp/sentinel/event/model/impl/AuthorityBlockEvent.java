package com.alibaba.csp.sentinel.event.model.impl;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

public class AuthorityBlockEvent extends SentinelRuleEvent {

    /**
     * flow source.
     */
    private String origin;

    public AuthorityBlockEvent(String origin, AbstractRule rule) {
        super(rule);
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
