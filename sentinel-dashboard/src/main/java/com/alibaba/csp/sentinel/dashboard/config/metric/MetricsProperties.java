package com.alibaba.csp.sentinel.dashboard.config.metric;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@ConfigurationProperties(prefix = "metric")
public class MetricsProperties {

    private String storeType = "default";

    private Duration maxQueryIntervalTime = Duration.ofMinutes(30);

    private Duration maxLiveTime = Duration.ofMinutes(10);

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(final String storeType) {
        this.storeType = storeType;
    }

    public Duration getMaxQueryIntervalTime() {
        return maxQueryIntervalTime;
    }

    public void setMaxQueryIntervalTime(final Duration maxQueryIntervalTime) {
        this.maxQueryIntervalTime = maxQueryIntervalTime;
    }

    public Duration getMaxLiveTime() {
        return maxLiveTime;
    }

    public void setMaxLiveTime(final Duration maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }
}
