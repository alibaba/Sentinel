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
package com.alibaba.csp.sentinel.dashboard.apollo.repository.project;

import com.alibaba.csp.sentinel.dashboard.apollo.config.SentinelApolloOpenApiProperties;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * save information to apollo's private namespace of current application.
 * @author wxq
 */
public class ApolloProjectStore implements ProjectRepository {

    private final Logger logger = LoggerFactory.getLogger(ApolloProjectStore.class);

    private static final String DEFAULT_NAMESPACE_NAME = "storage";

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final String appId;
    private final String env;
    private final String clusterName;
    /**
     * store sentinel dashboard's data in this private namespace.
     */
    private final String namespaceName = DEFAULT_NAMESPACE_NAME;
    private final String operatedUser;

    private volatile boolean selfPrivateNamespaceExists = false;

    private final Object lock = new Object();

    public ApolloProjectStore(
            String appId,
            ApolloOpenApiClient apolloOpenApiClient,
            SentinelApolloOpenApiProperties sentinelApolloOpenApiProperties) {
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.appId = appId;
        this.env = sentinelApolloOpenApiProperties.getOperatedEnv();
        this.clusterName = sentinelApolloOpenApiProperties.getOperatedCluster();
        this.operatedUser = sentinelApolloOpenApiProperties.getOperatedUser();
    }

    private void ensureSelfPrivateNamespaceExists() {
        if (false == this.selfPrivateNamespaceExists) {
            synchronized (this.lock) {
                if (false == this.selfPrivateNamespaceExists) {
                    this.createSelfPrivateNamespaceIfNotExists();
                    this.selfPrivateNamespaceExists = true;
                    logger.info("self private namespace [{}] create success or exist already.", this.namespaceName);
                }
            }
        }
    }

    /**
     * watch out that apollo portal may be not available.
     */
    private void createSelfPrivateNamespaceIfNotExists() {
        try {
            this.apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
            return;
        } catch (ApolloOpenApiException e) {
            logger.warn("namespace {} may not exists or apollo portal is unavailable now. status = {}, exception message = {}", namespaceName, e.getStatus(), e.getMessage());
        }

        OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
        openAppNamespaceDTO.setName(namespaceName);
        openAppNamespaceDTO.setAppId(appId);
        openAppNamespaceDTO.setFormat(ConfigFileFormat.Properties.getValue());
        openAppNamespaceDTO.setDataChangeCreatedBy(operatedUser);

        this.apolloOpenApiClient.createAppNamespace(openAppNamespaceDTO);
        logger.info("self private namespace [{}] create success.", this.namespaceName);
    }

    @Override
    public void add(String projectName) {
        this.ensureSelfPrivateNamespaceExists();
        if (this.exists(projectName)) {
            return;
        }

        logger.info("project [{}] not exists, now try to add it", projectName);

        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(projectName);
        openItemDTO.setValue(new Date().toString());
        openItemDTO.setDataChangeCreatedBy(operatedUser);
        this.apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);
    }

    @Override
    public int delete(String projectName) {
        this.ensureSelfPrivateNamespaceExists();
        if (this.exists(projectName)) {
            this.apolloOpenApiClient.removeItem(appId, env, clusterName, namespaceName, projectName, operatedUser);
            logger.debug("delete project [{}] success.", projectName);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean exists(String projectName) {
        this.ensureSelfPrivateNamespaceExists();
        return null != apolloOpenApiClient.getItem(this.appId, this.env, this.clusterName, this.namespaceName, projectName);
    }

    @Override
    public Set<String> findAll() {
        this.ensureSelfPrivateNamespaceExists();
        OpenNamespaceDTO openNamespaceDTO = this.apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
        List<OpenItemDTO> openItemDTOS = openNamespaceDTO.getItems();
        Set<String> projectNames = new HashSet<>();
        for (OpenItemDTO openItemDTO : openItemDTOS) {
            String projectName = openItemDTO.getKey();
            projectNames.add(projectName);
        }
        return Collections.unmodifiableSet(projectNames);
    }

    @Override
    public Set<String> deleteAll() {
        this.ensureSelfPrivateNamespaceExists();
        Set<String> projectNames = this.findAll();
        for (String projectName : projectNames) {
            this.delete(projectName);
        }
        return projectNames;
    }

}
