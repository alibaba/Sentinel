package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * publish param flow rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloParamFlowRulePublisher extends ApolloPublishAdapter<ParamFlowRuleEntity> {

    protected ApolloParamFlowRulePublisher(AppManagement appManagement, InMemoryRuleRepositoryAdapter<ParamFlowRuleEntity> repository) {
        super(appManagement, repository);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getParamFlowRulesKey();
    }

    @Override
    protected String processRules(List<ParamFlowRuleEntity> rules) {
        return JSON.toJSONString(rules.stream().map(ParamFlowRuleEntity::toRule).collect(Collectors.toList()));
    }
}
