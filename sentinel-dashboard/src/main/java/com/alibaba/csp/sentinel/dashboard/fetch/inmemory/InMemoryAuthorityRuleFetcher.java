package com.alibaba.csp.sentinel.dashboard.fetch.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Obtaining authority rules from Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryAuthorityRuleFetcher extends InMemoryFetchAdapter<AuthorityRuleEntity> {

    public InMemoryAuthorityRuleFetcher(SentinelApiClient sentinelApiClient) {
        super(sentinelApiClient);
    }

    @Override
    public List<AuthorityRuleEntity> fetch(String app, String ip, int port) {
        return sentinelApiClient.fetchAuthorityRulesOfMachine(app, ip, port);
    }
}
