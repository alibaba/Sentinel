package com.alibaba.csp.sentinel.dashboard.config.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.adapter.DefaultFlowRuleDynamicRuleStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@Configuration
@ConditionalOnProperty(prefix = "rule.store", name = "type", havingValue = "default", matchIfMissing = true)
public class DefaultRuleConfiguration {

    @Bean("flowRuleDynamicRuleStore")
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore() {
        return new DefaultFlowRuleDynamicRuleStore();
    }

}
