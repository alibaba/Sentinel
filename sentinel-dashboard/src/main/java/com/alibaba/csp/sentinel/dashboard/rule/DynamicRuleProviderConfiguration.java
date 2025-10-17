package com.alibaba.csp.sentinel.dashboard.rule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicRuleProviderConfiguration {
    @Bean("flowRuleDefaultProvider")
    @ConditionalOnMissingBean
    public FlowRuleApiProvider flowRuleApiProvider(){
        return new FlowRuleApiProvider();
    }
}
