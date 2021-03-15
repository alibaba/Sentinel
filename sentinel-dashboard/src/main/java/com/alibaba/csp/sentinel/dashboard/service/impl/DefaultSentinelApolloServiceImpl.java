/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
