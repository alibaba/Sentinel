package com.alibaba.csp.sentinel.dashboard.transpot.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * publish degrade rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.DEGRADE_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloDegradeRulePublisher extends ApolloPublishAdapter<DegradeRuleEntity> {
}
