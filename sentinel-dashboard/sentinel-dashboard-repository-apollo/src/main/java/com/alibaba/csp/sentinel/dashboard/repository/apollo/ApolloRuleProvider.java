/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.apollo;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRuleProvider;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class ApolloRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);

        String appId = "appId";
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, "DEV", "default", "application");
        String rules = openNamespaceDTO
            .getItems()
            .stream()
            .filter(p -> p.getKey().equals(ruleKey))
            .map(OpenItemDTO::getValue)
            .findFirst()
            .orElse("");
        return rules;
    }
}
