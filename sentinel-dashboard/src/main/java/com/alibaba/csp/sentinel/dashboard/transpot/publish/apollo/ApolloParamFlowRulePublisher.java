package com.alibaba.csp.sentinel.dashboard.transpot.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * publish param flow rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.PARAM_FLOW_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloParamFlowRulePublisher extends ApolloPublishAdapter<ParamFlowRuleEntity> {
}
