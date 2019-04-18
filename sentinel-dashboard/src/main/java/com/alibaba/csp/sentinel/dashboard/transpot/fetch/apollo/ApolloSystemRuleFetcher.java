package com.alibaba.csp.sentinel.dashboard.transpot.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch system rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.SYSTEM_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloSystemRuleFetcher extends ApolloFetchAdapter<SystemRuleEntity> {
}
