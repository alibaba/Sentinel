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

package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.rule.ApolloProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.RuleConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.RuleType;
import com.alibaba.csp.sentinel.dashboard.rule.aop.SentinelApiClientAspect;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FengJianxin
 * @since 2022/7/29
 */
public class DynamicRuleApolloStore<T extends RuleEntity> extends DynamicRuleStore<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelApiClientAspect.class);

    private final ApolloProperties apolloProperties;
    private final ApolloOpenApiClientProvider openApiClientProvider;

    public DynamicRuleApolloStore(final RuleType ruleType,
                                  final ApolloProperties apolloProperties,
                                  final ApolloOpenApiClientProvider openApiClientProvider) {
        super.ruleType = ruleType;
        this.apolloProperties = apolloProperties;
        this.openApiClientProvider = openApiClientProvider;
    }


    @Override
    public List<T> getRules(final String appName) throws Exception {
        ApolloOpenApiClient apolloOpenApiClient = openApiClientProvider.get();
        String appId = apolloProperties.getAppId();
        String env = apolloProperties.getEnv();
        String clusterName = apolloProperties.getClusterName();
        String namespace = apolloProperties.getNamespace();
        String flowDataId = RuleConfigUtil.getDataId(appName, ruleType);
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespace);
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        Converter<String, List<T>> decoder = RuleConfigUtil.getDecoder(ruleType.getClazz());
        return decoder.convert(rules);
    }

    @Override
    public void publish(final String app, final List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        ApolloOpenApiClient apolloOpenApiClient = openApiClientProvider.get();
        String appId = apolloProperties.getAppId();
        String env = apolloProperties.getEnv();
        String clusterName = apolloProperties.getClusterName();
        String namespace = apolloProperties.getNamespace();
        String flowDataId = RuleConfigUtil.getDataId(app, ruleType);
        Converter<Object, String> encoder = RuleConfigUtil.getEncoder();
        String value = encoder.convert(rules);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(value);
        openItemDTO.setComment("update from sentinel-dashboard");
        openItemDTO.setDataChangeCreatedBy(apolloProperties.getOperator());
        apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespace, openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleasedBy(apolloProperties.getOperator());
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespace, namespaceReleaseDTO);
        LOG.info("publish rule success - app: {}, type: {}, value: {}", app, ruleType.getName(), value);
    }


}
