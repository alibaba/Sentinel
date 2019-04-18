package com.alibaba.csp.sentinel.dashboard.transpot.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * publish flow rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.FLOW_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloFlowRulePublisher extends ApolloPublishAdapter<FlowRuleEntity> {
}
