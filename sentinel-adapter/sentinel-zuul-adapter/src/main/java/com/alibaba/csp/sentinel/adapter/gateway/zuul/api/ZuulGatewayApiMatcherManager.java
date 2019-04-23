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
package com.alibaba.csp.sentinel.adapter.gateway.zuul.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.matcher.RequestContextApiMatcher;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class ZuulGatewayApiMatcherManager {

    private static final Map<String, RequestContextApiMatcher> API_MATCHER_MAP = new ConcurrentHashMap<>();

    public static Map<String, RequestContextApiMatcher> getApiMatcherMap() {
        return Collections.unmodifiableMap(API_MATCHER_MAP);
    }

    public static RequestContextApiMatcher getMatcher(final String apiName) {
        if (apiName == null) {
            return null;
        }
        return API_MATCHER_MAP.get(apiName);
    }

    public static Set<ApiDefinition> getApiDefinitionSet() {
        Set<ApiDefinition> set = new HashSet<>();
        for (RequestContextApiMatcher matcher : API_MATCHER_MAP.values()) {
            set.add(matcher.getApiDefinition());
        }
        return set;
    }

    static synchronized void loadApiDefinitions(/*@Valid*/ Set<ApiDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            API_MATCHER_MAP.clear();
            return;
        }
        for (ApiDefinition definition : definitions) {
            addApiDefinition(definition);
        }
    }

    static void addApiDefinition(ApiDefinition definition) {
        API_MATCHER_MAP.put(definition.getApiName(), new RequestContextApiMatcher(definition));
    }

    private ZuulGatewayApiMatcherManager() {}
}
