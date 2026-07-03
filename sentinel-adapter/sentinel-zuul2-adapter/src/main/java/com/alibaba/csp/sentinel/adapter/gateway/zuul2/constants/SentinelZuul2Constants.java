/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants;

/**
 * @author wavesZh
 */
public class SentinelZuul2Constants {
    /**
     * The default entrance (context) name when the routeId is empty.
     */
    public static final String ZUUL_DEFAULT_CONTEXT = "zuul2_default_context";
    /**
     * Zuul context key for keeping Sentinel entries.
     */
    public static final String ZUUL_CTX_SENTINEL_ENTRIES_KEY = "_sentinel_entries";

    public static final String ZUUL_CTX_SENTINEL_FALLBACK_ROUTE = "_sentinel_fallback_route";
    /**
     * Indicate if request is blocked .
     */
    public static final String ZUUL_CTX_SENTINEL_BLOCKED_FLAG = "_sentinel_blocked_flag";

    private SentinelZuul2Constants() {}
}
