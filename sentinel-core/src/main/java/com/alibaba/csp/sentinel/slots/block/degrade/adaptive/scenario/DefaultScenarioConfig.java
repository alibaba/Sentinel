package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

/**
 * Default scenario configuration.
 *
 * @author ylnxwlp
 */
public class DefaultScenarioConfig implements ScenarioConfig {

    public int getRecoveryTimeoutMs() {
        return 10000;
    }

    public long getHalfOpenTimeoutMs() {
        return 20000;
    }

    public double getOverloadCpuThreshold() {
        return 0.6;
    }

    public double getResponseTimeMultiple() {
        return 2;
    }

    public double getErrorRateMultiple() {
        return 5;
    }

    public double getTomcatUsageRate() {
        return 0.7;
    }
}
