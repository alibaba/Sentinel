package com.taobao.csp.sentinel.dashboard.rule.api;

import com.taobao.csp.sentinel.dashboard.client.SentinelApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by YL on 2018/12/27
 */
@Configuration
@ConditionalOnProperty(name = "csp.sentinel.dashboard.dynamic-rule.type", matchIfMissing = true, havingValue = "api")
@ConditionalOnBean(SentinelApiClient.class)
public class ApiDynamicRuleAutoConfiguration {

    @Configuration
    class FlowRuleConfiguration {
        @Bean
        public FlowRuleApiProvider flowRuleApiProvider() {
            return new FlowRuleApiProvider();
        }

        @Bean
        public FlowRuleApiPublisher flowRuleApiPublisher() {
            return new FlowRuleApiPublisher();
        }
    }

    @Configuration
    class AuthorityRuleConfiguration {
        @Bean
        public AuthorityRuleApiProvider authorityRuleApiProvider() {
            return new AuthorityRuleApiProvider();
        }

        @Bean
        public AuthorityRuleApiPublisher authorityRuleApiPublisher() {
            return new AuthorityRuleApiPublisher();
        }
    }

    @Configuration
    class DegradeRuleConfiguration {
        @Bean
        public DegradeRuleApiProvider degradeRuleApiProvider() {
            return new DegradeRuleApiProvider();
        }

        @Bean
        public DegradeRuleApiPublisher degradeRuleApiPublisher() {
            return new DegradeRuleApiPublisher();
        }
    }

    @Configuration
    class ParamFlowRuleConfiguration {
        @Bean
        public ParamFlowRuleApiProvider paramFlowRuleApiProvider() {
            return new ParamFlowRuleApiProvider();
        }

        @Bean
        public ParamFlowRuleApiPublisher paramFlowRuleApiPublisher() {
            return new ParamFlowRuleApiPublisher();
        }
    }

    @Configuration
    class SystemRuleRuleConfiguration {
        @Bean
        public SystemRuleApiProvider systemRuleApiProvider() {
            return new SystemRuleApiProvider();
        }

        @Bean
        public SystemRuleApiPublisher systemRuleApiPublisher() {
            return new SystemRuleApiPublisher();
        }
    }
}
