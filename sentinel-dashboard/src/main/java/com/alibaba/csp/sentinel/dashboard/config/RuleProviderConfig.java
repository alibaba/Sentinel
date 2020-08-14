package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.rule.FlowRuleApiProvider;
import com.alibaba.csp.sentinel.dashboard.rule.FlowRuleApiPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "rule",name = "provider",havingValue = "mem",matchIfMissing = true)
public class RuleProviderConfig {

    @Bean
    public FlowRuleApiProvider flowRuleProvider(){
       return new  FlowRuleApiProvider();
    }

    @Bean
    public FlowRuleApiPublisher flowRulePublisher(){
        return new FlowRuleApiPublisher();
    }
}
