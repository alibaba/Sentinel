package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.rule.*;
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

    @Bean
    public DegradeRuleApiProvider degradeRuleProvider(){
        return new DegradeRuleApiProvider();
    }

    @Bean
    public DegradeRuleApiPublisher degradeRulePublisher(){
        return new DegradeRuleApiPublisher();
    }

    @Bean
    public ParamFlowRuleApiProvider paramFlowRuleProvider(){
        return new ParamFlowRuleApiProvider();
    }

    @Bean
    public ParamFlowRuleApiPublisher paramFlowRulePublisher(){
        return new ParamFlowRuleApiPublisher();
    }

    @Bean
    public SystemRuleApiProvider systemRuleProvider(){
        return new SystemRuleApiProvider();
    }

    @Bean
    public SystemRuleApiPublisher systemRulePublisher(){
        return new SystemRuleApiPublisher();
    }

    @Bean
    public AuthorityRuleApiProvider authorityRuleProvider(){
        return new AuthorityRuleApiProvider();
    }

    @Bean
    public AuthorityRuleApiPublisher authorityRulePublisher(){
        return new AuthorityRuleApiPublisher();
    }

}
