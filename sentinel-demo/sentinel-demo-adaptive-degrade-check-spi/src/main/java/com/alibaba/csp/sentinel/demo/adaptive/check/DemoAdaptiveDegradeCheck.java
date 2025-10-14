package com.alibaba.csp.sentinel.demo.adaptive.check;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AbstractAdaptiveDegradeCheck;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

/**
 * This class demonstrates a user-defined adaptive circuit breaker algorithm.
 *
 * @author ylnxwlp
 */
public class DemoAdaptiveDegradeCheck extends AbstractAdaptiveDegradeCheck {

    @Override
    protected double getPassProbabilityWhenOverloading(String resourceName,WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow, List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        System.out.println("This is a user-defined implementation of the adaptive circuit breaker algorithm...");
        return 0.5201314;
    }
}
