package com.alibaba.csp.sentinel.demo.adaptive.check;


import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveDegradeCheck;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check.AdaptiveDegradeCheckProvider;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;

/**
 * This demo demonstrates the acquisition of the SPI mechanism for the adaptive circuit breaker user-defined algorithm.
 *
 * @author ylnxwlp
 */
public class DemoAdaptiveCheckApplication {
    public static void main(String[] args) {
        AdaptiveDegradeCheck instance = AdaptiveDegradeCheckProvider.getInstance();
        System.out.println("The probability that a user-defined adaptive circuit breaker algorithm request can be processed is: " + instance.getPassProbability("test", Scenario.SystemScenario.OVER_LOAD, null, null));
    }
}
