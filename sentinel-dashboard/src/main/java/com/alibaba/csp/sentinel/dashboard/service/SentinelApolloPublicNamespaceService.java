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
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.alibaba.csp.sentinel.dashboard.util.DataSourceConverterUtils.SERIALIZER;

@Service
public class SentinelApolloPublicNamespaceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String operatedAppId;

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
        this.operatedAppId = sentinelApolloPrivateConfiguration.getOperatedAppId();
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
     * reverse operation with {@link #resolvePublicNamespaceName(String)}.
     */
    private String deResolvePublicNamespaceName(String publicNamespaceName) {
        Assert.notNull(publicNamespaceName, "public namespace name should not be null");
        final String namespacePrefix = this.sentinelApolloPublicProperties.getNamespacePrefix();
        final int namespacePrefixLength = namespacePrefix.length();
        Assert.isTrue(
                publicNamespaceName.length() > namespacePrefixLength,
                "public namespace name's length " + publicNamespaceName.length() + "should bigger that namespace prefix " + namespacePrefix + "'s length" + namespacePrefixLength
        );
        return publicNamespaceName.substring(namespacePrefixLength);
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
     *
     * @return null if resolve failed
     */
    private Object resolvePublicNamespaceInApollo(String publicNamespaceName) {

        // create
        OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
        openAppNamespaceDTO.setName(publicNamespaceName);
        openAppNamespaceDTO.setAppId(this.operatedAppId);
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

        this.apolloOpenApiClient.publishNamespace(this.operatedAppId, this.env, this.clusterName, publicNamespaceName, namespaceReleaseDTO);

        // authorize
        // TODO

        return new Object();
    }

    private void ensurePublicNamespaceExists(String publicNamespaceName) {
        // query by apollo portal
        try {
            this.publicNamespaceNames.computeIfAbsent(publicNamespaceName, key -> this.apolloOpenApiClient.getNamespace(operatedAppId, env, clusterName, key));
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
        final String value = SERIALIZER.convert(rules);
        openItemDTO.setValue(value);
        openItemDTO.setDataChangeCreatedBy(this.operateUser);

        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleasedBy(this.operateUser);
        namespaceReleaseDTO.setReleaseTitle(projectName + "." + ruleType);

        final String publicNamespaceName = this.resolvePublicNamespaceName(projectName);
        Runnable runnable = () -> {
            apolloOpenApiClient.createOrUpdateItem(this.operatedAppId, this.env, this.clusterName, publicNamespaceName, openItemDTO);
            apolloOpenApiClient.publishNamespace(this.operatedAppId, this.env, this.clusterName, publicNamespaceName, namespaceReleaseDTO);
        };

        return CompletableFuture.runAsync(runnable);
    }

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

    /**
     * @return project's name in sentinel dashboard's cache
     */
    public Set<String> listCachedProjectNames() {
        return this.publicNamespaceNames.keySet().stream().map(this::deResolvePublicNamespaceName).collect(Collectors.toSet());
    }

    public void clearCacheOfProject(String projectName) {
        String publicNamespaceName = this.resolvePublicNamespaceName(projectName);
        this.publicNamespaceNames.remove(publicNamespaceName);
    }

    /**
     * this method is not atomic.
     * maybe exists concurrent problem with {@link #ensurePublicNamespaceExists(String)}.
     *
     * @return project's names which cleared in cache
     */
    public Set<String> clearAllCachedProjectNames() {
        Set<String> projectNames = new TreeSet<>(this.listCachedProjectNames());
        for (String projectName : projectNames) {
            this.clearCacheOfProject(projectName);
        }
        return projectNames;
    }

}
