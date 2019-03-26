package com.alibaba.csp.sentinel.dashboard.publish.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish authority rules to Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryAuthorityRulePublisher extends InMemoryPublishAdapter<AuthorityRuleEntity> {

    public InMemoryAuthorityRulePublisher(InMemoryRuleRepositoryAdapter<AuthorityRuleEntity> repository, SentinelApiClient sentinelApiClient) {
        super(repository, sentinelApiClient);
    }

    @Override
    protected boolean publish(String app, String ip, int port, List<AuthorityRuleEntity> rules) {
        return sentinelApiClient.setAuthorityRuleOfMachine(app, ip, port, rules);
    }
}
