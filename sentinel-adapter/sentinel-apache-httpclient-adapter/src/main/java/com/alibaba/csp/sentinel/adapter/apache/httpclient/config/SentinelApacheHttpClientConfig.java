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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.config;

import com.alibaba.csp.sentinel.adapter.apache.httpclient.extractor.ApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.extractor.DefaultApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback.ApacheHttpClientFallback;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback.DefaultApacheHttpClientFallback;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author zhaoyuguang
 */
public final class SentinelApacheHttpClientConfig {

    private static volatile String prefix = "httpclient:";
    private static volatile ApacheHttpClientResourceExtractor extractor = new DefaultApacheHttpClientResourceExtractor();
    private static volatile ApacheHttpClientFallback fallback = new DefaultApacheHttpClientFallback();

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        AssertUtil.notNull(prefix, "prefix cannot be null");
        SentinelApacheHttpClientConfig.prefix = prefix;
    }

    public static ApacheHttpClientResourceExtractor getExtractor() {
        return extractor;
    }

    public static void setExtractor(ApacheHttpClientResourceExtractor extractor) {
        AssertUtil.notNull(extractor, "extractor cannot be null");
        SentinelApacheHttpClientConfig.extractor = extractor;
    }

    public static ApacheHttpClientFallback getFallback() {
        return fallback;
    }

    public static void setFallback(ApacheHttpClientFallback fallback) {
        AssertUtil.notNull(fallback, "fallback cannot be null");
        SentinelApacheHttpClientConfig.fallback = fallback;
    }
}
