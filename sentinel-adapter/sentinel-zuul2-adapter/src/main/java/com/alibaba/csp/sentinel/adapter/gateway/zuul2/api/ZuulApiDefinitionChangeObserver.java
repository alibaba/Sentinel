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
package com.alibaba.csp.sentinel.adapter.gateway.zuul2.api;

import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinitionChangeObserver;

/**
 * @author Eric Zhao
 * @since 1.7.2
 */
public class ZuulApiDefinitionChangeObserver implements ApiDefinitionChangeObserver {

    @Override
    public void onChange(Set<ApiDefinition> apiDefinitions) {
        ZuulGatewayApiMatcherManager.loadApiDefinitions(apiDefinitions);
    }
}
