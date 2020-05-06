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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.config;

import com.alibaba.csp.sentinel.adapter.apache.httpclient.cleaner.ApacheHttpClientUrlCleaner;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.cleaner.DefaultApacheHttpClientUrlCleaner;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback.ApacheHttpClientFallback;
import com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback.DefaultApacheHttpClientFallback;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhaoyuguang
 */
public final class SentinelApacheHttpClientConfig {

    private static volatile String attributeName = HttpContext.RESERVED_PREFIX + "sentinel_apache_httpclient_entry_attr";
    private static volatile ApacheHttpClientUrlCleaner cleaner = new DefaultApacheHttpClientUrlCleaner();
    private static volatile ApacheHttpClientFallback fallback = new DefaultApacheHttpClientFallback();

    public static String getAttributeName() {
        return attributeName;
    }

    public static void setAttributeName(String attributeName) {
        AssertUtil.notNull(attributeName, "attributeName cannot be null");
        SentinelApacheHttpClientConfig.attributeName = attributeName;
    }

    public static ApacheHttpClientUrlCleaner getCleaner() {
        return cleaner;
    }

    public static void setCleaner(ApacheHttpClientUrlCleaner cleaner) {
        AssertUtil.notNull(cleaner, "cleaner cannot be null");
        SentinelApacheHttpClientConfig.cleaner = cleaner;
    }

    public static ApacheHttpClientFallback getFallback() {
        return fallback;
    }

    public static void setFallback(ApacheHttpClientFallback fallback) {
        AssertUtil.notNull(fallback, "fallback cannot be null");
        SentinelApacheHttpClientConfig.fallback = fallback;
    }
}
