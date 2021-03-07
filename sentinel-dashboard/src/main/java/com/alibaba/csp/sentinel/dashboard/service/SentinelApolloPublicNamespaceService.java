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
import com.alibaba.csp.sentinel.dashboard.config.SentinelApolloOpenApiProperties;
import com.alibaba.csp.sentinel.dashboard.config.SentinelApolloPrivateConfiguration;
import com.alibaba.csp.sentinel.dashboard.config.SentinelApolloPublicProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SentinelApolloPublicNamespaceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String appId;

    private final String env;

    private final String clusterName;

    private final String operateUser;

    private final SentinelApolloPublicProperties sentinelApolloPublicProperties;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final Map<String, Object> publicNamespaceNames = new ConcurrentHashMap<>();

    public SentinelApolloPublicNamespaceService(
            SentinelApolloPrivateConfiguration sentinelApolloPrivateConfiguration,
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties,
            SentinelApolloPublicProperties sentinelApolloPublicProperties, ApolloOpenApiClient apolloOpenApiClient) {
        this.appId = sentinelApolloPrivateConfiguration.getAppId();
        this.env = sentinelApolloPrivateConfiguration.getEnv();
        this.clusterName = sentinelApolloPrivateConfiguration.getClusterName();
        this.operateUser = sentinelApolloOpenApiProperties.getOperateUser();

        this.sentinelApolloPublicProperties = sentinelApolloPublicProperties;
        this.apolloOpenApiClient = apolloOpenApiClient;
    }

    /**
     * every project name in sentinel is one to one with apollo's public namespace.
     */
    private String resolvePublicNamespaceName(String projectName) {
        // recommend use orgId.sentinel.project-own-appId
        return this.sentinelApolloPublicProperties.getNamespacePrefix() + projectName;
    }

    /**
     * @see com.alibaba.cloud.sentinel.datasource.config.ApolloDataSourceProperties#setFlowRulesKey(String)
     */
    private String resolveFlowRulesKey(String projectName, RuleType ruleType) {
        return projectName + this.sentinelApolloPublicProperties.getSuffix().get(ruleType);
    }

    /**
     * create, publish, authorize to current user about new public namespace.
     * use synchronized to forbid concurrent problem, because there is no performance problem here.
     * @return null if resolve failed
     */
    private Object resolvePublicNamespaceInApollo(String publicNamespaceName) {

        // create
        OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
        openAppNamespaceDTO.setName(publicNamespaceName);
        openAppNamespaceDTO.setAppId(this.appId);
        openAppNamespaceDTO.setFormat(ConfigFileFormat.Properties.getValue());
        openAppNamespaceDTO.setPublic(true);
        // disable auto prefix with orgId
        openAppNamespaceDTO.setAppendNamespacePrefix(false);
        openAppNamespaceDTO.setDataChangeCreatedBy(this.operateUser);

        try {
            this.apolloOpenApiClient.createAppNamespace(openAppNamespaceDTO);
        } catch (RuntimeException e) {
            logger.error("create public namespace fail. public namespace's name = " + publicNamespaceName, e);
        }

        // still execute follow operations

        // publish
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleasedBy(this.operateUser);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        final String currentDateString = simpleDateFormat.format(new Date());
        namespaceReleaseDTO.setReleaseTitle(currentDateString + "-first-publish-release");

        this.apolloOpenApiClient.publishNamespace(this.appId, this.env, this.clusterName, publicNamespaceName, namespaceReleaseDTO);

        // authorize
        // TODO

        return new Object();
    }

    private void ensurePublicNamespaceExists(String publicNamespaceName) {
        // query by apollo portal
        try {
            this.publicNamespaceNames.computeIfAbsent(publicNamespaceName, key -> this.apolloOpenApiClient.getNamespace(appId, env, clusterName, key));
        } catch (RuntimeException e) {
            this.logger.warn("get public namespace [{}] meet an RuntimeException, may not exists in apollo. exception message = '{}'", publicNamespaceName, e.getMessage());
        }

        this.publicNamespaceNames.computeIfAbsent(publicNamespaceName, this::resolvePublicNamespaceInApollo);
    }

    public void registryProjectIfNotExists(String projectName) {
        final String publicNamespaceName = this.resolvePublicNamespaceName(projectName);
        if (this.publicNamespaceNames.containsKey(publicNamespaceName)) {
            return;
        }
        this.ensurePublicNamespaceExists(publicNamespaceName);
    }

    public CompletableFuture<Void> setRulesAsync(String projectName, RuleType ruleType, List<? extends Rule> rules) {
        this.registryProjectIfNotExists(projectName);

        OpenItemDTO openItemDTO = new OpenItemDTO();
        final String ruleKey = this.resolveFlowRulesKey(projectName, ruleType);
        openItemDTO.setKey(ruleKey);

        // TODO, use json converter in spring-cloud-starter-alibaba-sentinel defined in SentinelConverterConfiguration?
        final String value = JSON.toJSONString(rules, true);
        openItemDTO.setValue(value);
        openItemDTO.setDataChangeCreatedBy(this.operateUser);

        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleasedBy(this.operateUser);
        namespaceReleaseDTO.setReleaseTitle(projectName + "." + ruleType);

        final String publicNamespaceName = this.resolvePublicNamespaceName(projectName);
        Runnable runnable = () -> {
            apolloOpenApiClient.createOrUpdateItem(this.appId, this.env, this.clusterName, publicNamespaceName, openItemDTO);
            apolloOpenApiClient.publishNamespace(this.appId, this.env, this.clusterName, publicNamespaceName, namespaceReleaseDTO);
        };

        return CompletableFuture.runAsync(runnable);
    }

}
