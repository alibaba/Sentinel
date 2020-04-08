package com.alibaba.csp.sentinel.dashboard.rule.type.api.provider;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.type.api.ApiConfig;
import com.alibaba.csp.sentinel.dashboard.rule.type.api.ApiRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
//@ConditionalOnMissingBean(DynamicRuleProvider.class)
//@ConditionalOnMissingBean
@ConditionalOnBean(ApiConfig.class)
@Component
//public class ApiFlowRuleProvider extends ApiRuleProvider<FlowRuleEntity> {
public class ApiFlowRuleProvider extends ApiRuleProvider {
    public ApiFlowRuleProvider() {
        System.out.println("ApiFlowRuleProvider");
    }
}
