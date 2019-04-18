package com.alibaba.csp.sentinel.dashboard.transpot.fetch.nacos;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch param flow rule by nacos
 *
 * @author longqiang
 */
@Component(Constants.PARAM_FLOW_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosParamFlowRuleFetcher extends NacosFetchAdapter<ParamFlowRuleEntity> {
}
