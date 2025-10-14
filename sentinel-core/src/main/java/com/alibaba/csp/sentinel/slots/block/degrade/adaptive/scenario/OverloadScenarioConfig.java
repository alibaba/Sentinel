package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * The threshold configuration relied upon for the specific scenario judgment of overload.
 * The user profile can be read from the configuration file, JVM startup parameters or environment variables.
 *
 * @author ylnxwlp
 */
public class OverloadScenarioConfig implements ScenarioConfig {

    private final String resourceName;

    private int recoveryTimeoutMs = 10000;
    private long halfOpenTimeoutMs = 20000;
    private double overloadCpuThreshold = 0.6;
    private double responseTimeMultiple = 2;
    private double errorRateMultiple = 5;
    private double tomcatUsageRate = 0.7;

    public final String RECOVERY_TIMEOUT_MS;
    public final String HALFOPEN_TIMEOUT_MS;
    public final String OVERLOAD_CPU_THRESHOLD;
    public final String RESPONSE_TIME_MULTIPLE;
    public final String ERROR_RATE_MULTIPLE;
    public final String TOMCAT_USAGE_RATE;

    public OverloadScenarioConfig(String resourceName) {
        this.resourceName = resourceName;
        this.RECOVERY_TIMEOUT_MS = "csp.sentinel.adaptive.overload." + resourceName + ".recovery";
        this.HALFOPEN_TIMEOUT_MS = "csp.sentinel.adaptive.overload." + resourceName + ".halfopen";
        this.OVERLOAD_CPU_THRESHOLD = "csp.sentinel.adaptive.overload." + resourceName + ".cpu";
        this.RESPONSE_TIME_MULTIPLE = "csp.sentinel.adaptive.overload." + resourceName + ".rt";
        this.ERROR_RATE_MULTIPLE = "csp.sentinel.adaptive.overload." + resourceName + ".error";
        this.TOMCAT_USAGE_RATE = "csp.sentinel.adaptive.overload." + resourceName + ".tomcat.usage";
        loadConfig();
    }

    public void loadConfig() {
        RecordLog.debug("[OverloadScenarioConfig] Starting to load user configuration");

        String recoveryTimeoutStr = SentinelConfig.getConfig(RECOVERY_TIMEOUT_MS);
        if (recoveryTimeoutStr != null && !recoveryTimeoutStr.isEmpty()) {
            recoveryTimeoutMs = Integer.parseInt(recoveryTimeoutStr);
        }

        String halfOpenTimeoutStr = SentinelConfig.getConfig(HALFOPEN_TIMEOUT_MS);
        if (halfOpenTimeoutStr != null && !halfOpenTimeoutStr.isEmpty()) {
            halfOpenTimeoutMs = Long.parseLong(halfOpenTimeoutStr);
        }

        String cpuThresholdStr = SentinelConfig.getConfig(OVERLOAD_CPU_THRESHOLD);
        if (cpuThresholdStr != null && !cpuThresholdStr.isEmpty()) {
            overloadCpuThreshold = Double.parseDouble(cpuThresholdStr);
        }

        String rtMultipleStr = SentinelConfig.getConfig(RESPONSE_TIME_MULTIPLE);
        if (rtMultipleStr != null && !rtMultipleStr.isEmpty()) {
            responseTimeMultiple = Double.parseDouble(rtMultipleStr);
        }

        String errorRateMultipleStr = SentinelConfig.getConfig(ERROR_RATE_MULTIPLE);
        if (errorRateMultipleStr != null && !errorRateMultipleStr.isEmpty()) {
            errorRateMultiple = Double.parseDouble(errorRateMultipleStr);
        }

        String tomcatUsageRateStr = SentinelConfig.getConfig(TOMCAT_USAGE_RATE);
        if (tomcatUsageRateStr != null && !tomcatUsageRateStr.isEmpty()) {
            tomcatUsageRate = Double.parseDouble(tomcatUsageRateStr);
        }

        RecordLog.debug("[OverloadScenarioConfig] User configuration has been loaded successfully, current configuration: " +
                        "recoveryTimeoutMs:{}, halfOpenTimeoutMs:{}, overloadCpuThreshold:{}, responseTimeMultiple:{}, errorRateMultiple:{}, tomcatUsageRate:{}",
                recoveryTimeoutMs, halfOpenTimeoutMs, overloadCpuThreshold, responseTimeMultiple, errorRateMultiple, tomcatUsageRate);
    }

    public double getOverloadCpuThreshold() {
        return overloadCpuThreshold;
    }

    public void setOverloadCpuThreshold(double overloadCpuThreshold) {
        this.overloadCpuThreshold = overloadCpuThreshold;
    }

    public int getRecoveryTimeoutMs() {
        return recoveryTimeoutMs;
    }

    public void setRecoveryTimeoutMs(int recoveryTimeoutMs) {
        this.recoveryTimeoutMs = recoveryTimeoutMs;
    }

    public double getResponseTimeMultiple() {
        return responseTimeMultiple;
    }

    public void setResponseTimeMultiple(double responseTimeMultiple) {
        this.responseTimeMultiple = responseTimeMultiple;
    }

    public double getErrorRateMultiple() {
        return errorRateMultiple;
    }

    public void setErrorRateMultiple(double errorRateMultiple) {
        this.errorRateMultiple = errorRateMultiple;
    }

    public long getHalfOpenTimeoutMs() {
        return halfOpenTimeoutMs;
    }

    public void setHalfOpenTimeoutMs(long halfOpenTimeoutMs) {
        this.halfOpenTimeoutMs = halfOpenTimeoutMs;
    }

    public double getTomcatUsageRate() {
        return tomcatUsageRate;
    }

    public void setTomcatUsageRate(double tomcatUsageRate) {
        this.tomcatUsageRate = tomcatUsageRate;
    }
}