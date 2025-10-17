package com.alibaba.csp.sentinel.dashboard.repository.rule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleRepositoryConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public InMemAuthorityRuleStore inMemAuthorityRuleStore(){
        return new InMemAuthorityRuleStore();
    }
    @Bean
    @ConditionalOnMissingBean
    public InMemDegradeRuleStore inMemDegradeRuleStore(){
        return new InMemDegradeRuleStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public InMemFlowRuleStore inMemFlowRuleStore(){
        return new InMemFlowRuleStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public InMemParamFlowRuleStore inMemParamFlowRuleStore(){
        return new InMemParamFlowRuleStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public InMemSystemRuleStore inMemSystemRuleStore(){
        return new InMemSystemRuleStore();
    }
}
