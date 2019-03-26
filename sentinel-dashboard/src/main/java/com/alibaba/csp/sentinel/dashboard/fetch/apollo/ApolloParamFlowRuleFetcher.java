package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

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
    protected List<ParamFlowRuleEntity> convert(String value) {
        return JSON.parseObject(value, new TypeReference<List<ParamFlowRuleEntity>>() {});
    }
}
