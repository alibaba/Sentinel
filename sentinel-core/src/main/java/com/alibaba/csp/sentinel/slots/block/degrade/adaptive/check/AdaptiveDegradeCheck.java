package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

/**
 * Implementation of the adaptive circuit breaker algorithm. Used to calculate whether this request is allowed to pass.
 *
 * @author ylnxwlp
 */
public interface AdaptiveDegradeCheck {

    double getPassProbability(String resourceName, Scenario.SystemScenario scenario, WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow, List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows);

}
