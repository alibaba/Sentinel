package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DegradeRuleApiProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DegradeRuleApiPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.FlowRuleApiProvider;
import com.alibaba.csp.sentinel.dashboard.rule.FlowRuleApiPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author wuwen
 */
public abstract class DynamicRuleConfiguration {

    /**
     * Sentinel Api configuration.
     */
    @Configuration
    @ConditionalOnProperty(name = "sentinel.dashboard.dynamic_rule", havingValue = "api",
            matchIfMissing = true)
    static class SentinelApi {

        @Bean
        DynamicRuleProvider<List<FlowRuleEntity>> flowRuleProvider() {
            return new FlowRuleApiProvider();
        }

        @Bean
        DynamicRuleProvider<List<DegradeRuleEntity>> degradeRuleProvider() {
            return new DegradeRuleApiProvider();
        }

        @Bean
        DynamicRulePublisher<List<FlowRuleEntity>> flowRulePublisher() {
            return new FlowRuleApiPublisher();
        }

        @Bean
        DynamicRulePublisher<List<DegradeRuleEntity>> degradeRuleApiPublisher() {
            return new DegradeRuleApiPublisher();
        }
    }

}
