package com.alibaba.csp.sentinel.dashboard.config.rule;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
public enum RuleStoreType {

    DEFAULT(DefaultRuleConfiguration.class),
    APOLLO(ApolloRuleConfiguration.class);

    private final Class<?> configurationClass;

    RuleStoreType(final Class<?> configurationClass) {
        this.configurationClass = configurationClass;
    }

    public Class<?> getConfigurationClass() {
        return configurationClass;
    }
}
