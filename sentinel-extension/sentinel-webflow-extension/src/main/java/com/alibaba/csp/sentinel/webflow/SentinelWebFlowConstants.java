/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.webflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaunyu
 * @since 1.10.0
 */
public final class SentinelWebFlowConstants {

    public static final int RESOURCE_MODE_INTERFACE_ID = 0;
    public static final int RESOURCE_MODE_CUSTOM_API_NAME = 1;

    public static final int PARAM_PARSE_STRATEGY_CLIENT_IP = 0;
    public static final int PARAM_PARSE_STRATEGY_HOST = 1;
    public static final int PARAM_PARSE_STRATEGY_HEADER = 2;
    public static final int PARAM_PARSE_STRATEGY_URL_PARAM = 3;
    public static final int PARAM_PARSE_STRATEGY_COOKIE = 4;
    public static final int PARAM_PARSE_STRATEGY_BODY_PARAM = 5;
    public static final int PARAM_PARSE_STRATEGY_PATH_PARAM = 6;

    public static final int URL_MATCH_STRATEGY_EXACT = 0;
    public static final int URL_MATCH_STRATEGY_PREFIX = 1;
    public static final int URL_MATCH_STRATEGY_REGEX = 2;

    public static final int PARAM_MATCH_STRATEGY_EXACT = 0;
    public static final int PARAM_MATCH_STRATEGY_PREFIX = 1;
    public static final int PARAM_MATCH_STRATEGY_REGEX = 2;
    public static final int PARAM_MATCH_STRATEGY_CONTAINS = 3;

    public static final String WEB_FLOW_CONTEXT_DEFAULT = "sentinel_web_flow_context_default";
    public static final String WEB_FLOW_CONTEXT_PREFIX = "sentinel_web_flow_context$$";
    public static final String WEB_FLOW_CONTEXT_ROUTE_PREFIX = "sentinel_web_flow_context$$route$$";

    public static final String WEB_FLOW_NOT_MATCH_PARAM = "$NM";
    public static final String WEB_FLOW_DEFAULT_PARAM = "$D";

    public static final String WEB_PARAM_UNKNOWN_PARSE_STRATEGY_KEY = "UNKNOWN";
    public static final Map<Integer, String> PARSE_STRATEGY_KEY_MAP = Collections.unmodifiableMap(
            new HashMap<Integer, String>(8) {{
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_CLIENT_IP, "CLIENT_IP");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HOST, "HOST");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HEADER, "HEADER");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_URL_PARAM, "URL_PARAM");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_COOKIE, "COOKIE");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_BODY_PARAM, "BODY");
                put(SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_PATH_PARAM, "PATH");
            }});

    public static final String WEB_FLOW_NON_PARAM_DEFAULT_KEY = "DEFAULT_KEY";

    private SentinelWebFlowConstants() {}
}
