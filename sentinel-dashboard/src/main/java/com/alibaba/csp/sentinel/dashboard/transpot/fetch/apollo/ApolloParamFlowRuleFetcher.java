package com.alibaba.csp.sentinel.dashboard.transpot.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch param flow rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.PARAM_FLOW_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloParamFlowRuleFetcher extends ApolloFetchAdapter<ParamFlowRuleEntity> {
}
