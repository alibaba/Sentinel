package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * fetch authority rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.AUTHORITY_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloAuthorityRuleFetcher extends ApolloFetchAdapter<AuthorityRuleEntity> {

    public ApolloAuthorityRuleFetcher(AppManagement appManagement) {
        super(appManagement);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getAuthorityRulesKey();
    }

    @Override
    protected List<AuthorityRuleEntity> convert(String app, String ip, int port, String value) {
        return Optional.ofNullable(value)
                        .map(rules -> JSON.parseArray(rules, AuthorityRule.class))
                        .map(rules -> rules.stream()
                                            .map(e -> AuthorityRuleEntity.fromAuthorityRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }
}
