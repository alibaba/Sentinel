package com.alibaba.csp.sentinel.dashboard.publish.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish system rules to Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemorySystemRulePublisher extends InMemoryPublishAdapter<SystemRuleEntity> {

    public InMemorySystemRulePublisher(InMemoryRuleRepositoryAdapter<SystemRuleEntity> repository, SentinelApiClient sentinelApiClient) {
        super(repository, sentinelApiClient);
    }

    @Override
    protected boolean publish(String app, String ip, int port, List<SystemRuleEntity> rules) {
        return sentinelApiClient.setSystemRuleOfMachine(app, ip, port, rules);
    }
}
