/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Spring Web MVC interceptor that integrates with Sentinel.
 * <p>
 * This will record resource as `${httpMethod}:${uri}`.
 *
 * @since 1.8.8
 */
public class SentinelWebPrefixInterceptor extends SentinelWebInterceptor {

    @Override
    protected String getResourceName(HttpServletRequest request) {
        String resourceName = super.getResourceName(request);
        // Add method specification
        if (StringUtil.isNotEmpty(resourceName)) {
            resourceName = request.getMethod().toUpperCase() + ":" + resourceName;
        }
        return resourceName;
    }
}