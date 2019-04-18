package com.alibaba.csp.sentinel.dashboard.transpot.publish.inmemory;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * publish flow rules to Target Machines by http client
 *
 * @author longqiang
 */
@Component(Constants.FLOW_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryFlowRulePublisher extends InMemoryPublishAdapter<FlowRuleEntity> {

    public InMemoryFlowRulePublisher(InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository, SentinelApiClient sentinelApiClient) {
        super(repository, sentinelApiClient);
    }

    @Override
    protected boolean publish(String app, String ip, int port, List<FlowRuleEntity> rules) {
        return sentinelApiClient.setFlowRuleOfMachine(app, ip, port, rules);
    }
}
