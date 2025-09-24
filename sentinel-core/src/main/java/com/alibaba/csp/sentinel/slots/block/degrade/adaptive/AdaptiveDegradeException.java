package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * Exception for AdaptiveDegrade.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeException extends BlockException {

    public AdaptiveDegradeException() {
        super(null);
    }

    public AdaptiveDegradeException(DegradeRule rule) {
        super(null, rule);
    }

    public AdaptiveDegradeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdaptiveDegradeException(String message) {
        super(null, message);
    }

    public AdaptiveDegradeException(String message, AdaptiveDegradeRule rule) {
        super(null, message, rule);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public AdaptiveDegradeRule getRule() {
        return (AdaptiveDegradeRule) rule;
    }
}