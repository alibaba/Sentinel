package com.alibaba.csp.sentinel.event.freq;

import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;

public class ParamFlowEventPeriodFreqLimiter extends PeriodFreqLimiter {

    public ParamFlowEventPeriodFreqLimiter(long limitPeriod) {
        super(limitPeriod);
    }

    @Override
    protected String getLimitDimensionKey(SentinelEvent event) {
        if (event instanceof ParamFlowBlockEvent) {
            ParamFlowBlockEvent paramFlowBlockEvent = (ParamFlowBlockEvent) event;
            return paramFlowBlockEvent.getResourceName() + ":" + paramFlowBlockEvent.getRule().getId();
        }
        // degrade to global event.
        return "unknown";
    }
}
