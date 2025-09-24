package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

/**
 * Provide a template implementation for the adaptive circuit breaker algorithm.
 *
 * @author ylnxwlp
 */
public abstract class AbstractAdaptiveDegradeCheck implements AdaptiveDegradeCheck {

    @Override
    public final double getPassProbability(String resourceName, Scenario.SystemScenario scenario, WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                                           List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        switch (scenario) {
            case OVER_LOAD:
                return getPassProbabilityWhenOverloading(resourceName, currentWindow, windows);
            default:
                RecordLog.warn("[AbstractAdaptiveDegradeCheck] The invalid scenarios are used to calculate the probability of the request passing.");
                return -1;
        }
    }

    protected double getPassProbabilityWhenOverloading(
            String resourceName,
            WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
            List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        return AdaptiveUtils.getPassProbabilityWhenOverloading(resourceName, currentWindow, windows);
    }
}