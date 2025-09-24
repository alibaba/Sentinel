package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;

/**
 * Overload Scenario.
 *
 * @author ylnxwlp
 */
public class OverLoadScenario implements Scenario {

    private static final double MIN_BASELINE_ERROR_RATE = 0.001;

    @Override
    public boolean matchScenario(String resourceName, WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow, List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows, AdaptiveServerMetric adaptiveServerMetric) {
        OverloadScenarioConfig config = (OverloadScenarioConfig) ScenarioManager.getConfig(resourceName, SystemScenario.OVER_LOAD);
        if (windows.size() < 3) {
            return false;
        }
        long currentTotal = currentWindow.value().getTotalCount().sum();
        long currentErrors = currentWindow.value().getErrorCount().sum();
        long currentRT = currentWindow.value().getOverallRTTime().sum();

        double currentAvgRT = currentTotal > 0 ? (double) currentRT / currentTotal : 0;
        double currentErrorRate = currentTotal > 0 ? (double) currentErrors / currentTotal : 0;

        long totalRequests = 0;
        long totalErrors = 0;
        long totalRT = 0;

        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> window : windows) {
            AdaptiveCircuitBreaker.AdaptiveCounter counter = window.value();
            totalRequests += counter.getTotalCount().sum();
            totalErrors += counter.getErrorCount().sum();
            totalRT += counter.getOverallRTTime().sum();
        }

        totalRequests -= currentTotal;
        totalErrors -= currentErrors;
        totalRT -= currentRT;

        double avgRT = totalRequests > 0 ? (double) totalRT / totalRequests : 0;
        double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests : 0;

        double baselineErrorRate = Math.max(MIN_BASELINE_ERROR_RATE, errorRate);
        boolean rtCondition = currentAvgRT > config.getResponseTimeMultiple() * avgRT;
        boolean errorCondition = currentErrorRate > config.getErrorRateMultiple() * baselineErrorRate;

        if (rtCondition || errorCondition) {
            double serverCpuUsage = adaptiveServerMetric.getServerCpuUsage();
            double serveTomcatUsageRate = adaptiveServerMetric.getServerTomcatUsageRate();
            if (serverCpuUsage <= 0 && serveTomcatUsageRate <= 0) {
                return false;
            }
            boolean cpuCondition = serverCpuUsage > 0 && serverCpuUsage > config.getOverloadCpuThreshold();
            boolean tomcatCondition = serveTomcatUsageRate > 0 && serveTomcatUsageRate > config.getTomcatUsageRate();
            return cpuCondition || tomcatCondition;
        }
        return false;
    }

    @Override
    public SystemScenario getScenarioType() {
        return SystemScenario.OVER_LOAD;
    }
}