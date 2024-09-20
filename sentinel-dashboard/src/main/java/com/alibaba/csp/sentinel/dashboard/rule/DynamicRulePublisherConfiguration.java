package com.alibaba.csp.sentinel.dashboard.rule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicRulePublisherConfiguration {
    @Bean("flowRuleDefaultPublisher")
    @ConditionalOnMissingBean
    public FlowRuleApiPublisher flowRuleApiPublisher(){
        return new FlowRuleApiPublisher();
    }
}
