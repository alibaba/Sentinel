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
package com.alibaba.csp.sentinel.dashboard.service;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * sentinel dashboard call this service to modify rules of project.
 * @author wxq
 */
public interface SentinelApolloService {

    boolean registryProjectIfNotExists(String projectName);

    CompletableFuture<Void> setRulesAsync(String projectName, RuleType ruleType, List<? extends Rule> rules);

    boolean setRules(String projectName, RuleType ruleType, List<? extends Rule> rules);

    void setRules(Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules);

    Map<RuleType, List<? extends Rule>> getRules(String projectName);

    Map<String, Map<RuleType, List<? extends Rule>>> getRules();

}
