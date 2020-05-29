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
package com.alibaba.csp.sentinel.adapter.okhttp.config;

import com.alibaba.csp.sentinel.adapter.okhttp.extractor.DefaultOkHttpResourceExtractor;
import com.alibaba.csp.sentinel.adapter.okhttp.extractor.OkHttpResourceExtractor;
import com.alibaba.csp.sentinel.adapter.okhttp.fallback.DefaultOkHttpFallback;
import com.alibaba.csp.sentinel.adapter.okhttp.fallback.OkHttpFallback;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author zhaoyuguang
 */
public final class SentinelOkHttpConfig {

    private static volatile String prefix = "okhttp:";
    private static volatile OkHttpResourceExtractor extractor = new DefaultOkHttpResourceExtractor();
    private static volatile OkHttpFallback fallback = new DefaultOkHttpFallback();

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        AssertUtil.notNull(prefix, "prefix cannot be null");
        SentinelOkHttpConfig.prefix = prefix;
    }

    public static OkHttpResourceExtractor getExtractor() {
        return extractor;
    }

    public static void setExtractor(OkHttpResourceExtractor extractor) {
        AssertUtil.notNull(extractor, "extractor cannot be null");
        SentinelOkHttpConfig.extractor = extractor;
    }

    public static OkHttpFallback getFallback() {
        return fallback;
    }

    public static void setFallback(OkHttpFallback fallback) {
        AssertUtil.notNull(fallback, "fallback cannot be null");
        SentinelOkHttpConfig.fallback = fallback;
    }
}
