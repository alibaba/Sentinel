package com.alibaba.csp.sentinel.dashboard.transpot.fetch.nacos;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * fetch system rule by nacos
 *
 * @author longqiang
 */
@Component(Constants.SYSTEM_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosSystemRuleFetcher extends NacosFetchAdapter<SystemRuleEntity> {
}
