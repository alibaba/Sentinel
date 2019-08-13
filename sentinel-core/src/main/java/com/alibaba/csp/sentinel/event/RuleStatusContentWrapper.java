package com.alibaba.csp.sentinel.event;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class RuleStatusContentWrapper<T extends AbstractRule> {

    public RuleStatusContentWrapper(RuleStatus status, T rule) {
        this.status = status;
        this.rule = rule;
    }

    private RuleStatus status;

    private T rule;


    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public T getRule() {
        return rule;
    }

    public void setRule(T rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "{" +
                "status=" + status.name() +
                ",rule=" + rule +
                "}";
    }
}
