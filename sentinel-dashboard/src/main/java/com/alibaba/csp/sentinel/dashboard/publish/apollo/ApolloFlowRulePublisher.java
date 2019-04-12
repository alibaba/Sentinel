package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * publish flow rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloFlowRulePublisher extends ApolloPublishAdapter<FlowRuleEntity> {

    protected ApolloFlowRulePublisher(AppManagement appManagement, InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository) {
        super(appManagement, repository);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getFlowRulesKey();
    }

    @Override
    protected String processRules(List<FlowRuleEntity> rules) {
        return JSON.toJSONString(rules.stream().map(FlowRuleEntity::toRule).collect(Collectors.toList()));
    }
}
