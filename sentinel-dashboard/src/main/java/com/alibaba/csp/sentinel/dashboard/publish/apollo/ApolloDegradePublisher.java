package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish degrade rule by apollo
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "apollo")
public class ApolloDegradePublisher extends ApolloPublishAdapter<DegradeRuleEntity> {

    protected ApolloDegradePublisher(AppManagement appManagement, InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository) {
        super(appManagement, repository);
    }

    @Override
    protected String getKey(ApolloMachineInfo apolloMachineInfo) {
        return apolloMachineInfo.getDegradeRulesKey();
    }

    @Override
    protected String processRules(List<DegradeRuleEntity> rules) {
        return JSON.toJSONString(rules);
    }
}
