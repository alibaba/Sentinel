package com.alibaba.csp.sentinel.dashboard.transpot.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch authority rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.AUTHORITY_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloAuthorityRuleFetcher extends ApolloFetchAdapter<AuthorityRuleEntity> {
}
