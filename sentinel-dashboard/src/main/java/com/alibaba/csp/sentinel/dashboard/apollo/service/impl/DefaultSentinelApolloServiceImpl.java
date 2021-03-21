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
package com.alibaba.csp.sentinel.dashboard.apollo.service.impl;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.cloud.sentinel.datasource.config.ApolloDataSourceProperties;
import com.alibaba.csp.sentinel.dashboard.apollo.config.SentinelApolloOpenApiProperties;
import com.alibaba.csp.sentinel.dashboard.apollo.config.SentinelApolloProperties;
import com.alibaba.csp.sentinel.dashboard.apollo.repository.project.ProjectRepository;
import com.alibaba.csp.sentinel.dashboard.apollo.service.SentinelApolloService;
import com.alibaba.csp.sentinel.dashboard.apollo.util.DataSourceConverterUtils;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Use private namespace to save rules.
 *
 * @author wxq
 */
public class DefaultSentinelApolloServiceImpl implements SentinelApolloService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSentinelApolloServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final String operatedUser;

    private final String operatedEnv;

    private final String operatedCluster;

    private final String namespaceName;

    private final SentinelApolloProperties sentinelApolloProperties;

    public DefaultSentinelApolloServiceImpl(
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            ApolloOpenApiClient apolloOpenApiClient,
            SentinelApolloProperties sentinelApolloProperties
    ) {
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.operatedUser = sentinelApolloOpenApiProperties.getOperatedUser();
        this.operatedEnv = sentinelApolloOpenApiProperties.getOperatedEnv();
        this.operatedCluster = sentinelApolloOpenApiProperties.getOperatedCluster();
        this.namespaceName = sentinelApolloProperties.getNamespaceName();
        this.sentinelApolloProperties = sentinelApolloProperties;
    }

    private static Map<String, String> toKeyValues(List<OpenItemDTO> openItemDTOS) {
        Map<String, String> map = new HashMap<>();
        for (OpenItemDTO openItemDTO : openItemDTOS) {
            String key = openItemDTO.getKey();
            String value = openItemDTO.getValue();
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

    @ExceptionHandler(ApolloOpenApiException.class)
    public void handleApolloOpenApiException(ApolloOpenApiException e) {
        final int status = e.getStatus();
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            logger.info("{}", HttpStatus.UNAUTHORIZED);
        }

        throw e;
    }

    /**
     * for {@link ApolloDataSourceProperties#setFlowRulesKey(java.lang.String)} used.
     */
    private String resolveFlowRulesKey(String projectName, RuleType ruleType) {
        String flowRulesKeySuffix = this.sentinelApolloProperties.getSuffix().get(ruleType);
        return projectName + flowRulesKeySuffix;
    }

    private void ensureClusterExists(String appId) {
        try {
            this.apolloOpenApiClient.getCluster(appId, this.operatedEnv, this.operatedCluster);
            return;
        } catch (RuntimeException e) {
            logger.info("app id [{}], env [{}], cluster [{}] not exists ", appId, this.operatedEnv, this.operatedCluster);
        }

        OpenClusterDTO openClusterDTO = new OpenClusterDTO();
        openClusterDTO.setAppId(appId);
        openClusterDTO.setName(this.operatedCluster);
        openClusterDTO.setDataChangeCreatedBy(this.operatedUser);
        this.apolloOpenApiClient.createCluster(this.operatedEnv, openClusterDTO);
    }

    private void createPrivateNamespace(String projectName, String privateNamespaceName) {
        final String appId = projectName;
        OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
        openAppNamespaceDTO.setName(privateNamespaceName);
        openAppNamespaceDTO.setAppId(appId);
        openAppNamespaceDTO.setDataChangeCreatedBy(this.operatedUser);
        openAppNamespaceDTO.setFormat(ConfigFileFormat.Properties.getValue());
        openAppNamespaceDTO.setComment("create by sentinel dashboard. use by app");
        this.apolloOpenApiClient.createAppNamespace(openAppNamespaceDTO);
    }

    private void publishPrivateNamespace(String projectName, String privateNamespaceName) {
        final String appId = projectName;
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleasedBy(this.operatedUser);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final String currentDateString = simpleDateFormat.format(new Date());
        namespaceReleaseDTO.setReleaseTitle("sentinel dashboard operate on " + currentDateString);
        this.apolloOpenApiClient.publishNamespace(appId, this.operatedEnv, this.operatedCluster, privateNamespaceName, namespaceReleaseDTO);
    }

    private void publishRulesOfProject(String projectName) {
        this.publishPrivateNamespace(projectName, this.namespaceName);
    }

    private boolean existsNamespace(String projectName) {
        final String appId = projectName;
        this.ensureClusterExists(appId);
        try {
            this.apolloOpenApiClient.getNamespace(appId, this.operatedEnv, this.operatedCluster, this.namespaceName);
            return true;
        } catch (RuntimeException e) {
            logger.warn("project [{}] not exists namespace [{}] in apollo", projectName, this.namespaceName);
            return false;
        }
    }

    @Override
    public void registryProjectIfNotExists(String projectName) {
        if (! this.projectRepository.exists(projectName)) {
            if (! this.existsNamespace(projectName)) {
                this.createPrivateNamespace(projectName, this.namespaceName);
                this.publishPrivateNamespace(projectName, this.namespaceName);
            }
            this.projectRepository.add(projectName);
        }
    }

    @Override
    public Set<String> getRegisteredProjects() {
//        return Collections.unmodifiableSet(this.registeredProjects.keySet());
        return Collections.unmodifiableSet(this.projectRepository.findAll());
    }

    @Override
    public Set<String> clearRegisteredProjects() {
        return this.projectRepository.deleteAll();
    }

    @Override
    public Set<String> clearCannotReadConfigProjects() {
        Set<String> allProjectNames = this.projectRepository.findAll();
        // TODO, change to parallel
        Set<String> exceptionProjectNames = new HashSet<>();
        for (String projectName : allProjectNames) {
            try {
                this.getRules(projectName);
            } catch (RuntimeException e) {
                logger.debug("cannot read project [{}]'s config", projectName);
                exceptionProjectNames.add(projectName);
            }
        }

        logger.info("those projects will be clear from storage because cannot read their config. {}", exceptionProjectNames);
        for (String exceptionProjectName : exceptionProjectNames) {
            this.projectRepository.delete(exceptionProjectName);
        }

        return Collections.unmodifiableSet(exceptionProjectNames);
    }

    @Override
    public Set<String> clearCannotPublishConfigProjects() {
        Set<String> allProjectNames = this.projectRepository.findAll();
        // TODO, change to parallel
        Set<String> exceptionProjectNames = new HashSet<>();
        for (String projectName : allProjectNames) {
            try {
                this.publishRulesOfProject(projectName);
            } catch (RuntimeException e) {
                logger.debug("cannot publish project [{}]'s config", projectName);
                exceptionProjectNames.add(projectName);
            }
        }

        logger.info("those projects will be clear from storage because cannot publish their config. {}", exceptionProjectNames);
        for (String exceptionProjectName : exceptionProjectNames) {
            this.projectRepository.delete(exceptionProjectName);
        }

        return Collections.unmodifiableSet(exceptionProjectNames);
    }

    private OpenItemDTO resolveOpenItemDTO(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        OpenItemDTO openItemDTO = new OpenItemDTO();
        final String ruleKey = this.resolveFlowRulesKey(projectName, ruleType);
        openItemDTO.setKey(ruleKey);

        // TODO, use json converter in spring-cloud-starter-alibaba-sentinel defined in SentinelConverterConfiguration?
        final String value = DataSourceConverterUtils.serializeToString(rules);

        openItemDTO.setValue(value);
        openItemDTO.setDataChangeCreatedBy(this.operatedUser);

        return openItemDTO;
    }

    @Override
    public CompletableFuture<Void> setRulesAsync(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        final String appId = projectName;
        this.registryProjectIfNotExists(projectName);
        OpenItemDTO openItemDTO = this.resolveOpenItemDTO(projectName, ruleType, rules);
        Runnable runnable = () -> {
            this.apolloOpenApiClient.createOrUpdateItem(appId, this.operatedEnv, this.operatedCluster, this.namespaceName, openItemDTO);
            this.publishPrivateNamespace(projectName, this.namespaceName);
        };

        return CompletableFuture.runAsync(runnable);
    }

    @Override
    public boolean setRules(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        CompletableFuture<Void> completableFuture = this.setRulesAsync(projectName, ruleType, rules);
        try {
            completableFuture.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("wait fail. setRules of project " + projectName, e);
        }

        return false;
    }

    private void setRules(String projectName, Map<RuleType, List<? extends Rule>> ruleTypeListMap) {
        // create or update config
        for (Map.Entry<RuleType, List<? extends Rule>> entry : ruleTypeListMap.entrySet()) {
            RuleType ruleType = entry.getKey();
            List<? extends Rule> rules = entry.getValue();
            this.setRules(projectName, ruleType, rules);
        }

        // publish config
        this.publishPrivateNamespace(projectName, this.namespaceName);
    }

    @Override
    public void setRules(Map<String, Map<RuleType, List<? extends Rule>>> projectName2rules) {
        for (Map.Entry<String, Map<RuleType, List<? extends Rule>>> entry : projectName2rules.entrySet()) {
            String projectName = entry.getKey();
            Map<RuleType, List<? extends Rule>> ruleTypeListMap = entry.getValue();
            // TODO, consider that parallel with each project
            this.setRules(projectName, ruleTypeListMap);
        }
    }

    @Override
    public List<? extends Rule> getRules(String projectName, RuleType ruleType) {
        Map<RuleType, List<? extends Rule>> ruleTypeListMap = this.getRules(projectName);
        return ruleTypeListMap.getOrDefault(ruleType, Collections.emptyList());
    }

    @Override
    public Map<RuleType, List<? extends Rule>> getRules(String projectName) {
        final String appId = projectName;
        OpenNamespaceDTO openNamespaceDTO = this.apolloOpenApiClient.getNamespace(appId, this.operatedEnv, this.operatedCluster, this.namespaceName);

        Map<RuleType, List<? extends Rule>> ruleTypeListMap = new HashMap<>();

        Map<String, String> keyValues = toKeyValues(openNamespaceDTO.getItems());

        for (RuleType ruleType : RuleType.values()) {
            String flowRulesKey = this.resolveFlowRulesKey(projectName, ruleType);
            if (keyValues.containsKey(flowRulesKey)) {
                List<? extends Rule> rules = DataSourceConverterUtils.deserialize(keyValues.get(flowRulesKey), ruleType);
                ruleTypeListMap.put(ruleType, rules);
            }
        }

        return Collections.unmodifiableMap(ruleTypeListMap);
    }

    @Override
    public Map<String, Map<RuleType, List<? extends Rule>>> getRules() {
        Set<String> projectNames = this.getRegisteredProjects();

        Map<String, Map<RuleType, List<? extends Rule>>> map = new HashMap<>();

        for (String projectName : projectNames) {
            map.put(projectName, this.getRules(projectName));
        }
        return Collections.unmodifiableMap(map);
    }

}
