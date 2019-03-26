package com.alibaba.csp.sentinel.dashboard.fetch.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Obtaining degrade rules from Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryDegradeFetcher extends InMemoryFetchAdapter<DegradeRuleEntity> {

    public InMemoryDegradeFetcher(SentinelApiClient sentinelApiClient) {
        super(sentinelApiClient);
    }

    @Override
    public List<DegradeRuleEntity> fetch(String app, String ip, int port) {
        return sentinelApiClient.fetchDegradeRuleOfMachine(app, ip, port);
    }
}
