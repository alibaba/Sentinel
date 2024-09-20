package com.alibaba.csp.sentinel.dashboard.config.metric;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
public enum MetricStoreType {

    DEFAULT(DefaultMetricConfiguration.class),
    REDIS(RedisMetricConfiguration.class);

    private final Class<?> configurationClass;


    MetricStoreType(final Class<?> configurationClass) {
        this.configurationClass = configurationClass;
    }

    public Class<?> getConfigurationClass() {
        return configurationClass;
    }
}
