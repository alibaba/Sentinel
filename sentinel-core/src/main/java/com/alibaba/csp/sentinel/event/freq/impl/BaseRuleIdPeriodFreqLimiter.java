package com.alibaba.csp.sentinel.event.freq.impl;

import com.alibaba.csp.sentinel.event.freq.PeriodFreqLimiter;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.model.impl.SentinelRuleEvent;

/**
 * Base on rule id to finish frequency limit, event should be class or subclass of {@link SentinelRuleEvent}.
 *
 * @author Daydreamer-ia
 */
public class BaseRuleIdPeriodFreqLimiter extends PeriodFreqLimiter {

    public BaseRuleIdPeriodFreqLimiter(long limitPeriod) {
        super(limitPeriod);
    }

    @Override
    protected String getLimitDimensionKey(SentinelEvent event) {
        if (event instanceof SentinelRuleEvent) {
            return ((SentinelRuleEvent) event).getRule().getId() + "";
        }
        return "unknown";
    }

}
