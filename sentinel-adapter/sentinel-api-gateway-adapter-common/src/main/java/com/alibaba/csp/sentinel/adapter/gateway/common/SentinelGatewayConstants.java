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
package com.alibaba.csp.sentinel.adapter.gateway.common;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class SentinelGatewayConstants {

    public static final int APP_TYPE_GATEWAY = 1;

    public static final int RESOURCE_MODE_ROUTE_ID = 0;
    public static final int RESOURCE_MODE_CUSTOM_API_NAME = 1;

    public static final int PARAM_PARSE_STRATEGY_CLIENT_IP = 0;
    public static final int PARAM_PARSE_STRATEGY_HOST = 1;
    public static final int PARAM_PARSE_STRATEGY_HEADER = 2;
    public static final int PARAM_PARSE_STRATEGY_URL_PARAM = 3;
    public static final int PARAM_PARSE_STRATEGY_COOKIE = 4;

    public static final int URL_MATCH_STRATEGY_EXACT = 0;
    public static final int URL_MATCH_STRATEGY_PREFIX = 1;
    public static final int URL_MATCH_STRATEGY_REGEX = 2;

    public static final int PARAM_MATCH_STRATEGY_EXACT = 0;
    public static final int PARAM_MATCH_STRATEGY_PREFIX = 1;
    public static final int PARAM_MATCH_STRATEGY_REGEX = 2;
    public static final int PARAM_MATCH_STRATEGY_CONTAINS = 3;

    public static final String GATEWAY_CONTEXT_DEFAULT = "sentinel_gateway_context_default";
    public static final String GATEWAY_CONTEXT_PREFIX = "sentinel_gateway_context$$";
    public static final String GATEWAY_CONTEXT_ROUTE_PREFIX = "sentinel_gateway_context$$route$$";

    public static final String GATEWAY_NOT_MATCH_PARAM = "$NM";
    public static final String GATEWAY_DEFAULT_PARAM = "$D";

    private SentinelGatewayConstants() {}
}
