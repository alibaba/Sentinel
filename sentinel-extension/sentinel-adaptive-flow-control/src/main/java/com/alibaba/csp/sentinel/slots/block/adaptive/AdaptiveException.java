package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author Liu Yiming
 * @date 2019-07-16 21:05
 */
public class AdaptiveException extends BlockException {

    public AdaptiveException(String ruleLimitApp) { super(ruleLimitApp); }

    public AdaptiveException(String ruleLimitApp, AdaptiveRule rule) {
        super(ruleLimitApp, rule);
    }

    public AdaptiveException(String message, Throwable cause) { super(message, cause); }

    public AdaptiveException(String ruleLimitApp, String message) { super(ruleLimitApp, message); }

    @Override
    public Throwable fillInStackTrace() { return this; }

    /**
     * Get triggered rule.
     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.
     *
     * @return triggered rule
     * @since 1.4.2
     */
    @Override
    public AdaptiveRule getRule() { return rule.as(AdaptiveRule.class); }
}
