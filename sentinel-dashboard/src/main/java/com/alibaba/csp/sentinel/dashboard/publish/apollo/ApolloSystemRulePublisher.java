package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish system rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloSystemRulePublisher extends ApolloPublishAdapter<SystemRuleEntity> {

    protected ApolloSystemRulePublisher(AppManagement appManagement, InMemoryRuleRepositoryAdapter<SystemRuleEntity> repository) {
        super(appManagement, repository);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getSystemRulesKey();
    }

    @Override
    protected String processRules(List<SystemRuleEntity> rules) {
        return JSON.toJSONString(rules);
    }
}
