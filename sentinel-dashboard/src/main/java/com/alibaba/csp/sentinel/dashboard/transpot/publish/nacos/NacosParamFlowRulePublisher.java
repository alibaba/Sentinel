package com.alibaba.csp.sentinel.dashboard.transpot.publish.nacos;

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
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosParamFlowRulePublisher extends NacosPublishAdapter<ParamFlowRuleEntity> {
}
