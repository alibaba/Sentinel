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
package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author Eric Zhao
 * @since 1.6.2
 */
public final class GatewayRegexCache {

    private static final Map<String, Pattern> REGEX_CACHE = new ConcurrentHashMap<>();

    public static Pattern getRegexPattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        return REGEX_CACHE.get(pattern);
    }

    public static boolean addRegexPattern(String pattern) {
        if (pattern == null) {
            return false;
        }
        try {
            Pattern regex = Pattern.compile(pattern);
            REGEX_CACHE.put(pattern, regex);
            return true;
        } catch (Exception ex) {
            RecordLog.warn("[GatewayRegexCache] Failed to compile the regex: " + pattern, ex);
            return false;
        }
    }

    public static void clear() {
        REGEX_CACHE.clear();
    }

    private GatewayRegexCache() {}
}
