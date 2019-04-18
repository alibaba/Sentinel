package com.alibaba.csp.sentinel.dashboard.transpot.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * publish authority rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.AUTHORITY_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloAuthorityRulePublisher extends ApolloPublishAdapter<AuthorityRuleEntity> {
}
