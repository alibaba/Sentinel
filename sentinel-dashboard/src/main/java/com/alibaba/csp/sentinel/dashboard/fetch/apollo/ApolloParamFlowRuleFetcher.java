package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
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
@Component(Constants.PARAM_FLOW_RULE_FETCHER)
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
                        .map(rules -> JSON.parseArray(rules, ParamFlowRule.class))
                        .map(rules -> rules.stream()
                                            .map(e -> ParamFlowRuleEntity.fromParamFlowRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }
}
