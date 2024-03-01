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
package com.alibaba.csp.sentinel.webflow.param;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;

import java.util.regex.Pattern;

/**
 * @author guanyu
 * @author Eric Zhao
 * @since 1.8.8
 */
public class WebParamParser<T> {

    private static final String UNKNOWN = "unknown";
    private static final String SEPARATOR = ",";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_HTTP_CLIENT_IP = "HTTP_CLIENT_IP";

    private final RequestItemParser<T> requestItemParser;

    public WebParamParser(RequestItemParser<T> requestItemParser) {
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");
        this.requestItemParser = requestItemParser;
    }

    static String parseWithMatchStrategyInternal(Integer matchStrategy, String value, String pattern) {
        if (value == null || matchStrategy == null) {
            return null;
        }
        switch (matchStrategy) {
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_EXACT:
                return value.equals(pattern) ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_CONTAINS:
                return value.contains(pattern) ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_REGEX:
                Pattern regex = ParamRegexCache.getRegexPattern(pattern);
                if (regex == null) {
                    return value;
                }
                return regex.matcher(value).matches() ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            default:
                return value;
        }
    }
}
