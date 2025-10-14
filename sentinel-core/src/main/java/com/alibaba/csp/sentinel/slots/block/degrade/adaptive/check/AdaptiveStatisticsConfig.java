package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * The adaptive fault-tolerance statistics-related configuration class determines the statistical period of the sliding window and the total number of windows.
 * The user-defined configuration file path can be obtained by reading the configuration file, JVM startup parameters, and environment variables.
 *
 * @author ylnxwlp
 */
public class AdaptiveStatisticsConfig {

    private static final AdaptiveStatisticsConfig INSTANCE = new AdaptiveStatisticsConfig();

    private int sampleCount = 20;

    private int intervalInMs = 20000;

    public static final String SAMPLE_COUNT = "csp.sentinel.adaptive.statistics.sampleCount";
    public static final String INTERVAL_IN_MS = "csp.sentinel.adaptive.statistics.interval";

    private AdaptiveStatisticsConfig() {
        loadConfig();
    }

    public static AdaptiveStatisticsConfig getInstance() {
        return INSTANCE;
    }

    public void loadConfig() {
        RecordLog.info("[AdaptiveStatisticsConfig] Starting to load user configuration");

        String sampleCountStr = SentinelConfig.getConfig(SAMPLE_COUNT);
        if (sampleCountStr != null && !sampleCountStr.isEmpty()) {
            sampleCount = Integer.parseInt(sampleCountStr);
        }

        String intervalInMsStr = SentinelConfig.getConfig(INTERVAL_IN_MS);
        if (intervalInMsStr != null && !intervalInMsStr.isEmpty()) {
            intervalInMs = Integer.parseInt(intervalInMsStr);
        }

        RecordLog.info("[AdaptiveStatisticsConfig] User configuration has been loaded successfully , current configuration: sampleCount:{},intervalInMs:{}", sampleCount, intervalInMs);
    }


    public int getSampleCount() {
        return sampleCount;
    }

    public int getIntervalInMs() {
        return intervalInMs;
    }
}