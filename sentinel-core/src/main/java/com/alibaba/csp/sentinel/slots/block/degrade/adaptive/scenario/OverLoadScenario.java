package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

public class OverLoadScenario implements Scenario {

    @Override
    public boolean matchScenario(String resourceName, WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow, List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows, AdaptiveServerMetric adaptiveServerMetric) {
        OverloadScenarioConfig config = (OverloadScenarioConfig) ScenarioManager.getConfig(resourceName, SystemScenario.OVER_LOAD);
        return AdaptiveUtils.isOverloadScenarioMatched(currentWindow, windows, adaptiveServerMetric, config);
    }

    @Override
    public SystemScenario getScenarioType() {
        return SystemScenario.OVER_LOAD;
    }
}