/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.sc.api;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.matcher.WebExchangeApiMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class GatewayApiMatcherManager {

    private static volatile Map<String, WebExchangeApiMatcher> API_MATCHER_MAP = new HashMap<>();

    public static Map<String, WebExchangeApiMatcher> getApiMatcherMap() {
        return Collections.unmodifiableMap(API_MATCHER_MAP);
    }

    public static Optional<WebExchangeApiMatcher> getMatcher(final String apiName) {
        return Optional.ofNullable(apiName)
            .map(e -> API_MATCHER_MAP.get(apiName));
    }

    public static Set<ApiDefinition> getApiDefinitionSet() {
        return API_MATCHER_MAP.values()
            .stream()
            .map(WebExchangeApiMatcher::getApiDefinition)
            .collect(Collectors.toSet());
    }

    static synchronized void loadApiDefinitions(/*@Valid*/ Set<ApiDefinition> definitions) {
        Map<String, WebExchangeApiMatcher> apiMatcherMap = new HashMap<>();
        for (ApiDefinition definition : definitions) {
            apiMatcherMap.put(definition.getApiName(), new WebExchangeApiMatcher(definition));
        }

        API_MATCHER_MAP = apiMatcherMap;
    }

    private GatewayApiMatcherManager() {}
}
