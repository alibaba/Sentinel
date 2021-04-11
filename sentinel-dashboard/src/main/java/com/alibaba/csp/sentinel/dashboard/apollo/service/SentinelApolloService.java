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
package com.alibaba.csp.sentinel.dashboard.apollo.service;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * sentinel dashboard call this service to modify rules of project.
 *
 * @author wxq
 */
public interface SentinelApolloService {

    /**
     * @throws ApolloOpenApiException if cannot create or publish namespace of project in apollo
     */
    void registryProjectIfNotExists(String projectName);

    /**
     * @return projects are saved in sentinel dashboard's storage
     */
    Set<String> getRegisteredProjects();

    /**
     * @return all projects in apollo
     */
    Set<String> getAllApps();

    /**
     * Find which project not exist in apollo.
     *
     * @return projects not exist in apollo
     */
    Set<String> getNotExistingProjectNames(Set<String> projectNames);

    /**
     * Delete projects are saved in sentinel dashboard's storage
     *
     * @return projects are deleted
     */
    Set<String> clearRegisteredProjects();

    /**
     * If sentinel dashboard cannot read project's config from apollo, sentinel dashboard will delete this project in self storage.
     *
     * @return projects are deleted
     */
    Set<String> clearCannotReadConfigProjects();

    /**
     * If sentinel dashboard cannot publish project's config to apollo, sentinel dashboard will delete this project in self storage.
     *
     * @return projects are deleted
     */
    Set<String> clearCannotPublishConfigProjects();

    /**
     * Traversal all projects in apollo, try to registry them by using {@link #registryProjectIfNotExists(String)}.
     *
     * @return projects are registered
     */
    Set<String> autoRegistryProjectsSkipFailed();

    /**
     * Asynchronous operation of {@link #autoRegistryProjectsSkipFailed()}.
     */
    CompletableFuture<Set<String>> autoRegistryProjectsSkipFailedAsync();

    /**
     * Registry projects which show in sentinel dashboard's sidebar
     *
     * @param jsessionid JSESSIONID in Cookie
     * @return key is project name, value is it registry successful or not
     */
    Map<String, Boolean> autoRegistryProjectsInSidebar(String jsessionid);

    CompletableFuture<Void> setRulesAsync(String projectName, RuleType ruleType, List<? extends Rule> rules);

    boolean setRules(String projectName, RuleType ruleType, List<? extends Rule> rules);

    void setRules(Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules);

    List<? extends Rule> getRules(String projectName, RuleType ruleType);

    Map<RuleType, List<? extends Rule>> getRules(String projectName);

    Map<String, Map<RuleType, List<? extends Rule>>> getRules(Set<String> projectNames);

    Map<String, Map<RuleType, List<? extends Rule>>> getRules();

}
