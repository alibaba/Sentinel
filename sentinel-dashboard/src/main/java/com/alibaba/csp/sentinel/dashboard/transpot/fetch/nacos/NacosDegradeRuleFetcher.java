package com.alibaba.csp.sentinel.dashboard.transpot.fetch.nacos;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch degrade rule by nacos
 *
 * @author longqiang
 */
@Component(Constants.DEGRADE_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosDegradeRuleFetcher extends NacosFetchAdapter<DegradeRuleEntity> {
}
