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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.DefaultRestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.DefaultRestClientFallback;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.RestClientFallback;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Configuration for Sentinel RestClient interceptor.
 *
 * @author QHT, uuuyuqi
 */
public class SentinelRestClientConfig {

    public static final String DEFAULT_RESOURCE_PREFIX = "restclient:";

    private final String resourcePrefix;
    private final RestClientResourceExtractor resourceExtractor;
    private final RestClientFallback fallback;

    public SentinelRestClientConfig() {
        this(DEFAULT_RESOURCE_PREFIX);
    }

    public SentinelRestClientConfig(String resourcePrefix) {
        this(resourcePrefix, new DefaultRestClientResourceExtractor(), new DefaultRestClientFallback());
    }

    public SentinelRestClientConfig(RestClientResourceExtractor resourceExtractor, RestClientFallback fallback) {
        this(DEFAULT_RESOURCE_PREFIX, resourceExtractor, fallback);
    }

    public SentinelRestClientConfig(String resourcePrefix,
                                    RestClientResourceExtractor resourceExtractor,
                                    RestClientFallback fallback) {
        AssertUtil.notNull(resourceExtractor, "resourceExtractor cannot be null");
        AssertUtil.notNull(fallback, "fallback cannot be null");
        this.resourcePrefix = resourcePrefix;
        this.resourceExtractor = resourceExtractor;
        this.fallback = fallback;
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    public RestClientResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }

    public RestClientFallback getFallback() {
        return fallback;
    }

    @Override
    public String toString() {
        return "SentinelRestClientConfig{" +
            "resourcePrefix='" + resourcePrefix + '\'' +
            ", resourceExtractor=" + resourceExtractor +
            ", fallback=" + fallback +
            '}';
    }
}