package com.alibaba.csp.sentinel.dashboard.publish.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish degrade rules to Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryDegradePublisher extends InMemoryPublishAdapter<DegradeRuleEntity> {

    public InMemoryDegradePublisher(InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository, SentinelApiClient sentinelApiClient) {
        super(repository, sentinelApiClient);
    }

    @Override
    protected boolean publish(String app, String ip, int port, List<DegradeRuleEntity> rules) {
        return sentinelApiClient.setDegradeRuleOfMachine(app, ip, port, rules);
    }
}
