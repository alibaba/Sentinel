package com.alibaba.csp.sentinel.dashboard.rule.type.api.publisher;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.type.api.ApiConfig;
import com.alibaba.csp.sentinel.dashboard.rule.type.api.ApiRulePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(ApiConfig.class)
@Component
//public class ApiFlowRulePublisher extends ApiRulePublisher<FlowRuleEntity> {
public class ApiFlowRulePublisher extends ApiRulePublisher {

}
