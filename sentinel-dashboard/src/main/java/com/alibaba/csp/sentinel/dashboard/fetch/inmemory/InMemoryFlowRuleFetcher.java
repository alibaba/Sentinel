package com.alibaba.csp.sentinel.dashboard.fetch.inmemory;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Obtaining flow rules from Target Machines by http client
 *
 * @author longqiang
 */
@Component(Constants.FLOW_RULE_FETCHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryFlowRuleFetcher extends InMemoryFetchAdapter<FlowRuleEntity> {

    public InMemoryFlowRuleFetcher(SentinelApiClient sentinelApiClient) {
        super(sentinelApiClient);
    }

    @Override
    public List<FlowRuleEntity> fetch(String app, String ip, int port) {
        return sentinelApiClient.fetchFlowRuleOfMachine(app, ip, port);
    }

}
