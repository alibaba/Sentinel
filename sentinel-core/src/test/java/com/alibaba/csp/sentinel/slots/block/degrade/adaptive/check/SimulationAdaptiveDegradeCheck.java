package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

public class SimulationAdaptiveDegradeCheck implements AdaptiveDegradeCheck {
    @Override
    public double getPassProbability(String resourceName,
                                     Scenario.SystemScenario scenario,
                                     WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                                     List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
        return 0.123;
    }
}
