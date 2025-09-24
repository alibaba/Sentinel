package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

/**
 * Define the functions that the scene implementation class needs to have.
 *
 * @author ylnxwlp
 */
public interface Scenario {

    boolean matchScenario(String resourceName, WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                          List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows, AdaptiveServerMetric adaptiveServerMetric);

    SystemScenario getScenarioType();

    enum SystemScenario {

        NORMAL,

        OVER_LOAD;

        //TODO Integrate more scenarios
    }
}
