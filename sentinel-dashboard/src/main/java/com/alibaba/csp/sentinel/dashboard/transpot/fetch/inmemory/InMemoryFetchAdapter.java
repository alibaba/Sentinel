package com.alibaba.csp.sentinel.dashboard.transpot.fetch.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.transpot.fetch.Fetcher;

/**
 * Memory fetch rules adapter
 *
 * @author longqiang
 */
public abstract class InMemoryFetchAdapter<T extends RuleEntity> implements Fetcher<T> {

    protected SentinelApiClient sentinelApiClient;

    protected InMemoryFetchAdapter(SentinelApiClient sentinelApiClient) {
        this.sentinelApiClient = sentinelApiClient;
    }

}
