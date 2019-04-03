package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.util.RuleUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * fetch param flow rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloParamFlowRuleFetcher extends ApolloFetchAdapter<ParamFlowRuleEntity> {

    public ApolloParamFlowRuleFetcher(AppManagement appManagement) {
        super(appManagement);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getParamFlowRulesKey();
    }

    @Override
    protected List<ParamFlowRuleEntity> convert(String app, String ip, int port, String value) {
        return Optional.ofNullable(value)
                        .map(RuleUtils::parseParamFlowRule)
                        .map(rules -> rules.stream()
                                            .map(e -> ParamFlowRuleEntity.fromParamFlowRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }
}
