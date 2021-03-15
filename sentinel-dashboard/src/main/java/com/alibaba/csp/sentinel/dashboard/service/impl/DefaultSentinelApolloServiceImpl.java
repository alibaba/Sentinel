package com.alibaba.csp.sentinel.dashboard.service.impl;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.dashboard.service.SentinelApolloService;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultSentinelApolloServiceImpl implements SentinelApolloService {

    private final ApolloOpenApiClient apolloOpenApiClient;

    public DefaultSentinelApolloServiceImpl(ApolloOpenApiClient apolloOpenApiClient) {
        this.apolloOpenApiClient = apolloOpenApiClient;
    }

    @Override
    public boolean registryProjectIfNotExists(String projectName) {
        return false;
    }

    @Override
    public CompletableFuture<Void> setRulesAsync(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        return null;
    }

    @Override
    public boolean setRules(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        return false;
    }

    @Override
    public void setRules(Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules) {

    }

    @Override
    public Map<RuleType, List<? extends Rule>> getRules(String projectName) {
        return null;
    }

    @Override
    public Map<String, Map<RuleType, List<? extends Rule>>> getRules() {
        return null;
    }
}
