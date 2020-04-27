/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.jaxrs.config;

import com.alibaba.csp.sentinel.adapter.jaxrs.fallback.DefaultSentinelJaxRsFallback;
import com.alibaba.csp.sentinel.adapter.jaxrs.fallback.SentinelJaxRsFallback;
import com.alibaba.csp.sentinel.adapter.jaxrs.request.DefaultRequestOriginParser;
import com.alibaba.csp.sentinel.adapter.jaxrs.request.DefaultResourceNameParser;
import com.alibaba.csp.sentinel.adapter.jaxrs.request.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.jaxrs.request.ResourceNameParser;

/**
 *  @author sea
 */
public class SentinelJaxRsConfig {

    private static volatile ResourceNameParser resourceNameParser = new DefaultResourceNameParser();

    private static volatile RequestOriginParser requestOriginParser = new DefaultRequestOriginParser();

    private static volatile SentinelJaxRsFallback jaxRsFallback = new DefaultSentinelJaxRsFallback();

    public static ResourceNameParser getResourceNameParser() {
        return resourceNameParser;
    }

    public static void setResourceNameParser(ResourceNameParser resourceNameParser) {
        SentinelJaxRsConfig.resourceNameParser = resourceNameParser;
    }

    public static RequestOriginParser getRequestOriginParser() {
        return requestOriginParser;
    }

    public static void setRequestOriginParser(RequestOriginParser originParser) {
        SentinelJaxRsConfig.requestOriginParser = originParser;
    }

    public static SentinelJaxRsFallback getJaxRsFallback() {
        return jaxRsFallback;
    }

    public static void setJaxRsFallback(SentinelJaxRsFallback jaxRsFallback) {
        SentinelJaxRsConfig.jaxRsFallback = jaxRsFallback;
    }

    private SentinelJaxRsConfig() {
    }
}
