package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish authority rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloAuthorityRulePublisher extends ApolloPublishAdapter<AuthorityRuleEntity> {

    protected ApolloAuthorityRulePublisher(AppManagement appManagement, InMemoryRuleRepositoryAdapter<AuthorityRuleEntity> repository) {
        super(appManagement, repository);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getAuthorityRulesKey();
    }

    @Override
    protected String processRules(List<AuthorityRuleEntity> rules) {
        return JSON.toJSONString(rules);
    }
}
